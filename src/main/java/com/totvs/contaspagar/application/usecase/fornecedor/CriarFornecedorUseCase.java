package com.totvs.contaspagar.application.usecase.fornecedor;

import com.totvs.contaspagar.application.dto.request.FornecedorRequest;
import com.totvs.contaspagar.application.dto.response.FornecedorResponse;
import com.totvs.contaspagar.domain.exception.ConflictException;
import com.totvs.contaspagar.domain.model.Fornecedor;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CriarFornecedorUseCase {

    private final FornecedorRepository fornecedorRepository;

    public CriarFornecedorUseCase(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public FornecedorResponse executar(FornecedorRequest request) {
        if (fornecedorRepository.existePorNomeIgnoreCase(request.nome())) {
            throw new ConflictException("Já existe um fornecedor com o nome '" + request.nome() + "'.");
        }
        var fornecedor = new Fornecedor(request.nome());
        return FornecedorResponse.from(fornecedorRepository.salvar(fornecedor));
    }
}
