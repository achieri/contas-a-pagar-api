package com.totvs.contaspagar.infrastructure.messaging;

import com.totvs.contaspagar.domain.exception.FornecedorNaoEncontradoException;
import com.totvs.contaspagar.domain.model.Conta;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
import com.totvs.contaspagar.infrastructure.csv.CsvParser;
import com.totvs.contaspagar.infrastructure.persistence.entity.ImportacaoLog;
import com.totvs.contaspagar.infrastructure.persistence.jpa.ImportacaoLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer assíncrono que processa arquivos CSV de importação em lote.
 *
 * Estratégia de resiliência:
 *  - Cada linha é processada de forma independente (falha parcial não para o lote)
 *  - Erros de linha são logados e contabilizados no ImportacaoLog
 *  - Erros fatais (ex: mensagem corrompida) enviam para a DLQ após 3 tentativas
 */
@Component
public class CsvImportConsumer {

    private static final Logger log = LoggerFactory.getLogger(CsvImportConsumer.class);

    private final CsvParser csvParser;
    private final ContaRepository contaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ImportacaoLogJpaRepository importacaoLogRepository;

    public CsvImportConsumer(CsvParser csvParser,
                              ContaRepository contaRepository,
                              FornecedorRepository fornecedorRepository,
                              ImportacaoLogJpaRepository importacaoLogRepository) {
        this.csvParser = csvParser;
        this.contaRepository = contaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.importacaoLogRepository = importacaoLogRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CSV)
    @Transactional
    public void processar(CsvImportMessage message) {
        log.info("Iniciando processamento CSV. Protocolo: {}", message.protocoloId());

        var logEntry = importacaoLogRepository.findByProtocoloId(message.protocoloId())
                .orElseGet(() -> ImportacaoLog.criar(message.protocoloId()));

        try {
            var resultado = csvParser.parsear(message.csvContent());
            var linhasValidas = resultado.linhasValidas();
            var errosParsing = new java.util.ArrayList<>(resultado.erros());

            if (linhasValidas.isEmpty() && errosParsing.isEmpty()) {
                logEntry.falhar("Arquivo CSV não contém dados para importar.");
                importacaoLogRepository.save(logEntry);
                log.warn("Protocolo {}: arquivo sem dados.", message.protocoloId());
                return;
            }

            logEntry.iniciarProcessamento(linhasValidas.size() + errosParsing.size());
            importacaoLogRepository.save(logEntry);

            int processadas = 0;
            int comErro = errosParsing.size();

            for (var linha : linhasValidas) {
                try {
                    processarLinha(linha);
                    processadas++;
                } catch (FornecedorNaoEncontradoException e) {
                    log.warn("Fornecedor não encontrado para linha do CSV: {}", e.getMessage());
                    errosParsing.add("Fornecedor não encontrado: " + linha.fornecedorId());
                    comErro++;
                } catch (Exception e) {
                    log.error("Erro inesperado ao processar linha do CSV", e);
                    errosParsing.add("Erro inesperado: " + e.getMessage());
                    comErro++;
                }
            }

            String detalhes = errosParsing.isEmpty() ? null : String.join(" | ", errosParsing);
            logEntry.concluir(processadas, comErro, detalhes);
            importacaoLogRepository.save(logEntry);

            log.info("Processamento concluído. Protocolo: {} | Processadas: {} | Com erro: {}",
                    message.protocoloId(), processadas, comErro);

        } catch (Exception e) {
            log.error("Falha crítica no processamento do CSV. Protocolo: {}", message.protocoloId(), e);
            logEntry.falhar(e.getMessage());
            importacaoLogRepository.save(logEntry);
            // Re-lança para acionar o mecanismo de retry do RabbitMQ → DLQ
            throw new RuntimeException("Falha crítica no processamento CSV", e);
        }
    }

    private void processarLinha(CsvParser.LinhaCSV linha) {
        var fornecedor = fornecedorRepository.buscarPorId(linha.fornecedorId())
                .orElseThrow(() -> new FornecedorNaoEncontradoException(linha.fornecedorId()));

        var conta = Conta.criar(linha.dataVencimento(), linha.valor(), linha.descricao(), fornecedor);
        contaRepository.salvar(conta);
    }
}
