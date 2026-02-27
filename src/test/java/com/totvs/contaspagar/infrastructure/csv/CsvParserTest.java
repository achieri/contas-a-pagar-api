package com.totvs.contaspagar.infrastructure.csv;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CsvParser")
class CsvParserTest {

    private final CsvParser parser = new CsvParser();

    @Test
    @DisplayName("Deve parsear CSV válido corretamente")
    void deveParsearCsvValido() {
        var csv = """
                data_vencimento,data_pagamento,valor,descricao,fornecedor_id
                2024-06-30,,500.00,Conta de luz,3fa85f64-5717-4562-b3fc-2c963f66afa6
                2024-07-15,2024-07-10,200.50,Internet,3fa85f64-5717-4562-b3fc-2c963f66afa6
                """;

        var resultado = parser.parsear(csv);

        assertThat(resultado.linhasValidas()).hasSize(2);
        assertThat(resultado.erros()).isEmpty();

        var primeira = resultado.linhasValidas().get(0);
        assertThat(primeira.dataVencimento()).isEqualTo(LocalDate.of(2024, 6, 30));
        assertThat(primeira.valor()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(primeira.descricao()).isEqualTo("Conta de luz");
        assertThat(primeira.dataPagamento()).isNull();

        var segunda = resultado.linhasValidas().get(1);
        assertThat(segunda.dataPagamento()).isEqualTo(LocalDate.of(2024, 7, 10));
    }

    @Test
    @DisplayName("Deve rejeitar linha com valor negativo e manter as válidas")
    void deveRejeitarLinhaComValorNegativo() {
        var csv = """
                data_vencimento,data_pagamento,valor,descricao,fornecedor_id
                2024-06-30,,-100.00,Conta inválida,3fa85f64-5717-4562-b3fc-2c963f66afa6
                2024-07-15,,300.00,Conta válida,3fa85f64-5717-4562-b3fc-2c963f66afa6
                """;

        var resultado = parser.parsear(csv);

        assertThat(resultado.linhasValidas()).hasSize(1);
        assertThat(resultado.erros()).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar erro para CSV vazio")
    void deveRetornarErroParaCsvVazio() {
        var resultado = parser.parsear("");
        assertThat(resultado.erros()).isNotEmpty();
        assertThat(resultado.linhasValidas()).isEmpty();
    }

    @Test
    @DisplayName("Deve rejeitar linha com data inválida")
    void deveRejeitarLinhaComDataInvalida() {
        var csv = """
                data_vencimento,data_pagamento,valor,descricao,fornecedor_id
                data-invalida,,100.00,Teste,3fa85f64-5717-4562-b3fc-2c963f66afa6
                """;

        var resultado = parser.parsear(csv);
        assertThat(resultado.linhasValidas()).isEmpty();
        assertThat(resultado.erros()).hasSize(1);
    }
}
