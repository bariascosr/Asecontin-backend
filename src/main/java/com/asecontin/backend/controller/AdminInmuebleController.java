package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.InmuebleRequest;
import com.asecontin.backend.dto.InmuebleResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.service.InmuebleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * CRUD de inmuebles. Requiere JWT.
 */
@RestController
@RequestMapping("/api/admin/inmuebles")
@Tag(name = "Gestión de inmuebles", description = "CRUD de inmuebles (casa, apartamento) con estado y filtros")
@SecurityRequirement(name = "bearerAuth")
public class AdminInmuebleController {

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	private final InmuebleService inmuebleService;

	public AdminInmuebleController(InmuebleService inmuebleService) {
		this.inmuebleService = inmuebleService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar inmuebles",
			description = "Lista todos o filtra por estadoId, localidadId, tipoId, rangos de precio, área (m²), habitaciones, baños, estrato y parqueaderos. Los filtros se combinan. Paginado: page (0-based), size (default 20, max 100).")
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
				List.of(),
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
	@Operation(summary = "Obtener inmueble por ID")
	public Mono<ApiResponse<InmuebleResponse>> obtenerPorId(@PathVariable Long id) {
		return inmuebleService.obtenerPorId(id)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear inmueble",
			description = "Requiere JWT. Al menos uno de precioVenta (precio de venta) o valorArriendo es obligatorio; si es arriendo no es necesario enviar precioVenta. estadoId, localidadId, tipoId obligatorios. Resto de campos opcionales. Todos los campos son editables en PUT.")
	public Mono<ApiResponse<InmuebleResponse>> crear(@AuthenticationPrincipal String email, @Valid @RequestBody InmuebleRequest request) {
		if (email == null || email.isBlank()) {
			return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no identificado"));
		}
		return inmuebleService.crear(request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Actualizar inmueble", description = "Todos los campos del inmueble son modificables: titulo, descripcion, precioVenta, valorArriendo, direccion, localidadId, tipoId, estadoId, propietarioId, etiquetas, parqueaderos, sectorId, areaM2, habitaciones, banos, estrato, valorAdministracion, anoConstruccion, amoblado, piso. Al menos uno de precioVenta o valorArriendo es obligatorio.")
	public Mono<ApiResponse<InmuebleResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody InmuebleRequest request) {
		return inmuebleService.actualizar(id, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(
						e.getMessage() != null && e.getMessage().contains("no encontrado") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST, e.getMessage())));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar inmueble")
	public Mono<Void> eliminar(@PathVariable Long id) {
		return inmuebleService.eliminar(id)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
