package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.LocalidadRequest;
import com.asecontin.backend.dto.LocalidadResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.service.LocalidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * CRUD de localidades (por ciudad). Requiere JWT.
 */
@RestController
@RequestMapping("/api/admin/localidades")
@Tag(name = "Gestión de localidades", description = "CRUD de localidades por ciudad (principalmente Bogotá)")
@SecurityRequirement(name = "bearerAuth")
public class AdminLocalidadController {

	private static final int MAX_PAGE_SIZE = 100;

	private final LocalidadService localidadService;

	public AdminLocalidadController(LocalidadService localidadService) {
		this.localidadService = localidadService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar localidades", description = "Paginado: page (0-based), size (default 20, max 100).")
	public Mono<ApiResponse<PageResponse<LocalidadResponse>>> listar(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
		int safePage = Math.max(0, page);
		return localidadService.listar(safePage, safeSize).map(ApiResponse::success);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Obtener localidad por ID")
	public Mono<ApiResponse<LocalidadResponse>> obtenerPorId(@PathVariable Long id) {
		return localidadService.obtenerPorId(id)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear localidad", description = "nombre y ciudadId obligatorios. ciudadId debe existir en GET /api/public/ciudades. No se puede repetir el mismo nombre en la misma ciudad.")
	public Mono<ApiResponse<LocalidadResponse>> crear(@Valid @RequestBody LocalidadRequest request) {
		return localidadService.crear(request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Actualizar localidad")
	public Mono<ApiResponse<LocalidadResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody LocalidadRequest request) {
		return localidadService.actualizar(id, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar localidad")
	public Mono<Void> eliminar(@PathVariable Long id) {
		return localidadService.eliminar(id)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
