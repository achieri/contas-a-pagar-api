package com.totvs.contaspagar.application.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ImportacaoProtocoloResponse(
        UUID protocoloId,
        String status,
        String mensagem,
        LocalDateTime solicitadoEm
) {}
