package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.ConfiguracionInmobiliariaResponse;
import com.asecontin.backend.service.ConfiguracionInmobiliariaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/public/configuracion-inmobiliaria")
@Tag(name = "Configuración institucional (público)", description = "Contenido para Acerca de nosotros, términos, política de datos")
public class PublicConfiguracionInmobiliariaController {

	private final ConfiguracionInmobiliariaService configuracionInmobiliariaService;

	public PublicConfiguracionInmobiliariaController(ConfiguracionInmobiliariaService configuracionInmobiliariaService) {
		this.configuracionInmobiliariaService = configuracionInmobiliariaService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Obtener configuración actual", description = "Devuelve la configuración institucional para mostrar en Acerca de nosotros.")
	public Mono<ApiResponse<ConfiguracionInmobiliariaResponse>> obtenerActual() {
		return configuracionInmobiliariaService.obtenerActual()
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
