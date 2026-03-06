package com.totvs.contaspagar.domain.exception;

/**
 * Exceção lançada quando tentam registrar um usuário com username já existente.
 * Mapeada para HTTP 409 Conflict pelo GlobalExceptionHandler.
 */
public class UsuarioJaExistenteException extends ConflictException {

    public UsuarioJaExistenteException(String username) {
        super("Usuário já existente: " + username);
    }
}
