package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ContactoRequest;
import com.asecontin.backend.service.WhatsappNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Endpoint público para contacto con el agente (formulario).
 * Acepta el mensaje, envía notificación al agente por WhatsApp (si está configurado) y responde 202.
 */
@RestController
@RequestMapping("/api/public")
@Tag(name = "Contacto (público)", description = "Formulario de contacto con el agente")
public class PublicContactoController {

	private final WhatsappNotificationService whatsappNotificationService;

	public PublicContactoController(WhatsappNotificationService whatsappNotificationService) {
		this.whatsappNotificationService = whatsappNotificationService;
	}

	@PostMapping(value = "/contacto", consumes = "application/json")
	@ResponseStatus(HttpStatus.ACCEPTED)
	@Operation(summary = "Enviar mensaje de contacto",
			description = "Recibe nombre, email, mensaje e inmuebleId opcional. Envía notificación al agente por WhatsApp y responde 202 Accepted.")
	public Mono<Void> contacto(@Valid @RequestBody ContactoRequest request) {
		return whatsappNotificationService.notifyContactToAgent(request).then();
	}
}
