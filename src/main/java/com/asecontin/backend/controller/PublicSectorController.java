package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.SectorResponse;
import com.asecontin.backend.service.SectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Catálogo de sectores de ciudad (Oriente, Occidente, etc.) para formularios y filtros.
 */
@RestController
@RequestMapping("/api/public/sectores")
@Tag(name = "Sectores (público)", description = "Sectores de ciudad para el campo sector del inmueble")
public class PublicSectorController {

	private final SectorService sectorService;

	public PublicSectorController(SectorService sectorService) {
		this.sectorService = sectorService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar sectores", description = "Devuelve todos los sectores (Oriente, Occidente, Norte, Sur, etc.) para selector.")
	public Mono<ApiResponse<List<SectorResponse>>> listar() {
		return sectorService.listarTodos()
				.collectList()
				.map(ApiResponse::success);
	}
}
