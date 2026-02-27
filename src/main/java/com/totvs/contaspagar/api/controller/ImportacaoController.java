package com.totvs.contaspagar.api.controller;

import com.totvs.contaspagar.application.dto.response.ImportacaoProtocoloResponse;
import com.totvs.contaspagar.infrastructure.csv.CsvParser;
import com.totvs.contaspagar.infrastructure.messaging.CsvImportMessage;
import com.totvs.contaspagar.infrastructure.messaging.CsvImportPublisher;
import com.totvs.contaspagar.infrastructure.persistence.entity.ImportacaoLog;
import com.totvs.contaspagar.infrastructure.persistence.jpa.ImportacaoLogJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/importacao")
@Tag(name = "Importação CSV")
@SecurityRequirement(name = "bearerAuth")
public class ImportacaoController {

    private final CsvImportPublisher publisher;
    private final ImportacaoLogJpaRepository importacaoLogRepository;
    private final CsvParser csvParser;

    public ImportacaoController(CsvImportPublisher publisher,
                                 ImportacaoLogJpaRepository importacaoLogRepository,
                                 CsvParser csvParser) {
        this.publisher = publisher;
        this.importacaoLogRepository = importacaoLogRepository;
        this.csvParser = csvParser;
    }

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Upload de CSV para importação assíncrona de contas em lote",
        description = "Recebe o arquivo .csv, publica no RabbitMQ e retorna um protocolo de rastreamento. " +
                      "Use o protocoloId retornado para consultar o status do processamento."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Arquivo aceito e enfileirado para processamento"),
        @ApiResponse(responseCode = "400", description = "Arquivo ausente, extensão inválida, vazio ou sem linhas válidas",
                     content = @Content(mediaType = "application/problem+json")),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido")
    })
    public ResponseEntity<?> importarCsv(
            @Parameter(
                description = "Arquivo CSV com contas a importar (colunas: data_vencimento, data_pagamento, valor, descricao, fornecedor_id)",
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                   schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {

        if (arquivo.isEmpty()) {
            var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            pd.setTitle("Arquivo vazio");
            pd.setDetail("O arquivo CSV enviado está vazio.");
            pd.setType(URI.create("urn:contas-pagar:empty-file"));
            return ResponseEntity.badRequest().body(pd);
        }

        if (!arquivo.getOriginalFilename().endsWith(".csv")) {
            var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            pd.setTitle("Formato inválido");
            pd.setDetail("Apenas arquivos .csv são aceitos.");
            pd.setType(URI.create("urn:contas-pagar:invalid-format"));
            return ResponseEntity.badRequest().body(pd);
        }

        var csvContent = new String(arquivo.getBytes(), StandardCharsets.UTF_8);

        // Valida conteúdo antes de enfileirar
        var resultado = csvParser.parsear(csvContent);
        if (resultado.linhasValidas().isEmpty() && resultado.erros().isEmpty()) {
            var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            pd.setTitle("Arquivo sem dados");
            pd.setDetail("O arquivo CSV contém apenas o cabeçalho, sem dados para importar.");
            pd.setType(URI.create("urn:contas-pagar:empty-file"));
            return ResponseEntity.badRequest().body(pd);
        }
        if (resultado.linhasValidas().isEmpty()) {
            var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            pd.setTitle("Nenhuma linha válida");
            pd.setDetail("Todas as linhas do CSV contêm erros e nenhuma pode ser importada. Erros: "
                    + String.join(" | ", resultado.erros()));
            pd.setType(URI.create("urn:contas-pagar:invalid-csv"));
            return ResponseEntity.badRequest().body(pd);
        }

        var protocoloId = UUID.randomUUID();

        // Persiste o log de importação antes de publicar
        var logEntry = ImportacaoLog.criar(protocoloId);
        importacaoLogRepository.save(logEntry);

        // Publica a mensagem — processamento ocorre em background no Consumer
        publisher.publicar(new CsvImportMessage(protocoloId, csvContent, LocalDateTime.now()));

        return ResponseEntity.accepted().body(new ImportacaoProtocoloResponse(
                protocoloId,
                "AGUARDANDO",
                "Arquivo recebido e enfileirado para processamento.",
                LocalDateTime.now()
        ));
    }

    @GetMapping("/csv/{protocoloId}")
    @Operation(summary = "Consultar status de importação por protocolo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
        @ApiResponse(responseCode = "404", description = "Protocolo não encontrado",
                     content = @Content),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou inválido")
    })
    public ResponseEntity<ImportacaoProtocoloResponse> consultarStatus(
            @Parameter(description = "UUID do protocolo retornado no upload", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID protocoloId) {

        return importacaoLogRepository.findByProtocoloId(protocoloId)
                .map(log -> ResponseEntity.ok(new ImportacaoProtocoloResponse(
                        log.getProtocoloId(),
                        log.getStatus().name(),
                        formatarMensagem(log),
                        log.getSolicitadoEm()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String formatarMensagem(ImportacaoLog log) {
        return switch (log.getStatus()) {
            case AGUARDANDO -> "Aguardando processamento.";
            case PROCESSANDO -> "Processando " + log.getTotalLinhas() + " registros...";
            case CONCLUIDO -> "Concluído. " + log.getLinhasProcessadas() + " registros importados.";
            case CONCLUIDO_COM_ERROS -> {
                String base = "Concluído com erros. Importados: " + log.getLinhasProcessadas()
                        + " | Erros: " + log.getLinhasComErro();
                yield log.getMensagemErro() != null
                        ? base + " — Detalhes: " + log.getMensagemErro()
                        : base;
            }
            case FALHA -> "Falha: " + log.getMensagemErro();
        };
    }
}
