package com.totvs.contaspagar.infrastructure.persistence.jpa;

import com.totvs.contaspagar.domain.model.Conta;
import com.totvs.contaspagar.domain.model.SituacaoConta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ContaJpaRepository extends JpaRepository<Conta, UUID> {

    /**
     * Busca por ID com fornecedor carregado em um único JOIN — previne N+1.
     */
    @Query("SELECT c FROM Conta c LEFT JOIN FETCH c.fornecedor WHERE c.id = :id")
    Optional<Conta> findByIdWithFornecedor(@Param("id") UUID id);

    /**
     * Listagem paginada com filtros opcionais.
     * Usa @EntityGraph para carregar o fornecedor em batch (evita N+1 na paginação).
     * O countQuery separado é necessário para paginação correta com JOIN FETCH.
     */
    @EntityGraph(attributePaths = {"fornecedor"})
    @Query(value = """
            SELECT c FROM Conta c WHERE
            (cast(:dataVencimentoInicio as LocalDate) IS NULL OR c.dataVencimento >= cast(:dataVencimentoInicio as LocalDate)) AND
            (cast(:dataVencimentoFim    as LocalDate) IS NULL OR c.dataVencimento <= cast(:dataVencimentoFim    as LocalDate)) AND
            (cast(:descricao as String) IS NULL OR LOWER(c.descricao) LIKE LOWER(CONCAT('%', cast(:descricao as String), '%')))
            ORDER BY c.dataVencimento ASC
            """,
           countQuery = """
            SELECT COUNT(c) FROM Conta c WHERE
            (cast(:dataVencimentoInicio as LocalDate) IS NULL OR c.dataVencimento >= cast(:dataVencimentoInicio as LocalDate)) AND
            (cast(:dataVencimentoFim    as LocalDate) IS NULL OR c.dataVencimento <= cast(:dataVencimentoFim    as LocalDate)) AND
            (cast(:descricao as String) IS NULL OR LOWER(c.descricao) LIKE LOWER(CONCAT('%', cast(:descricao as String), '%')))
            """)
    Page<Conta> findByFiltros(
            @Param("dataVencimentoInicio") LocalDate dataVencimentoInicio,
            @Param("dataVencimentoFim") LocalDate dataVencimentoFim,
            @Param("descricao") String descricao,
            Pageable pageable
    );

    /**
     * Relatório: soma o valor de contas PAGAS no período informado.
     */
    @Query("""
            SELECT COALESCE(SUM(c.valor), 0) FROM Conta c
            WHERE c.situacao = :situacao
            AND c.dataPagamento >= :inicio
            AND c.dataPagamento <= :fim
            """)
    BigDecimal calcularTotalPagoPorPeriodo(
            @Param("situacao") SituacaoConta situacao,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim
    );
}
