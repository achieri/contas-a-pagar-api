package com.totvs.contaspagar.api.controller;

import com.totvs.contaspagar.application.dto.request.AlterarSituacaoRequest;
import com.totvs.contaspagar.application.dto.request.ContaRequest;
import com.totvs.contaspagar.application.dto.response.ContaResponse;
import com.totvs.contaspagar.application.dto.response.RelatorioTotalPagoResponse;
import com.totvs.contaspagar.application.usecase.conta.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/contas")
@Tag(name = "Contas a Pagar")
@SecurityRequirement(name = "bearerAuth")
public class ContaController {

    private final CriarContaUseCase criarUseCase;
    private final AtualizarContaUseCase atualizarUseCase;
    private final DeletarContaUseCase deletarUseCase;
    private final BuscarContaUseCase buscarUseCase;
    private final AlterarSituacaoContaUseCase alterarSituacaoUseCase;

    public ContaController(CriarContaUseCase criarUseCase,
                            AtualizarContaUseCase atualizarUseCase,
                            DeletarContaUseCase deletarUseCase,
                            BuscarContaUseCase buscarUseCase,
                            AlterarSituacaoContaUseCase alterarSituacaoUseCase) {
        this.criarUseCase = criarUseCase;
        this.atualizarUseCase = atualizarUseCase;
        this.deletarUseCase = deletarUseCase;
        this.buscarUseCase = buscarUseCase;
        this.alterarSituacaoUseCase = alterarSituacaoUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar conta a pagar")
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        var response = criarUseCase.executar(request);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar contas com filtros e paginação")
    public ResponseEntity<Page<ContaResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimentoInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataVencimentoFim,
            @RequestParam(required = false) String descricao,
            @PageableDefault(size = 20, sort = "dataVencimento") Pageable pageable) {
        return ResponseEntity.ok(
                buscarUseCase.listarComFiltros(dataVencimentoInicio, dataVencimentoFim, descricao, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(buscarUseCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta a pagar")
    public ResponseEntity<ContaResponse> atualizar(@PathVariable UUID id,
                                                    @Valid @RequestBody ContaRequest request) {
        return ResponseEntity.ok(atualizarUseCase.executar(id, request));
    }

    @PatchMapping("/{id}/situacao")
    @Operation(summary = "Alterar situação da conta (PENDENTE → PAGO / CANCELADO)")
    public ResponseEntity<ContaResponse> alterarSituacao(@PathVariable UUID id,
                                                          @Valid @RequestBody AlterarSituacaoRequest request) {
        return ResponseEntity.ok(alterarSituacaoUseCase.executar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar conta a pagar")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        deletarUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorio/total-pago")
    @Operation(summary = "Relatório de valor total pago por período")
    public ResponseEntity<RelatorioTotalPagoResponse> totalPago(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(buscarUseCase.calcularTotalPagoPorPeriodo(inicio, fim));
    }
}
