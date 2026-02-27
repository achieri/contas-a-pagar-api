package com.totvs.contaspagar.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

        @Schema(example = "novo_usuario", description = "Username desejado (3–100 caracteres, único)")
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 3, max = 100, message = "Username deve ter entre 3 e 100 caracteres")
        String username,

        @Schema(example = "senha123", description = "Senha (mínimo 6 caracteres)")
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String password
) {}
