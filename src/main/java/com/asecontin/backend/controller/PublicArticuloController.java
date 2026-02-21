package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.ArticuloResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.service.ArticuloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * API pública del blog/noticias: listado y detalle sin autenticación.
 */
@RestController
@RequestMapping("/api/public/articulos")
@Tag(name = "Blog / Noticias (público)", description = "Ver artículos y noticias inmobiliarias")
public class PublicArticuloController {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	private final ArticuloService articuloService;

	public PublicArticuloController(ArticuloService articuloService) {
		this.articuloService = articuloService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar artículos", description = "Artículos ordenados por fecha (más recientes primero). Paginado: page (0-based), size (default 20, max 100).")
	public Mono<ApiResponse<PageResponse<ArticuloResponse>>> listar(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
		int safePage = Math.max(0, page);
		return articuloService.listar(safePage, safeSize).map(ApiResponse::success);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Ver artículo")
	public Mono<ApiResponse<ArticuloResponse>> ver(@PathVariable Long id) {
		return articuloService.obtenerPorId(id)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
