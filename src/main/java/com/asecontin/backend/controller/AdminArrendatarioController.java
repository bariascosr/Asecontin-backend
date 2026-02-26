package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.ArrendatarioRequest;
import com.asecontin.backend.dto.ArrendatarioResponse;
import com.asecontin.backend.service.ArrendatarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * CRUD de arrendatarios. Solo administrador (JWT). Incluye consulta por cédula y por fecha de expedición con inmuebles asociados y valor de arriendo. Asociar/desasociar a inmuebles.
 */
@RestController
@RequestMapping("/api/admin/arrendatarios")
@Tag(name = "Arrendatarios (admin)", description = "CRUD de arrendatarios e inmuebles en arriendo")
@SecurityRequirement(name = "bearerAuth")
public class AdminArrendatarioController {

	private static final int MAX_PAGE_SIZE = 100;

	private final ArrendatarioService arrendatarioService;

	public AdminArrendatarioController(ArrendatarioService arrendatarioService) {
		this.arrendatarioService = arrendatarioService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar arrendatarios", description = "Paginado. Opcional: cedula o fechaExpedicion para filtrar y obtener inmuebles asociados con valor de arriendo.")
	public Mono<ApiResponse<Object>> listar(
			@RequestParam(required = false) String cedula,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaExpedicion,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (cedula != null && !cedula.isBlank()) {
			return arrendatarioService.obtenerPorCedula(cedula.trim())
					.map(r -> ApiResponse.<Object>success(r))
					.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
		}
		if (fechaExpedicion != null) {
			return arrendatarioService.listarPorFechaExpedicion(fechaExpedicion).collectList().map(ApiResponse::success);
		}
		int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
		int safePage = Math.max(0, page);
		return arrendatarioService.listar(safePage, safeSize).map(ApiResponse::success);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Obtener arrendatario por ID", description = "Incluye listado de inmuebles donde está en arriendo con valor de arriendo.")
	public Mono<ApiResponse<ArrendatarioResponse>> obtenerPorId(@PathVariable Long id) {
		return arrendatarioService.obtenerPorId(id)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear arrendatario",
			description = "Cuerpo: nombres, apellidos, cedula, fechaExpedicion (opcional). inmuebleIds (obligatorio): lista de IDs de inmuebles con los que asociar; debe incluir al menos un inmueble. La respuesta incluye el arrendatario con sus inmuebles.")
	public Mono<ApiResponse<ArrendatarioResponse>> crear(@Valid @RequestBody ArrendatarioRequest request) {
		return arrendatarioService.crear(request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Actualizar arrendatario")
	public Mono<ApiResponse<ArrendatarioResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody ArrendatarioRequest request) {
		return arrendatarioService.actualizar(id, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar arrendatario")
	public Mono<Void> eliminar(@PathVariable Long id) {
		return arrendatarioService.eliminar(id)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(value = "/{arrendatarioId}/inmuebles/{inmuebleId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Asociar arrendatario a inmueble")
	public Mono<Void> asociarInmueble(@PathVariable Long arrendatarioId, @PathVariable Long inmuebleId) {
		return arrendatarioService.asociarInmueble(arrendatarioId, inmuebleId)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@DeleteMapping(value = "/{arrendatarioId}/inmuebles/{inmuebleId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Desasociar arrendatario de inmueble")
	public Mono<Void> desasociarInmueble(@PathVariable Long arrendatarioId, @PathVariable Long inmuebleId) {
		return arrendatarioService.desasociarInmueble(arrendatarioId, inmuebleId);
	}
}
