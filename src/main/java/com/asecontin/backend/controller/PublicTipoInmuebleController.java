package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.TipoInmuebleResponse;
import com.asecontin.backend.service.TipoInmuebleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Catálogo de tipos de inmueble (Casa, Apartamento, Lote, etc.) para formularios y filtros.
 */
@RestController
@RequestMapping("/api/public/tipos-inmueble")
@Tag(name = "Tipos de inmueble (público)", description = "Tipos permitidos para el campo tipo de inmuebles")
public class PublicTipoInmuebleController {

	private final TipoInmuebleService tipoInmuebleService;

	public PublicTipoInmuebleController(TipoInmuebleService tipoInmuebleService) {
		this.tipoInmuebleService = tipoInmuebleService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar tipos de inmueble", description = "Devuelve todos los tipos (Casa, Apartamento, Lote, etc.) para selector y filtros.")
	public Mono<ApiResponse<List<TipoInmuebleResponse>>> listar() {
		return tipoInmuebleService.listarTodos()
				.collectList()
				.map(ApiResponse::success);
	}
}
