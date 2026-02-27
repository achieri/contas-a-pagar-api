package com.totvs.contaspagar.domain.repository;

import com.totvs.contaspagar.domain.model.Fornecedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta (interface) do repositório de Fornecedores.
 * Definida no domínio, implementada na infraestrutura.
 */
public interface FornecedorRepository {
    Fornecedor salvar(Fornecedor fornecedor);
    Optional<Fornecedor> buscarPorId(UUID id);
    Page<Fornecedor> listarTodos(Pageable pageable);
    void deletar(UUID id);
    boolean existePorId(UUID id);
    boolean existePorNomeIgnoreCase(String nome);
}
