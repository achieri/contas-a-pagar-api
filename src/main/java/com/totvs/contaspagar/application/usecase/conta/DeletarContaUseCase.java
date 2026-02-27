package com.totvs.contaspagar.application.usecase.conta;

import com.totvs.contaspagar.domain.exception.ContaNaoEncontradaException;
import com.totvs.contaspagar.domain.exception.DomainException;
import com.totvs.contaspagar.domain.model.SituacaoConta;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeletarContaUseCase {

    private final ContaRepository contaRepository;

    public DeletarContaUseCase(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Transactional
    public void executar(UUID id) {
        var conta = contaRepository.buscarPorId(id)
                .orElseThrow(() -> new ContaNaoEncontradaException(id));

        if (conta.getSituacao() == SituacaoConta.PAGO) {
            throw new DomainException("Conta já paga não pode ser excluída.");
        }

        contaRepository.deletar(id);
    }
}
