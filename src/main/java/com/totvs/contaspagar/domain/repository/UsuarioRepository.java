package com.totvs.contaspagar.domain.repository;

import com.totvs.contaspagar.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta (interface) do repositório de Usuário.
 * Definida no domínio, implementada na infraestrutura.
 */
public interface UsuarioRepository {
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
    Optional<Usuario> buscarPorId(UUID id);
}
