package com.totvs.contaspagar.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de entrada para criação/atualização de Conta.
 * Validações de contrato (camada de apresentação) via Bean Validation.
 */
public record ContaRequest(

        @Schema(example = "2025-09-15", description = "Data de vencimento no formato yyyy-MM-dd")
        @NotNull(message = "Data de vencimento é obrigatória")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataVencimento,

        @Schema(example = "2025-09-10", description = "Data de pagamento no formato yyyy-MM-dd (opcional)")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataPagamento,

        @Schema(example = "350.00", description = "Valor da conta (maior que zero)")
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        BigDecimal valor,

        @Schema(example = "Conta de energia elétrica", description = "Descrição da conta (máx. 500 caracteres)")
        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
        String descricao,

        @Schema(example = "586443e5-446b-4a0e-88a3-0e6393a550be", description = "UUID do fornecedor (criado pela migração V6)")
        @NotNull(message = "Fornecedor é obrigatório")
        UUID fornecedorId
) {}
