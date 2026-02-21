package com.asecontin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocalidadRequest(
		@NotBlank(message = "El nombre de la localidad es obligatorio")
		@Size(max = 100, message = "El nombre no puede superar 100 caracteres")
		String nombre,
		@NotNull(message = "La ciudad es obligatoria")
		Long ciudadId
) {}
