package com.totvs.contaspagar.application.usecase.conta;

import com.totvs.contaspagar.application.dto.request.ContaRequest;
import com.totvs.contaspagar.domain.exception.FornecedorNaoEncontradoException;
import com.totvs.contaspagar.domain.model.Conta;
import com.totvs.contaspagar.domain.model.Fornecedor;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
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
@DisplayName("CriarContaUseCase")
class CriarContaUseCaseTest {

    @Mock private ContaRepository contaRepository;
    @Mock private FornecedorRepository fornecedorRepository;

    @InjectMocks
    private CriarContaUseCase useCase;

    @Test
    @DisplayName("Deve criar conta com sucesso quando fornecedor existe")
    void deveCriarContaComSucesso() {
        var fornecedorId = UUID.randomUUID();
        var fornecedor = new Fornecedor("Fornecedor X");
        var request = new ContaRequest(
                LocalDate.now().plusDays(30),
                null,
                new BigDecimal("500.00"),
                "Conta de energia",
                fornecedorId
        );

        when(fornecedorRepository.buscarPorId(fornecedorId)).thenReturn(Optional.of(fornecedor));
        when(contaRepository.salvar(any(Conta.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = useCase.executar(request);

        assertThat(response).isNotNull();
        assertThat(response.descricao()).isEqualTo("Conta de energia");
        assertThat(response.valor()).isEqualByComparingTo(new BigDecimal("500.00"));
        verify(contaRepository, times(1)).salvar(any(Conta.class));
    }

    @Test
    @DisplayName("Deve lançar FornecedorNaoEncontradoException quando fornecedor não existe")
    void deveLancarExcecaoFornecedorNaoEncontrado() {
        var fornecedorId = UUID.randomUUID();
        var request = new ContaRequest(
                LocalDate.now(),
                null,
                new BigDecimal("100.00"),
                "Teste",
                fornecedorId
        );

        when(fornecedorRepository.buscarPorId(fornecedorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.executar(request))
                .isInstanceOf(FornecedorNaoEncontradoException.class);

        verify(contaRepository, never()).salvar(any());
    }
}
