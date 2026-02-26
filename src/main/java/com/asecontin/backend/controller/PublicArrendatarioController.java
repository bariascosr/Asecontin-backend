package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.ArrendatarioResponse;
import com.asecontin.backend.service.ArrendatarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Consulta pública: el arrendatario debe indicar cédula y fecha de expedición para consultar los inmuebles donde se hospeda y cuánto debe pagar.
 */
@RestController
@RequestMapping("/api/public/arrendatarios")
@Tag(name = "Arrendatarios (público)", description = "Consulta de inmuebles en arriendo por cédula y fecha de expedición (ambos requeridos)")
public class PublicArrendatarioController {

	private final ArrendatarioService arrendatarioService;

	public PublicArrendatarioController(ArrendatarioService arrendatarioService) {
		this.arrendatarioService = arrendatarioService;
	}

	@GetMapping(value = "/inmuebles", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Inmuebles del arrendatario",
			description = "Consulta pública: requiere cédula y fechaExpedicion (yyyy-MM-dd). Devuelve datos del arrendatario y listado de inmuebles donde está en arriendo con valor a pagar (valorArriendo).")
	public Mono<ApiResponse<ArrendatarioResponse>> inmueblesPorCedulaYFechaExpedicion(
			@RequestParam String cedula,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaExpedicion) {
		if (cedula == null || cedula.isBlank()) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El parámetro cedula es obligatorio"));
		}
		if (fechaExpedicion == null) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El parámetro fechaExpedicion es obligatorio"));
		}
		return arrendatarioService.obtenerPorCedulaYFechaExpedicion(cedula.trim(), fechaExpedicion)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
