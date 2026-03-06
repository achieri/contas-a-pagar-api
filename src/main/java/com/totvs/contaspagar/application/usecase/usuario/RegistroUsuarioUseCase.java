package com.totvs.contaspagar.application.usecase.usuario;

import com.totvs.contaspagar.application.dto.request.RegistroRequest;
import com.totvs.contaspagar.domain.exception.UsuarioJaExistenteException;
import com.totvs.contaspagar.domain.model.Usuario;
import com.totvs.contaspagar.domain.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistroUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroUsuarioUseCase(UsuarioRepository usuarioRepository,
                                   PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void executar(RegistroRequest request) {
        if (usuarioRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new UsuarioJaExistenteException(request.username());
        }

        String passwordHash = passwordEncoder.encode(request.password());
        var usuario = Usuario.criar(request.username(), passwordHash);
        usuarioRepository.salvar(usuario);
    }
}
