package com.totvs.contaspagar.infrastructure.persistence.jpa;

import com.totvs.contaspagar.domain.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FornecedorJpaRepository extends JpaRepository<Fornecedor, UUID> {
    boolean existsByNomeIgnoreCase(String nome);
}
