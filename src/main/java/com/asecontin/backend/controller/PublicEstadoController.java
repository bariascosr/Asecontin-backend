package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.EstadoResponse;
import com.asecontin.backend.service.EstadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Catálogo público de estados de inmueble (En venta, Disponible para arriendo, Arrendado, Vendido, etc.).
 * Permite al cliente filtrar inmuebles por estado en GET /api/public/inmuebles?estadoId=...
 */
@RestController
@RequestMapping("/api/public/estados")
@Tag(name = "Estados (público)", description = "Catálogo de estados de inmueble para filtros (en venta, en arriendo, arrendado, vendido)")
public class PublicEstadoController {

	private final EstadoService estadoService;

	public PublicEstadoController(EstadoService estadoService) {
		this.estadoService = estadoService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar estados",
			description = "Devuelve todos los estados de inmueble (En venta, Disponible para arriendo, Arrendado, Vendido, etc.) para usar en filtros. El cliente puede filtrar inmuebles por estadoId en GET /api/public/inmuebles?estadoId=...")
	public Mono<ApiResponse<List<EstadoResponse>>> listar() {
		return estadoService.listarTodos()
				.collectList()
				.map(ApiResponse::success);
	}
}
