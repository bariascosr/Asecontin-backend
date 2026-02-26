package com.asecontin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public record ArrendatarioRequest(
		@NotBlank(message = "Los nombres son obligatorios")
		@Size(max = 100)
		String nombres,

		@NotBlank(message = "Los apellidos son obligatorios")
		@Size(max = 100)
		String apellidos,

		@NotBlank(message = "La cédula es obligatoria")
		@Size(max = 20)
		String cedula,

		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate fechaExpedicion,

		/** Teléfono (ej. celular) para recordatorios por WhatsApp. Opcional. */
		@Size(max = 20)
		String telefono,

		/** IDs de inmuebles a asociar (obligatorio al crear; opcional al actualizar). */
		List<Long> inmuebleIds
) {}
