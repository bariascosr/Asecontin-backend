package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.InmuebleDetallePublicoResponse;
import com.asecontin.backend.dto.InmuebleResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.service.InmuebleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * API pública de inmuebles: sin autenticación. Búsqueda, filtros y detalle con galería.
 */
@RestController
@RequestMapping("/api/public/inmuebles")
@Tag(name = "Inmuebles (público)", description = "Búsqueda y detalle de inmuebles para visitantes")
public class PublicInmuebleController {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	private final InmuebleService inmuebleService;

	public PublicInmuebleController(InmuebleService inmuebleService) {
		this.inmuebleService = inmuebleService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Buscar inmuebles",
			description = "Lista inmuebles con filtros opcionales. Se pueden combinar: estadoId, localidadId, tipoId, rangos de precio, área (m²), habitaciones, baños, estrato y parqueaderos. Paginado: page (0-based), size (default 20, max 100).")
	public Mono<ApiResponse<PageResponse<InmuebleResponse>>> listar(
			@RequestParam(required = false) Long estadoId,
			@RequestParam(required = false) Long localidadId,
			@RequestParam(required = false) Long tipoId,
			@RequestParam(required = false) BigDecimal precioMin,
			@RequestParam(required = false) BigDecimal precioMax,
			@RequestParam(required = false) BigDecimal areaMin,
			@RequestParam(required = false) BigDecimal areaMax,
			@RequestParam(required = false) Integer habitacionesMin,
			@RequestParam(required = false) Integer habitacionesMax,
			@RequestParam(required = false) Integer banosMin,
			@RequestParam(required = false) Integer banosMax,
			@RequestParam(required = false) Integer estratoMin,
			@RequestParam(required = false) Integer estratoMax,
			@RequestParam(required = false) Integer parqueaderosMin,
			@RequestParam(required = false) Integer parqueaderosMax,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
		int safePage = Math.max(0, page);
		return inmuebleService.listar(
				Optional.ofNullable(estadoId),
				Optional.ofNullable(localidadId),
				Optional.ofNullable(tipoId),
				Optional.ofNullable(precioMin),
				Optional.ofNullable(precioMax),
				Optional.ofNullable(areaMin),
				Optional.ofNullable(areaMax),
				Optional.ofNullable(habitacionesMin),
				Optional.ofNullable(habitacionesMax),
				Optional.ofNullable(banosMin),
				Optional.ofNullable(banosMax),
				Optional.ofNullable(estratoMin),
				Optional.ofNullable(estratoMax),
				Optional.ofNullable(parqueaderosMin),
				Optional.ofNullable(parqueaderosMax),
				safePage,
				safeSize)
				.map(ApiResponse::success);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Detalle de inmueble",
			description = "Devuelve el inmueble con listas de URLs de imágenes y videos (galería).")
	public Mono<ApiResponse<InmuebleDetallePublicoResponse>> detalle(@PathVariable Long id) {
		return inmuebleService.obtenerDetallePublico(id)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
