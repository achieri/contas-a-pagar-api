package com.totvs.contaspagar.domain.model;

import com.totvs.contaspagar.domain.exception.DomainException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade raiz do agregado Usuario.
 * Representa um usuário do sistema com credenciais de autenticação.
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    protected Usuario() {}

    /**
     * Factory method — garante que o usuário nasce sempre no estado válido.
     *
     * @param username nome do usuário
     * @param passwordHash senha já codificada (bcrypt)
     * @return nova instância de Usuario
     */
    public static Usuario criar(String username, String passwordHash) {
        if (username == null || username.isBlank()) {
            throw new DomainException("Username não pode ser vazio.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainException("Senha não pode ser vazia.");
        }

        Usuario usuario = new Usuario();
        usuario.username = username.trim();
        usuario.password = passwordHash;
        usuario.role = "ROLE_USER";
        usuario.ativo = true;
        usuario.criadoEm = LocalDateTime.now();
        usuario.atualizadoEm = LocalDateTime.now();
        return usuario;
    }

    public void alterarSenha(String novoPasswordHash) {
        if (novoPasswordHash == null || novoPasswordHash.isBlank()) {
            throw new DomainException("Senha não pode ser vazia.");
        }
        this.password = novoPasswordHash;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void desativar() {
        this.ativo = false;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void ativar() {
        this.ativo = true;
        this.atualizadoEm = LocalDateTime.now();
    }

    public void promoverParaAdmin() {
        this.role = "ROLE_ADMIN";
        this.atualizadoEm = LocalDateTime.now();
    }

    public void rebaixarParaUsuario() {
        this.role = "ROLE_USER";
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // Getters (sem setters - entidade é modificada apenas via métodos de domínio)
    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
