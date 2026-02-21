package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.AuthResponse;
import com.asecontin.backend.dto.LoginRequest;
import com.asecontin.backend.dto.RegisterRequest;
import com.asecontin.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login y registro del administrador")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	@Operation(summary = "Login", description = "Inicia sesión con email y contraseña. Devuelve un token JWT.")
	public Mono<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas")));
	}

	@PostMapping("/register")
	@Operation(summary = "Registro primer administrador",
			description = "Registra el único administrador. Solo funciona si aún no existe ningún usuario.")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<ApiResponse<AuthResponse>> registerFirstAdmin(@Valid @RequestBody RegisterRequest request) {
		return authService.registerFirstAdmin(request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}
}
