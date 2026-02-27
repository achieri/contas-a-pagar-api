package com.totvs.contaspagar.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de Gestão de Contas a Pagar",
                version = "1.0.0",
                description = "Desafio Técnico TOTVS — API REST para gestão de contas a pagar com " +
                              "importação assíncrona via RabbitMQ, segurança JWT e arquitetura DDD."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {}
