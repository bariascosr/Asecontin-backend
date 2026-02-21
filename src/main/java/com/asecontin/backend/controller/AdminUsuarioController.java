package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ActualizarUsuarioRequest;
import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.UsuarioResponse;
import com.asecontin.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;

/**
 * Gestión del perfil del administrador (Paso 3).
 * Todos los endpoints requieren JWT.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@Tag(name = "Gestión de usuario administrador", description = "Consultar y actualizar el perfil del administrador")
@SecurityRequirement(name = "bearerAuth")
public class AdminUsuarioController {

	private final UsuarioService usuarioService;

	public AdminUsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Mi perfil", description = "Devuelve los datos del administrador autenticado (sin contraseña).")
	public Mono<ApiResponse<UsuarioResponse>> me(@AuthenticationPrincipal String email) {
		if (email == null || email.isBlank()) {
			return Mono.error(new ResponseStatusException(BAD_REQUEST, "No se pudo identificar al usuario"));
		}
		return usuarioService.obtenerPorEmail(email).map(ApiResponse::success);
	}

	@PutMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Actualizar mi perfil",
			description = "Actualiza nombre, email y/o contraseña. Para cambiar la contraseña es obligatorio enviar passwordActual y nuevaPassword.")
	public Mono<ApiResponse<UsuarioResponse>> actualizarMe(
			@AuthenticationPrincipal String email,
			@Valid @RequestBody ActualizarUsuarioRequest request) {
		if (email == null || email.isBlank()) {
			return Mono.error(new ResponseStatusException(BAD_REQUEST, "No se pudo identificar al usuario"));
		}
		return usuarioService.actualizar(email, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(BAD_REQUEST, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(CONFLICT, e.getMessage())));
	}
}
