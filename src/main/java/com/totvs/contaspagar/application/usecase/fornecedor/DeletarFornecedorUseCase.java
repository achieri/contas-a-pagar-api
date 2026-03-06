package com.totvs.contaspagar.application.usecase.fornecedor;

import com.totvs.contaspagar.domain.exception.DomainException;
import com.totvs.contaspagar.domain.exception.FornecedorNaoEncontradoException;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import com.totvs.contaspagar.domain.repository.FornecedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeletarFornecedorUseCase {

    private final FornecedorRepository fornecedorRepository;
    private final ContaRepository contaRepository;

    public DeletarFornecedorUseCase(FornecedorRepository fornecedorRepository,
                                     ContaRepository contaRepository) {
        this.fornecedorRepository = fornecedorRepository;
        this.contaRepository = contaRepository;
    }

    @Transactional
    public void executar(UUID id) {
        if (!fornecedorRepository.existePorId(id)) {
            throw new FornecedorNaoEncontradoException(id);
        }
        if (contaRepository.existeContaComFornecedor(id)) {
            throw new DomainException("Fornecedor não pode ser deletado pois possui contas vinculadas.");
        }
        fornecedorRepository.deletar(id);
    }
}
