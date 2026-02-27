package com.totvs.contaspagar.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "importacao_log")
public class ImportacaoLog {

    public enum Status { AGUARDANDO, PROCESSANDO, CONCLUIDO, CONCLUIDO_COM_ERROS, FALHA }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "protocolo_id", nullable = false, unique = true)
    private UUID protocoloId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "status_importacao")
    private Status status;

    @Column(name = "total_linhas")
    private Integer totalLinhas;

    @Column(name = "linhas_processadas")
    private Integer linhasProcessadas = 0;

    @Column(name = "linhas_com_erro")
    private Integer linhasComErro = 0;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @Column(name = "solicitado_em", nullable = false, updatable = false)
    private LocalDateTime solicitadoEm;

    @Column(name = "processado_em")
    private LocalDateTime processadoEm;

    protected ImportacaoLog() {}

    public static ImportacaoLog criar(UUID protocoloId) {
        var log = new ImportacaoLog();
        log.protocoloId = protocoloId;
        log.status = Status.AGUARDANDO;
        log.solicitadoEm = LocalDateTime.now();
        return log;
    }

    public void iniciarProcessamento(int totalLinhas) {
        this.status = Status.PROCESSANDO;
        this.totalLinhas = totalLinhas;
    }

    public void concluir(int processadas, int comErro, String detalhesErros) {
        this.linhasProcessadas = processadas;
        this.linhasComErro = comErro;
        this.processadoEm = LocalDateTime.now();
        this.status = comErro > 0 ? Status.CONCLUIDO_COM_ERROS : Status.CONCLUIDO;
        if (detalhesErros != null && !detalhesErros.isBlank()) {
            this.mensagemErro = detalhesErros;
        }
    }

    public void falhar(String mensagem) {
        this.status = Status.FALHA;
        this.mensagemErro = mensagem;
        this.processadoEm = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getProtocoloId() { return protocoloId; }
    public Status getStatus() { return status; }
    public Integer getTotalLinhas() { return totalLinhas; }
    public Integer getLinhasProcessadas() { return linhasProcessadas; }
    public Integer getLinhasComErro() { return linhasComErro; }
    public String getMensagemErro() { return mensagemErro; }
    public LocalDateTime getSolicitadoEm() { return solicitadoEm; }
    public LocalDateTime getProcessadoEm() { return processadoEm; }
}
