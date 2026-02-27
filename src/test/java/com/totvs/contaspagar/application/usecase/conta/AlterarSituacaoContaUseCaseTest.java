package com.totvs.contaspagar.application.usecase.conta;

import com.totvs.contaspagar.application.dto.request.AlterarSituacaoRequest;
import com.totvs.contaspagar.domain.exception.ContaNaoEncontradaException;
import com.totvs.contaspagar.domain.exception.TransicaoSituacaoInvalidaException;
import com.totvs.contaspagar.domain.model.Conta;
import com.totvs.contaspagar.domain.model.Fornecedor;
import com.totvs.contaspagar.domain.model.SituacaoConta;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AlterarSituacaoContaUseCase")
class AlterarSituacaoContaUseCaseTest {

    @Mock private ContaRepository contaRepository;
    @InjectMocks private AlterarSituacaoContaUseCase useCase;

    private Conta criarContaPendente() {
        return Conta.criar(LocalDate.now().plusDays(10),
                new BigDecimal("300.00"), "Fatura teste", new Fornecedor("Fornecedor Y"));
    }

    @Test
    @DisplayName("Deve alterar situação para PAGO com sucesso")
    void deveAlterarParaPago() {
        var id = UUID.randomUUID();
        var conta = criarContaPendente();
        var request = new AlterarSituacaoRequest(SituacaoConta.PAGO, LocalDate.now());

        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));
        when(contaRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = useCase.executar(id, request);

        assertThat(response.situacao()).isEqualTo(SituacaoConta.PAGO);
        assertThat(response.dataPagamento()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Deve lançar ContaNaoEncontradaException quando conta não existe")
    void deveLancarExcecaoContaNaoEncontrada() {
        var id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(id,
                new AlterarSituacaoRequest(SituacaoConta.PAGO, LocalDate.now())))
                .isInstanceOf(ContaNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve propagar TransicaoSituacaoInvalidaException do domínio")
    void devePropararExcecaoDominio() {
        var id = UUID.randomUUID();
        var conta = criarContaPendente();
        conta.alterarSituacao(SituacaoConta.PAGO, LocalDate.now());

        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> useCase.executar(id,
                new AlterarSituacaoRequest(SituacaoConta.PENDENTE, null)))
                .isInstanceOf(TransicaoSituacaoInvalidaException.class);
    }
}
