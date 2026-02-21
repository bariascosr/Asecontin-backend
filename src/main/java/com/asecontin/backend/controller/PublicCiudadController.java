package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.CiudadResponse;
import com.asecontin.backend.service.CiudadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Lista de ciudades permitidas (Colombia) para uso en formularios de inmuebles.
 */
@RestController
@RequestMapping("/api/public/ciudades")
@Tag(name = "Ciudades (público)", description = "Ciudades de Colombia para selector de ciudad en inmuebles")
public class PublicCiudadController {

	private final CiudadService ciudadService;

	public PublicCiudadController(CiudadService ciudadService) {
		this.ciudadService = ciudadService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar ciudades", description = "Devuelve todas las ciudades permitidas (capitales de departamentos de Colombia) para usar en filtros y formularios.")
	public Mono<ApiResponse<List<CiudadResponse>>> listar() {
		return ciudadService.listarTodas()
				.collectList()
				.map(ApiResponse::success);
	}
}
