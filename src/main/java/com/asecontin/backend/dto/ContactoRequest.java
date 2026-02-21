package com.asecontin.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud de contacto con el agente (formulario público).
 */
public record ContactoRequest(
		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 100)
		String nombre,

		@NotBlank(message = "El email es obligatorio")
		@Email
		@Size(max = 150)
		String email,

		@NotBlank(message = "El mensaje es obligatorio")
		@Size(max = 2000)
		String mensaje,

		/** ID del inmueble por el que consulta (opcional). */
		Long inmuebleId
) {}
