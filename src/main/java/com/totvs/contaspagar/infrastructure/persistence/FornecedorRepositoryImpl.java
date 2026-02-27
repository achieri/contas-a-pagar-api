package com.totvs.contaspagar.infrastructure.persistence;

import com.totvs.contaspagar.domain.model.Fornecedor;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
import com.totvs.contaspagar.infrastructure.persistence.jpa.FornecedorJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class FornecedorRepositoryImpl implements FornecedorRepository {

    private final FornecedorJpaRepository jpaRepository;

    public FornecedorRepositoryImpl(FornecedorJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override public Fornecedor salvar(Fornecedor fornecedor) { return jpaRepository.save(fornecedor); }
    @Override public Optional<Fornecedor> buscarPorId(UUID id) { return jpaRepository.findById(id); }
    @Override public Page<Fornecedor> listarTodos(Pageable pageable) { return jpaRepository.findAll(pageable); }
    @Override public void deletar(UUID id) { jpaRepository.deleteById(id); }
    @Override public boolean existePorId(UUID id) { return jpaRepository.existsById(id); }
    @Override public boolean existePorNomeIgnoreCase(String nome) { return jpaRepository.existsByNomeIgnoreCase(nome); }
}
