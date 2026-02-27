package com.totvs.contaspagar.application.usecase.fornecedor;

import com.totvs.contaspagar.application.dto.request.FornecedorRequest;
import com.totvs.contaspagar.application.dto.response.FornecedorResponse;
import com.totvs.contaspagar.domain.exception.ConflictException;
import com.totvs.contaspagar.domain.exception.FornecedorNaoEncontradoException;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AtualizarFornecedorUseCase {

    private final FornecedorRepository fornecedorRepository;

    public AtualizarFornecedorUseCase(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public FornecedorResponse executar(UUID id, FornecedorRequest request) {
        var fornecedor = fornecedorRepository.buscarPorId(id)
                .orElseThrow(() -> new FornecedorNaoEncontradoException(id));
        if (!fornecedor.getNome().equalsIgnoreCase(request.nome())
                && fornecedorRepository.existePorNomeIgnoreCase(request.nome())) {
            throw new ConflictException("Já existe um fornecedor com o nome '" + request.nome() + "'.");
        }
        fornecedor.atualizarNome(request.nome());
        return FornecedorResponse.from(fornecedorRepository.salvar(fornecedor));
    }
}
