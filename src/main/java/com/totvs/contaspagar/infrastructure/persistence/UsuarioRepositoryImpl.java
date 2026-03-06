package com.totvs.contaspagar.infrastructure.persistence;

import com.totvs.contaspagar.domain.model.Usuario;
import com.totvs.contaspagar.domain.repository.UsuarioRepository;
import com.totvs.contaspagar.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryImpl(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return jpaRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByUsernameIgnoreCase(String username) {
        return jpaRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return jpaRepository.findById(id);
    }
}
