package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.EstadoRequest;
import com.asecontin.backend.dto.EstadoResponse;
import com.asecontin.backend.service.EstadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import com.asecontin.backend.dto.PageResponse;

/**
 * CRUD de estados (disponible, vendido, rentado). Requiere JWT.
 */
@RestController
@RequestMapping("/api/admin/estados")
@Tag(name = "Gestión de estados", description = "CRUD de estados de inmuebles (disponible, vendido, rentado)")
@SecurityRequirement(name = "bearerAuth")
public class AdminEstadoController {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	private final EstadoService estadoService;

	public AdminEstadoController(EstadoService estadoService) {
		this.estadoService = estadoService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar estados", description = "Devuelve estados ordenados por nombre. Parámetros: page (0-based), size (default 20, max 100).")
	public Mono<ApiResponse<PageResponse<EstadoResponse>>> listar(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
		int safePage = Math.max(0, page);
		return estadoService.listar(safePage, safeSize).map(ApiResponse::success);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Obtener estado por ID")
	public Mono<ApiResponse<EstadoResponse>> obtenerPorId(@PathVariable Long id) {
		return estadoService.obtenerPorId(id)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear estado", description = "Crea un nuevo estado. El nombre no puede repetirse.")
	public Mono<ApiResponse<EstadoResponse>> crear(@Valid @RequestBody EstadoRequest request) {
		return estadoService.crear(request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Actualizar estado")
	public Mono<ApiResponse<EstadoResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody EstadoRequest request) {
		return estadoService.actualizar(id, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar estado", description = "Falla si el estado está en uso por algún inmueble.")
	public Mono<Void> eliminar(@PathVariable Long id) {
		return estadoService.eliminar(id)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}
}
