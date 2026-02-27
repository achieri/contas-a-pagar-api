package com.totvs.contaspagar.domain;

import com.totvs.contaspagar.domain.exception.DomainException;
import com.totvs.contaspagar.domain.exception.TransicaoSituacaoInvalidaException;
import com.totvs.contaspagar.domain.model.Conta;
import com.totvs.contaspagar.domain.model.Fornecedor;
import com.totvs.contaspagar.domain.model.SituacaoConta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Entidade Conta — Invariantes de Domínio")
class ContaTest {

    private Fornecedor fornecedor;

    @BeforeEach
    void setUp() {
        fornecedor = new Fornecedor("Fornecedor Teste");
    }

    @Nested
    @DisplayName("Criação")
    class Criacao {

        @Test
        @DisplayName("Deve criar conta com situação PENDENTE por padrão")
        void deveCriarContaPendente() {
            var conta = Conta.criar(LocalDate.now().plusDays(30),
                    new BigDecimal("100.00"), "Conta teste", fornecedor);

            assertThat(conta.getSituacao()).isEqualTo(SituacaoConta.PENDENTE);
            assertThat(conta.getDataPagamento()).isNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar conta com valor zero")
        void deveLancarExcecaoValorZero() {
            assertThatThrownBy(() ->
                    Conta.criar(LocalDate.now(), BigDecimal.ZERO, "Teste", fornecedor))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("positivo");
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar conta com valor negativo")
        void deveLancarExcecaoValorNegativo() {
            assertThatThrownBy(() ->
                    Conta.criar(LocalDate.now(), new BigDecimal("-50.00"), "Teste", fornecedor))
                    .isInstanceOf(DomainException.class);
        }
    }

    @Nested
    @DisplayName("Máquina de Estados — alterarSituacao")
    class MaquinaDeEstados {

        @Test
        @DisplayName("Deve transicionar de PENDENTE para PAGO e registrar data de pagamento")
        void deveTransicionarParaPago() {
            var conta = Conta.criar(LocalDate.now(), new BigDecimal("200.00"), "Teste", fornecedor);
            var dataPgto = LocalDate.now();

            conta.alterarSituacao(SituacaoConta.PAGO, dataPgto);

            assertThat(conta.getSituacao()).isEqualTo(SituacaoConta.PAGO);
            assertThat(conta.getDataPagamento()).isEqualTo(dataPgto);
        }

        @Test
        @DisplayName("Deve usar data atual quando data de pagamento não é informada")
        void deveUsarDataAtualSemDataPagamento() {
            var conta = Conta.criar(LocalDate.now(), new BigDecimal("200.00"), "Teste", fornecedor);
            conta.alterarSituacao(SituacaoConta.PAGO, null);

            assertThat(conta.getDataPagamento()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Deve transicionar de PENDENTE para CANCELADO")
        void deveTransicionarParaCancelado() {
            var conta = Conta.criar(LocalDate.now(), new BigDecimal("200.00"), "Teste", fornecedor);
            conta.alterarSituacao(SituacaoConta.CANCELADO, null);

            assertThat(conta.getSituacao()).isEqualTo(SituacaoConta.CANCELADO);
        }

        @Test
        @DisplayName("Invariante: Conta PAGA não pode voltar a PENDENTE")
        void contaPagaNaoVoltaParaPendente() {
            var conta = Conta.criar(LocalDate.now(), new BigDecimal("200.00"), "Teste", fornecedor);
            conta.alterarSituacao(SituacaoConta.PAGO, LocalDate.now());

            assertThatThrownBy(() -> conta.alterarSituacao(SituacaoConta.PENDENTE, null))
                    .isInstanceOf(TransicaoSituacaoInvalidaException.class)
                    .hasMessageContaining("PAGO")
                    .hasMessageContaining("PENDENTE");
        }

        @Test
        @DisplayName("Invariante: Conta CANCELADA não pode ter situação alterada")
        void contaCanceladaNaoAlteraSituacao() {
            var conta = Conta.criar(LocalDate.now(), new BigDecimal("200.00"), "Teste", fornecedor);
            conta.alterarSituacao(SituacaoConta.CANCELADO, null);

            assertThatThrownBy(() -> conta.alterarSituacao(SituacaoConta.PAGO, LocalDate.now()))
                    .isInstanceOf(TransicaoSituacaoInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("Atualização")
    class Atualizacao {

        @Test
        @DisplayName("Deve lançar exceção ao tentar editar conta PAGA")
        void naoDeveEditarContaPaga() {
            var conta = Conta.criar(LocalDate.now(), new BigDecimal("100.00"), "Teste", fornecedor);
            conta.alterarSituacao(SituacaoConta.PAGO, LocalDate.now());

            assertThatThrownBy(() ->
                    conta.atualizar(LocalDate.now(), new BigDecimal("200.00"), "Novo", fornecedor))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("paga");
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar editar conta CANCELADA")
        void naoDeveEditarContaCancelada() {
            var conta = Conta.criar(LocalDate.now(), new BigDecimal("100.00"), "Teste", fornecedor);
            conta.alterarSituacao(SituacaoConta.CANCELADO, null);

            assertThatThrownBy(() ->
                    conta.atualizar(LocalDate.now(), new BigDecimal("200.00"), "Novo", fornecedor))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("cancelada");
        }
    }
}
