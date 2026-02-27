package com.totvs.contaspagar.application.dto.response;

import com.totvs.contaspagar.domain.model.Conta;
import com.totvs.contaspagar.domain.model.SituacaoConta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ContaResponse(
        UUID id,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        BigDecimal valor,
        String descricao,
        SituacaoConta situacao,
        FornecedorResponse fornecedor,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static ContaResponse from(Conta c) {
        return new ContaResponse(
                c.getId(),
                c.getDataVencimento(),
                c.getDataPagamento(),
                c.getValor(),
                c.getDescricao(),
                c.getSituacao(),
                FornecedorResponse.from(c.getFornecedor()),
                c.getCriadoEm(),
                c.getAtualizadoEm()
        );
    }
}
