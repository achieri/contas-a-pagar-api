package com.totvs.contaspagar.domain.exception;

/**
 * Exceção base para invariantes e regras de domínio violadas.
 * Mapeada para HTTP 422 Unprocessable Entity pelo GlobalExceptionHandler.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
