package com.totvs.contaspagar.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RelatorioTotalPagoResponse(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate periodoInicio,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate periodoFim,
        BigDecimal totalPago
) {}
