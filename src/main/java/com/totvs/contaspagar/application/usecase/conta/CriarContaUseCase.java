package com.totvs.contaspagar.application.usecase.conta;

import com.totvs.contaspagar.application.dto.request.ContaRequest;
import com.totvs.contaspagar.application.dto.response.ContaResponse;
import com.totvs.contaspagar.domain.exception.FornecedorNaoEncontradoException;
import com.totvs.contaspagar.domain.model.Conta;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarContaUseCase {

    private final ContaRepository contaRepository;
    private final FornecedorRepository fornecedorRepository;

    public CriarContaUseCase(ContaRepository contaRepository,
                              FornecedorRepository fornecedorRepository) {
        this.contaRepository = contaRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public ContaResponse executar(ContaRequest request) {
        // Validação de fluxo: verifica existência do fornecedor
        var fornecedor = fornecedorRepository.buscarPorId(request.fornecedorId())
                .orElseThrow(() -> new FornecedorNaoEncontradoException(request.fornecedorId()));

        // O domínio valida as invariantes (valor positivo, etc.)
        var conta = Conta.criar(
                request.dataVencimento(),
                request.valor(),
                request.descricao(),
                fornecedor
        );

        return ContaResponse.from(contaRepository.salvar(conta));
    }
}
