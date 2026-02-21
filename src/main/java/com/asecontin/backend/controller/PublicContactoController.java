package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ContactoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Endpoint público para contacto con el agente (formulario).
 * Acepta el mensaje y responde 202; la integración con email/WhatsApp puede añadirse después.
 */
@RestController
@RequestMapping("/api/public")
@Tag(name = "Contacto (público)", description = "Formulario de contacto con el agente")
public class PublicContactoController {

	@PostMapping(value = "/contacto", consumes = "application/json")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(summary = "Enviar mensaje de contacto",
			description = "Recibe nombre, email, mensaje e inmuebleId opcional. Responde 202 Accepted. Preparado para integrar con envío de email o notificación.")
	public Mono<Void> contacto(@Valid @RequestBody ContactoRequest request) {
		// TODO: integrar con servicio de email o almacenar en BD (tabla contacto)
		return Mono.empty();
	}
}
