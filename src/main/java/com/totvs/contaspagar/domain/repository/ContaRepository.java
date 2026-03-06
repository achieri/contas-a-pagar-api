package com.totvs.contaspagar.domain.repository;

import com.totvs.contaspagar.domain.model.Conta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta (interface) do repositório de Contas.
 * Definida no domínio, implementada na infraestrutura.
 */
public interface ContaRepository {
    Conta salvar(Conta conta);
    Optional<Conta> buscarPorId(UUID id);
    Page<Conta> listarComFiltros(LocalDate dataVencimentoInicio,
                                  LocalDate dataVencimentoFim,
                                  String descricao,
                                  Pageable pageable);
    BigDecimal calcularTotalPagoPorPeriodo(LocalDate inicio, LocalDate fim);
    boolean existeContaComFornecedor(UUID fornecedorId);
    void deletar(UUID id);
}
