package com.totvs.contaspagar.application.usecase.fornecedor;

import com.totvs.contaspagar.application.dto.response.FornecedorResponse;
import com.totvs.contaspagar.domain.exception.FornecedorNaoEncontradoException;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BuscarFornecedorUseCase {

    private final FornecedorRepository fornecedorRepository;

    public BuscarFornecedorUseCase(FornecedorRepository fornecedorRepository) {
        this.fornecedorRepository = fornecedorRepository;
    }

    @Transactional(readOnly = true)
    public FornecedorResponse buscarPorId(UUID id) {
        return fornecedorRepository.buscarPorId(id)
                .map(FornecedorResponse::from)
                .orElseThrow(() -> new FornecedorNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Page<FornecedorResponse> listarTodos(Pageable pageable) {
        return fornecedorRepository.listarTodos(pageable)
                .map(FornecedorResponse::from);
    }
}
