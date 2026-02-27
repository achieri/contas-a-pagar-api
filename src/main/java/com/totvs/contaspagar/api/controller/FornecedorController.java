package com.totvs.contaspagar.api.controller;

import com.totvs.contaspagar.application.dto.request.FornecedorRequest;
import com.totvs.contaspagar.application.dto.response.FornecedorResponse;
import com.totvs.contaspagar.application.usecase.fornecedor.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/fornecedores")
@Tag(name = "Fornecedores")
@SecurityRequirement(name = "bearerAuth")
public class FornecedorController {

    private final CriarFornecedorUseCase criarUseCase;
    private final AtualizarFornecedorUseCase atualizarUseCase;
    private final BuscarFornecedorUseCase buscarUseCase;
    private final DeletarFornecedorUseCase deletarUseCase;

    public FornecedorController(CriarFornecedorUseCase criarUseCase,
                                 AtualizarFornecedorUseCase atualizarUseCase,
                                 BuscarFornecedorUseCase buscarUseCase,
                                 DeletarFornecedorUseCase deletarUseCase) {
        this.criarUseCase = criarUseCase;
        this.atualizarUseCase = atualizarUseCase;
        this.buscarUseCase = buscarUseCase;
        this.deletarUseCase = deletarUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar fornecedor")
    public ResponseEntity<FornecedorResponse> criar(@Valid @RequestBody FornecedorRequest request) {
        var response = criarUseCase.executar(request);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar fornecedores paginados")
    public ResponseEntity<Page<FornecedorResponse>> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(buscarUseCase.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar fornecedor por ID")
    public ResponseEntity<FornecedorResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(buscarUseCase.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fornecedor")
    public ResponseEntity<FornecedorResponse> atualizar(@PathVariable UUID id,
                                                         @Valid @RequestBody FornecedorRequest request) {
        return ResponseEntity.ok(atualizarUseCase.executar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar fornecedor")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        deletarUseCase.executar(id);
        return ResponseEntity.noContent().build();
    }
}
