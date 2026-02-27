package com.totvs.contaspagar.infrastructure.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mensagem publicada no RabbitMQ para processamento assíncrono do CSV.
 * Carrega o conteúdo bruto do arquivo e o protocolo para rastreamento.
 */
public record CsvImportMessage(
        UUID protocoloId,
        String csvContent,
        LocalDateTime solicitadoEm
) {}
