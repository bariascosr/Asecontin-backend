package com.asecontin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EstadoRequest(
		@NotBlank(message = "El nombre del estado es obligatorio")
		@Size(max = 50, message = "El nombre no puede superar 50 caracteres")
		String nombreEstado
) {}
