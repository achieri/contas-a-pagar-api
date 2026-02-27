package com.totvs.contaspagar.application.usecase.conta;

import com.totvs.contaspagar.application.dto.request.ContaRequest;
import com.totvs.contaspagar.application.dto.response.ContaResponse;
import com.totvs.contaspagar.domain.exception.ContaNaoEncontradaException;
import com.totvs.contaspagar.domain.exception.FornecedorNaoEncontradoException;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AtualizarContaUseCase {

    private final ContaRepository contaRepository;
    private final FornecedorRepository fornecedorRepository;

    public AtualizarContaUseCase(ContaRepository contaRepository,
                                  FornecedorRepository fornecedorRepository) {
        this.contaRepository = contaRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public ContaResponse executar(UUID id, ContaRequest request) {
        var conta = contaRepository.buscarPorId(id)
                .orElseThrow(() -> new ContaNaoEncontradaException(id));

        var fornecedor = fornecedorRepository.buscarPorId(request.fornecedorId())
                .orElseThrow(() -> new FornecedorNaoEncontradoException(request.fornecedorId()));

        // O domínio valida invariantes e restrições de estado
        conta.atualizar(request.dataVencimento(), request.valor(), request.descricao(), fornecedor);

        return ContaResponse.from(contaRepository.salvar(conta));
    }
}
