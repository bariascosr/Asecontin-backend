package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.LocalidadResponse;
import com.asecontin.backend.service.LocalidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/public/localidades")
@Tag(name = "Localidades (público)", description = "Localidades por ciudad para selector en inmuebles (principalmente Bogotá)")
public class PublicLocalidadController {

	private final LocalidadService localidadService;

	public PublicLocalidadController(LocalidadService localidadService) {
		this.localidadService = localidadService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar localidades", description = "Todas las localidades o solo las de una ciudad si se envía ciudadId.")
	public Mono<ApiResponse<List<LocalidadResponse>>> listar(@RequestParam(required = false) Long ciudadId) {
		if (ciudadId != null) {
			return localidadService.listarPorCiudad(ciudadId).collectList().map(ApiResponse::success);
		}
		return localidadService.listarTodas().collectList().map(ApiResponse::success);
	}
}
