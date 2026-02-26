package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.PropietarioRequest;
import com.asecontin.backend.dto.PropietarioResponse;
import com.asecontin.backend.service.PropietarioService;
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
 * CRUD de propietarios. Solo administrador (JWT). Incluye consulta por cédula y por fecha de expedición con inmuebles asociados y valor de arriendo.
 */
@RestController
@RequestMapping("/api/admin/propietarios")
@Tag(name = "Propietarios (admin)", description = "CRUD de propietarios e inmuebles asociados")
@SecurityRequirement(name = "bearerAuth")
public class AdminPropietarioController {

	private static final int MAX_PAGE_SIZE = 100;

	private final PropietarioService propietarioService;

	public AdminPropietarioController(PropietarioService propietarioService) {
		this.propietarioService = propietarioService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar propietarios", description = "Paginado. Opcional: cedula o fechaExpedicion para filtrar y obtener inmuebles asociados con valor de arriendo.")
	public Mono<ApiResponse<Object>> listar(
			@RequestParam(required = false) String cedula,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaExpedicion,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		if (cedula != null && !cedula.isBlank()) {
			return propietarioService.obtenerPorCedula(cedula.trim())
					.map(r -> ApiResponse.<Object>success(r))
					.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
		}
		if (fechaExpedicion != null) {
			return propietarioService.listarPorFechaExpedicion(fechaExpedicion).collectList().map(ApiResponse::success);
		}
		int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
		int safePage = Math.max(0, page);
		return propietarioService.listar(safePage, safeSize).map(ApiResponse::success);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Obtener propietario por ID", description = "Incluye listado de inmuebles asociados con valor de arriendo.")
	public Mono<ApiResponse<PropietarioResponse>> obtenerPorId(@PathVariable Long id) {
		return propietarioService.obtenerPorId(id)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear propietario",
			description = "Cuerpo: nombres, apellidos, cedula, fechaExpedicion (opcional). inmuebleIds (obligatorio): lista de IDs de inmuebles a asignar como propiedad; debe incluir al menos un inmueble. La respuesta incluye el propietario con sus inmuebles.")
	public Mono<ApiResponse<PropietarioResponse>> crear(@Valid @RequestBody PropietarioRequest request) {
		return propietarioService.crear(request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Actualizar propietario")
	public Mono<ApiResponse<PropietarioResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody PropietarioRequest request) {
		return propietarioService.actualizar(id, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar propietario")
	public Mono<Void> eliminar(@PathVariable Long id) {
		return propietarioService.eliminar(id)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
