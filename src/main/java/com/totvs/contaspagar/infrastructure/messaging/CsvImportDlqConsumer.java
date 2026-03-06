package com.totvs.contaspagar.infrastructure.messaging;

import com.totvs.contaspagar.infrastructure.persistence.entity.ImportacaoLog;
import com.totvs.contaspagar.infrastructure.persistence.jpa.ImportacaoLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumer da Dead Letter Queue (DLQ).
 * Processa mensagens que falharam após todas as tentativas de retry.
 * Marca o ImportacaoLog correspondente como FALHA para que o usuário
 * possa consultar o status e reenviar o arquivo se necessário.
 */
@Component
public class CsvImportDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(CsvImportDlqConsumer.class);

    private final ImportacaoLogJpaRepository importacaoLogRepository;

    public CsvImportDlqConsumer(ImportacaoLogJpaRepository importacaoLogRepository) {
        this.importacaoLogRepository = importacaoLogRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CSV_DLQ)
    @Transactional
    public void processar(CsvImportMessage message) {
        log.error("Mensagem na DLQ após todas as tentativas de retry. Protocolo: {}", message.protocoloId());

        importacaoLogRepository.findByProtocoloId(message.protocoloId())
                .ifPresentOrElse(
                        logEntry -> {
                            logEntry.falhar("Processamento falhou após múltiplas tentativas. Reenvie o arquivo CSV.");
                            importacaoLogRepository.save(logEntry);
                            log.error("ImportacaoLog {} marcado como FALHA.", message.protocoloId());
                        },
                        () -> log.error("ImportacaoLog não encontrado para protocolo: {}", message.protocoloId())
                );
    }
}
