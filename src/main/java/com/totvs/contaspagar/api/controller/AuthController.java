package com.totvs.contaspagar.api.controller;

import com.totvs.contaspagar.application.dto.request.LoginRequest;
import com.totvs.contaspagar.application.dto.request.RegistroRequest;
import com.totvs.contaspagar.application.dto.response.TokenResponse;
import com.totvs.contaspagar.application.usecase.usuario.RegistroUsuarioUseCase;
import com.totvs.contaspagar.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final RegistroUsuarioUseCase registroUseCase;

    public AuthController(AuthenticationManager authManager,
                          JwtTokenProvider tokenProvider,
                          RegistroUsuarioUseCase registroUseCase) {
        this.authManager = authManager;
        this.tokenProvider = tokenProvider;
        this.registroUseCase = registroUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar e obter token JWT")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        var authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");
        var token = tokenProvider.gerarToken(request.username(), role);
        return ResponseEntity.ok(new TokenResponse(token, tokenProvider.getExpirationMs()));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar novo usuário")
    public ResponseEntity<Void> registrar(@Valid @RequestBody RegistroRequest request) {
        registroUseCase.executar(request);
        return ResponseEntity.ok().build();
    }
}
