package com.totvs.contaspagar.api.controller;

import com.totvs.contaspagar.application.dto.request.LoginRequest;
import com.totvs.contaspagar.application.dto.request.RegistroRequest;
import com.totvs.contaspagar.application.dto.response.TokenResponse;
import com.totvs.contaspagar.infrastructure.security.JwtTokenProvider;
import com.totvs.contaspagar.infrastructure.security.entity.Usuario;
import com.totvs.contaspagar.infrastructure.security.entity.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenProvider tokenProvider,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar e obter token JWT")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var token = tokenProvider.gerarToken(request.username());
        return ResponseEntity.ok(new TokenResponse(token, tokenProvider.getExpirationMs()));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar novo usuário")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroRequest request) {
        if (usuarioRepository.existsByUsernameIgnoreCase(request.username())) {
            var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
            pd.setTitle("Usuário já existe");
            pd.setDetail("Username '" + request.username() + "' já está em uso.");
            pd.setType(URI.create("urn:contas-pagar:conflict"));
            return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
        }
        var usuario = new Usuario(request.username(), passwordEncoder.encode(request.password()));
        usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
