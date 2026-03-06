package com.totvs.contaspagar.infrastructure.persistence.jpa;

import com.totvs.contaspagar.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório JPA para persistência de Usuário.
 * Interface técnica, usada apenas pela implementação do repositório de domínio.
 */
public interface UsuarioJpaRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByUsernameIgnoreCase(String username);
}
