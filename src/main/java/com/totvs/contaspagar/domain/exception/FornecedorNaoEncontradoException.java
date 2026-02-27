package com.totvs.contaspagar.domain.exception;

import java.util.UUID;

public class FornecedorNaoEncontradoException extends RuntimeException {

    public FornecedorNaoEncontradoException(UUID id) {
        super("Fornecedor não encontrado com id: " + id);
    }

    public FornecedorNaoEncontradoException(String nome) {
        super("Fornecedor não encontrado com nome: " + nome);
    }
}
