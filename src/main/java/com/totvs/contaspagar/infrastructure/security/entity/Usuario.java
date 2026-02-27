package com.totvs.contaspagar.infrastructure.security.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    protected Usuario() {}

    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = "ROLE_USER";
        this.ativo = true;
        this.criadoEm = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public boolean isAtivo() { return ativo; }
}
