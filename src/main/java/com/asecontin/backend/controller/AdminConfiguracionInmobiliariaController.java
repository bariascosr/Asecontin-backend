package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.ConfiguracionInmobiliariaRequest;
import com.asecontin.backend.dto.ConfiguracionInmobiliariaResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.service.ConfiguracionInmobiliariaService;
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
 * CRUD de configuración institucional (Acerca de nosotros). Requiere JWT.
 */
@RestController
@RequestMapping("/api/admin/configuracion-inmobiliaria")
@Tag(name = "Configuración institucional (admin)", description = "CRUD de misión, visión, términos, política de datos")
@SecurityRequirement(name = "bearerAuth")
public class AdminConfiguracionInmobiliariaController {

	private static final int MAX_PAGE_SIZE = 100;

	private final ConfiguracionInmobiliariaService configuracionInmobiliariaService;

	public AdminConfiguracionInmobiliariaController(ConfiguracionInmobiliariaService configuracionInmobiliariaService) {
		this.configuracionInmobiliariaService = configuracionInmobiliariaService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar configuraciones", description = "Lista todas las filas de configuración (normalmente una). Paginado: page, size (max 100).")
	public Mono<ApiResponse<PageResponse<ConfiguracionInmobiliariaResponse>>> listar(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		int safeSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
		int safePage = Math.max(0, page);
		return configuracionInmobiliariaService.listar(safePage, safeSize).map(ApiResponse::success);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Obtener por ID")
	public Mono<ApiResponse<ConfiguracionInmobiliariaResponse>> obtenerPorId(@PathVariable Long id) {
		return configuracionInmobiliariaService.obtenerPorId(id)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Crear configuración", description = "Añade una nueva fila de configuración institucional. Todos los campos son opcionales.")
	public Mono<ApiResponse<ConfiguracionInmobiliariaResponse>> crear(@Valid @RequestBody ConfiguracionInmobiliariaRequest request) {
		return configuracionInmobiliariaService.crear(request).map(ApiResponse::success);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Actualizar configuración")
	public Mono<ApiResponse<ConfiguracionInmobiliariaResponse>> actualizar(@PathVariable Long id, @Valid @RequestBody ConfiguracionInmobiliariaRequest request) {
		return configuracionInmobiliariaService.actualizar(id, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar configuración")
	public Mono<Void> eliminar(@PathVariable Long id) {
		return configuracionInmobiliariaService.eliminar(id)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
