package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.ArticuloRequest;
import com.asecontin.backend.dto.ArticuloResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.service.ArticuloService;
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
 * CRUD de artículos del blog/noticias. Requiere JWT.
 */
@RestController
@RequestMapping("/api/admin/articulos")
@Tag(name = "Blog / Noticias (admin)", description = "Crear, editar y eliminar artículos del blog")
@SecurityRequirement(name = "bearerAuth")
public class AdminArticuloController {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	private final ArticuloService articuloService;

	public AdminArticuloController(ArticuloService articuloService) {
		this.articuloService = articuloService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar artículos", description = "Paginado: page (0-based), size (default 20, max 100).")
	public Mono<ApiResponse<PageResponse<ArticuloResponse>>> listar(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
		int safePage = Math.max(0, page);
		return articuloService.listar(safePage, safeSize).map(ApiResponse::success);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Obtener artículo por ID")
	public Mono<ApiResponse<ArticuloResponse>> obtenerPorId(@PathVariable Long id) {
		return articuloService.obtenerPorId(id)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear artículo")
	public Mono<ApiResponse<ArticuloResponse>> crear(@Valid @RequestBody ArticuloRequest request) {
		return articuloService.crear(request).map(ApiResponse::success);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Actualizar artículo")
	public Mono<ApiResponse<ArticuloResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody ArticuloRequest request) {
		return articuloService.actualizar(id, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar artículo")
	public Mono<Void> eliminar(@PathVariable Long id) {
		return articuloService.eliminar(id)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
