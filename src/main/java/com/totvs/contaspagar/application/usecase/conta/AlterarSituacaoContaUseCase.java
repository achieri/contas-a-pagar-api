package com.totvs.contaspagar.application.usecase.conta;

import com.totvs.contaspagar.application.dto.request.AlterarSituacaoRequest;
import com.totvs.contaspagar.application.dto.response.ContaResponse;
import com.totvs.contaspagar.domain.exception.ContaNaoEncontradaException;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AlterarSituacaoContaUseCase {

    private final ContaRepository contaRepository;

    public AlterarSituacaoContaUseCase(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Transactional
    public ContaResponse executar(UUID id, AlterarSituacaoRequest request) {
        var conta = contaRepository.buscarPorId(id)
                .orElseThrow(() -> new ContaNaoEncontradaException(id));

        // A lógica de transição de estado vive no domínio
        conta.alterarSituacao(request.situacao(), request.dataPagamento());

        return ContaResponse.from(contaRepository.salvar(conta));
    }
}
