package com.totvs.contaspagar.domain.model;

import com.totvs.contaspagar.domain.exception.DomainException;
import com.totvs.contaspagar.domain.exception.TransicaoSituacaoInvalidaException;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade raiz do agregado Conta.
 * Contém as invariantes de domínio e a máquina de estados da situação.
 */
@Entity
@Table(name = "conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "situacao", nullable = false, columnDefinition = "situacao_conta")
    private SituacaoConta situacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Conta() {}

    /**
     * Factory method — garante que a conta nasce sempre no estado PENDENTE
     * e com invariantes validadas.
     */
    public static Conta criar(LocalDate dataVencimento, BigDecimal valor,
                               String descricao, Fornecedor fornecedor) {
        Conta conta = new Conta();
        conta.dataVencimento = dataVencimento;
        conta.descricao = descricao;
        conta.fornecedor = fornecedor;
        conta.situacao = SituacaoConta.PENDENTE;
        conta.criadoEm = LocalDateTime.now();
        conta.atualizadoEm = LocalDateTime.now();
        conta.setValor(valor);
        return conta;
    }

    /**
     * Atualiza os dados da conta. Somente permitido para contas PENDENTES.
     */
    public void atualizar(LocalDate dataVencimento, BigDecimal valor,
                          String descricao, Fornecedor fornecedor) {
        if (this.situacao == SituacaoConta.PAGO) {
            throw new DomainException("Conta já paga não pode ser editada.");
        }
        if (this.situacao == SituacaoConta.CANCELADO) {
            throw new DomainException("Conta cancelada não pode ser editada.");
        }
        this.dataVencimento = dataVencimento;
        this.descricao = descricao;
        this.fornecedor = fornecedor;
        this.atualizadoEm = LocalDateTime.now();
        setValor(valor);
    }

    /**
     * Máquina de estados — valida e aplica a transição de situação.
     * Regras:
     *   - Conta PAGO não pode retornar a PENDENTE (invariante de domínio)
     *   - Conta CANCELADO não pode ter situação alterada
     *   - Ao marcar PAGO, registra a data de pagamento se não informada
     */
    public void alterarSituacao(SituacaoConta novaSituacao, LocalDate dataPagamento) {
        if (this.situacao == SituacaoConta.CANCELADO) {
            throw new TransicaoSituacaoInvalidaException(this.situacao, novaSituacao);
        }
        if (this.situacao == SituacaoConta.PAGO && novaSituacao == SituacaoConta.PENDENTE) {
            throw new TransicaoSituacaoInvalidaException(this.situacao, novaSituacao);
        }
        this.situacao = novaSituacao;
        if (novaSituacao == SituacaoConta.PAGO) {
            LocalDate hoje = LocalDate.now();
            if (dataPagamento != null && dataPagamento.isAfter(hoje)) {
                throw new DomainException("Data de pagamento não pode ser no futuro. Informada: " + dataPagamento);
            }
            this.dataPagamento = (dataPagamento != null) ? dataPagamento : hoje;
        }
        this.atualizadoEm = LocalDateTime.now();
    }

    private void setValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("Valor da conta deve ser positivo.");
        }
        this.valor = valor;
    }

    @PreUpdate
    protected void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public LocalDate getDataPagamento() { return dataPagamento; }
    public BigDecimal getValor() { return valor; }
    public String getDescricao() { return descricao; }
    public SituacaoConta getSituacao() { return situacao; }
    public Fornecedor getFornecedor() { return fornecedor; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
