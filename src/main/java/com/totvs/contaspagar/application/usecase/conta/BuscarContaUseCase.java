package com.totvs.contaspagar.application.usecase.conta;

import com.totvs.contaspagar.application.dto.response.ContaResponse;
import com.totvs.contaspagar.application.dto.response.RelatorioTotalPagoResponse;
import com.totvs.contaspagar.domain.exception.ContaNaoEncontradaException;
import com.totvs.contaspagar.domain.repository.ContaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class BuscarContaUseCase {

    private final ContaRepository contaRepository;

    public BuscarContaUseCase(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorId(UUID id) {
        return contaRepository.buscarPorId(id)
                .map(ContaResponse::from)
                .orElseThrow(() -> new ContaNaoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public Page<ContaResponse> listarComFiltros(LocalDate dataVencimentoInicio,
                                                 LocalDate dataVencimentoFim,
                                                 String descricao,
                                                 Pageable pageable) {
        return contaRepository.listarComFiltros(
                dataVencimentoInicio, dataVencimentoFim, descricao, pageable
        ).map(ContaResponse::from);
    }

    @Transactional(readOnly = true)
    public RelatorioTotalPagoResponse calcularTotalPagoPorPeriodo(LocalDate inicio, LocalDate fim) {
        BigDecimal total = contaRepository.calcularTotalPagoPorPeriodo(inicio, fim);
        return new RelatorioTotalPagoResponse(inicio, fim,
                total != null ? total : BigDecimal.ZERO);
    }
}
