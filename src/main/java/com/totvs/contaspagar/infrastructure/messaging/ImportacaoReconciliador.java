package com.totvs.contaspagar.infrastructure.messaging;

import com.totvs.contaspagar.infrastructure.persistence.entity.ImportacaoLog;
import com.totvs.contaspagar.infrastructure.persistence.jpa.ImportacaoLogJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Job de reconciliação de importações travadas.
 *
 * Detecta ImportacaoLogs no status PROCESSANDO há mais de 10 minutos
 * (sinal de falha silenciosa ou queda durante o processamento) e os
 * marca como FALHA para que o usuário possa reenviar o arquivo.
 *
 * Roda a cada 5 minutos. Requer @EnableScheduling na classe principal.
 */
@Component
public class ImportacaoReconciliador {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoReconciliador.class);
    private static final int TIMEOUT_MINUTOS = 10;

    private final ImportacaoLogJpaRepository importacaoLogRepository;

    public ImportacaoReconciliador(ImportacaoLogJpaRepository importacaoLogRepository) {
        this.importacaoLogRepository = importacaoLogRepository;
    }

    @Scheduled(fixedRate = 300_000) // 5 minutos
    @Transactional
    public void reconciliarImportsPendentes() {
        var timeout = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTOS);
        var travados = importacaoLogRepository.findProcessandoComTimeout(
                ImportacaoLog.Status.PROCESSANDO, timeout);

        if (travados.isEmpty()) {
            return;
        }

        log.warn("Reconciliação: {} importação(ões) travada(s) em PROCESSANDO detectada(s).", travados.size());

        for (var importacao : travados) {
            log.warn("Marcando protocolo {} como FALHA (travado há mais de {} min).",
                    importacao.getProtocoloId(), TIMEOUT_MINUTOS);
            importacao.falhar("Timeout de processamento (" + TIMEOUT_MINUTOS +
                    " min). Reenvie o arquivo CSV para tentar novamente.");
            importacaoLogRepository.save(importacao);
        }
    }
}
