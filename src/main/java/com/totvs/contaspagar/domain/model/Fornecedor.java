package com.totvs.contaspagar.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fornecedor")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Fornecedor() {}

    public Fornecedor(String nome) {
        this.nome = nome;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    public void atualizarNome(String novoNome) {
        this.nome = novoNome;
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
