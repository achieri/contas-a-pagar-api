package com.totvs.contaspagar.infrastructure.persistence;

import com.totvs.contaspagar.domain.model.Conta;
import com.totvs.contaspagar.domain.model.SituacaoConta;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import com.totvs.contaspagar.infrastructure.persistence.jpa.ContaJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ContaRepositoryImpl implements ContaRepository {

    private final ContaJpaRepository jpaRepository;

    public ContaRepositoryImpl(ContaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Conta salvar(Conta conta) {
        return jpaRepository.save(conta);
    }

    @Override
    public Optional<Conta> buscarPorId(UUID id) {
        return jpaRepository.findByIdWithFornecedor(id);
    }

    @Override
    public Page<Conta> listarComFiltros(LocalDate dataVencimentoInicio,
                                         LocalDate dataVencimentoFim,
                                         String descricao,
                                         Pageable pageable) {
        // A query já tem ORDER BY fixo; strip do sort do Pageable evita conflito com valores inválidos (ex: sort=string do Swagger)
        Pageable semSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return jpaRepository.findByFiltros(dataVencimentoInicio, dataVencimentoFim, descricao, semSort);
    }

    @Override
    public BigDecimal calcularTotalPagoPorPeriodo(LocalDate inicio, LocalDate fim) {
        return jpaRepository.calcularTotalPagoPorPeriodo(SituacaoConta.PAGO, inicio, fim);
    }

    @Override
    public void deletar(UUID id) {
        jpaRepository.deleteById(id);
    }
}
