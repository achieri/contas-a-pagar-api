package com.totvs.contaspagar.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(example = "admin", description = "Username cadastrado")
        @NotBlank(message = "Username é obrigatório")
        String username,

        @Schema(example = "admin123", description = "Senha do usuário")
        @NotBlank(message = "Senha é obrigatória")
        String password
) {}
