package com.totvs.contaspagar.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.totvs.contaspagar.domain.model.SituacaoConta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AlterarSituacaoRequest(

        @Schema(example = "PAGO", description = "Nova situação: PENDENTE, PAGO ou CANCELADO")
        @NotNull(message = "Situação é obrigatória")
        SituacaoConta situacao,

        @Schema(example = "2025-09-10", description = "Data de pagamento no formato yyyy-MM-dd (obrigatória quando situacao = PAGO)")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataPagamento
) {}
