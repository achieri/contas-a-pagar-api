package com.totvs.contaspagar.application.usecase.fornecedor;

import com.totvs.contaspagar.domain.exception.FornecedorNaoEncontradoException;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeletarFornecedorUseCase {

    private final FornecedorRepository fornecedorRepository;

    public DeletarFornecedorUseCase(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional
    public void executar(UUID id) {
        if (!fornecedorRepository.existePorId(id)) {
            throw new FornecedorNaoEncontradoException(id);
        }
        fornecedorRepository.deletar(id);
    }
}
