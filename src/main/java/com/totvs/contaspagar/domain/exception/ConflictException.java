package com.totvs.contaspagar.domain.exception;

/**
 * Indica que a operação viola uma regra de unicidade (ex: nome já cadastrado).
 * Mapeada para HTTP 409 Conflict pelo GlobalExceptionHandler.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
