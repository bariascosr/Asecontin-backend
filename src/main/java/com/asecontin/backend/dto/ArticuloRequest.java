package com.asecontin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticuloRequest(
		@NotBlank(message = "El título es obligatorio")
		@Size(max = 150)
		String titulo,

		@NotBlank(message = "El contenido es obligatorio")
		@Size(max = 50000)
		String contenido
) {}
