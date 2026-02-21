package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Endpoints de ejemplo del panel administrativo. Requieren JWT.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administrador", description = "Endpoints protegidos (requieren token JWT)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

	@GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Usuario actual", description = "Devuelve el email del administrador autenticado. Sirve para probar el JWT.")
	public Mono<ApiResponse<Map<String, String>>> me(@AuthenticationPrincipal String email) {
		String userEmail = email != null ? email : "anon";
		return Mono.just(ApiResponse.success(Map.of("email", userEmail, "message", "Acceso autorizado")));
	}
}
