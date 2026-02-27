package com.totvs.contaspagar.infrastructure.csv;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Componente responsável por parsear o CSV de importação de contas.
 *
 * Formato esperado (com cabeçalho):
 * data_vencimento,data_pagamento,valor,descricao,fornecedor_id
 * 2024-01-15,,100.50,Conta de luz,3fa85f64-5717-4562-b3fc-2c963f66afa6
 */
@Component
public class CsvParser {

    private static final Logger log = LoggerFactory.getLogger(CsvParser.class);

    public record LinhaCSV(
            LocalDate dataVencimento,
            LocalDate dataPagamento,
            BigDecimal valor,
            String descricao,
            UUID fornecedorId
    ) {}

    public record ResultadoParsing(
            List<LinhaCSV> linhasValidas,
            List<String> erros
    ) {}

    public ResultadoParsing parsear(String csvContent) {
        var linhasValidas = new ArrayList<LinhaCSV>();
        var erros = new ArrayList<String>();

        try (var reader = new CSVReader(new StringReader(csvContent))) {
            String[] cabecalho = reader.readNext(); // pula cabeçalho
            if (cabecalho == null) {
                erros.add("Arquivo CSV vazio ou sem cabeçalho.");
                return new ResultadoParsing(linhasValidas, erros);
            }

            String[] campos;
            int numeroLinha = 1;
            while ((campos = reader.readNext()) != null) {
                numeroLinha++;
                final int linhaAtual = numeroLinha;
                final String[] camposCapturados = campos;
                parsearLinha(campos, linhaAtual)
                        .ifPresentOrElse(
                                linhasValidas::add,
                                () -> erros.add("Linha " + linhaAtual + ": dados inválidos — " + String.join(",", camposCapturados))
                        );
            }
        } catch (IOException | CsvValidationException e) {
            log.error("Erro ao parsear CSV", e);
            erros.add("Erro crítico ao processar arquivo: " + e.getMessage());
        }

        return new ResultadoParsing(linhasValidas, erros);
    }

    private Optional<LinhaCSV> parsearLinha(String[] campos, int numeroLinha) {
        if (campos.length < 5) {
            log.warn("Linha {}: apenas {} coluna(s) — esperadas 5 (data_vencimento, data_pagamento, valor, descricao, fornecedor_id). "
                    + "Se o valor usa vírgula como separador decimal (ex: 0,50), substitua por ponto (0.50).",
                    numeroLinha, campos.length);
            return Optional.empty();
        }

        // data_vencimento
        LocalDate dataVencimento;
        try {
            dataVencimento = LocalDate.parse(campos[0].trim());
        } catch (DateTimeParseException e) {
            log.warn("Linha {}: data_vencimento inválida '{}' — use o formato yyyy-MM-dd (ex: 2025-09-15)",
                    numeroLinha, campos[0].trim());
            return Optional.empty();
        }

        // data_pagamento (opcional)
        LocalDate dataPagamento = null;
        if (!campos[1].trim().isEmpty()) {
            try {
                dataPagamento = LocalDate.parse(campos[1].trim());
            } catch (DateTimeParseException e) {
                log.warn("Linha {}: data_pagamento inválida '{}' — use o formato yyyy-MM-dd ou deixe vazio",
                        numeroLinha, campos[1].trim());
                return Optional.empty();
            }
        }

        // valor
        BigDecimal valor;
        try {
            var valorStr = campos[2].trim();
            if (valorStr.contains(",")) {
                log.warn("Linha {}: valor '{}' usa vírgula como separador decimal — use ponto (ex: 0.50 em vez de 0,50)",
                        numeroLinha, valorStr);
                return Optional.empty();
            }
            valor = new BigDecimal(valorStr);
        } catch (NumberFormatException e) {
            log.warn("Linha {}: valor '{}' não é um número válido (ex: 350.00)", numeroLinha, campos[2].trim());
            return Optional.empty();
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Linha {}: valor {} deve ser maior que zero", numeroLinha, valor);
            return Optional.empty();
        }
        // NUMERIC(19,2) exige valor absoluto < 10^17
        if (valor.precision() - valor.scale() > 17) {
            log.warn("Linha {}: valor {} excede a precisão máxima permitida (17 dígitos inteiros)", numeroLinha, valor.toPlainString());
            return Optional.empty();
        }

        // descricao
        var descricao = campos[3].trim();
        if (descricao.isBlank()) {
            log.warn("Linha {}: descrição não pode ser vazia", numeroLinha);
            return Optional.empty();
        }

        // fornecedor_id
        UUID fornecedorId;
        try {
            fornecedorId = UUID.fromString(campos[4].trim());
        } catch (IllegalArgumentException e) {
            log.warn("Linha {}: fornecedor_id '{}' não é um UUID válido (ex: 586443e5-446b-4a0e-88a3-0e6393a550be)",
                    numeroLinha, campos[4].trim());
            return Optional.empty();
        }

        return Optional.of(new LinhaCSV(dataVencimento, dataPagamento, valor, descricao, fornecedorId));
    }
}
