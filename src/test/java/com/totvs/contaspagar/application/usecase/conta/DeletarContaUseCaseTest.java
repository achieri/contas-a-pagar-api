package com.totvs.contaspagar.application.usecase.conta;

import com.totvs.contaspagar.domain.exception.ContaNaoEncontradaException;
import com.totvs.contaspagar.domain.exception.DomainException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeletarContaUseCase")
class DeletarContaUseCaseTest {

    @Mock private ContaRepository contaRepository;
    @InjectMocks private DeletarContaUseCase useCase;

    @Test
    @DisplayName("Deve deletar conta PENDENTE com sucesso")
    void deveDeletarContaPendente() {
        var id = UUID.randomUUID();
        var conta = Conta.criar(LocalDate.now(), new BigDecimal("100.00"), "Teste",
                new Fornecedor("F"));
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        useCase.executar(id);

        verify(contaRepository).deletar(id);
    }

    @Test
    @DisplayName("Não deve deletar conta PAGA")
    void naoDeveDeletarContaPaga() {
        var id = UUID.randomUUID();
        var conta = Conta.criar(LocalDate.now(), new BigDecimal("100.00"), "Teste",
                new Fornecedor("F"));
        conta.alterarSituacao(SituacaoConta.PAGO, LocalDate.now());
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> useCase.executar(id))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("paga");

        verify(contaRepository, never()).deletar(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não existe")
    void deveLancarExcecaoContaInexistente() {
        var id = UUID.randomUUID();
        when(contaRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(id))
                .isInstanceOf(ContaNaoEncontradaException.class);
    }
}
