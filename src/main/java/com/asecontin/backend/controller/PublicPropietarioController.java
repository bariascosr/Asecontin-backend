package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.PropietarioResponse;
import com.asecontin.backend.service.PropietarioService;
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
 * Consulta pública: el cliente (propietario) debe indicar cédula y fecha de expedición para consultar sus inmuebles y el valor de arriendo.
 */
@RestController
@RequestMapping("/api/public/propietarios")
@Tag(name = "Propietarios (público)", description = "Consulta de inmuebles del propietario por cédula y fecha de expedición (ambos requeridos)")
public class PublicPropietarioController {

	private final PropietarioService propietarioService;

	public PublicPropietarioController(PropietarioService propietarioService) {
		this.propietarioService = propietarioService;
	}

	@GetMapping(value = "/inmuebles", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Inmuebles del propietario",
			description = "Consulta pública: requiere cédula y fechaExpedicion (yyyy-MM-dd). Devuelve datos del propietario y listado de todos los inmuebles asociados (información completa, incl. valorArriendo).")
	public Mono<ApiResponse<PropietarioResponse>> inmueblesPorCedulaYFechaExpedicion(
			@RequestParam String cedula,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaExpedicion) {
		if (cedula == null || cedula.isBlank()) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El parámetro cedula es obligatorio"));
		}
		if (fechaExpedicion == null) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "El parámetro fechaExpedicion es obligatorio"));
		}
		return propietarioService.obtenerPorCedulaYFechaExpedicion(cedula.trim(), fechaExpedicion)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
