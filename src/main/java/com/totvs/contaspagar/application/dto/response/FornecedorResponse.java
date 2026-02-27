package com.totvs.contaspagar.application.dto.response;

import com.totvs.contaspagar.domain.model.Fornecedor;

import java.time.LocalDateTime;
import java.util.UUID;

public record FornecedorResponse(
        UUID id,
        String nome,
        LocalDateTime criadoEm
) {
    public static FornecedorResponse from(Fornecedor f) {
        return new FornecedorResponse(f.getId(), f.getNome(), f.getCriadoEm());
    }
}
