package com.totvs.contaspagar.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FornecedorRequest(

        @Schema(example = "Energia SA", description = "Nome do fornecedor (único, máx. 255 caracteres)")
        @NotBlank(message = "Nome do fornecedor é obrigatório")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome
) {}
