package com.asecontin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para agregar un video (URL del almacenamiento externo).
 */
public record VideoRequest(
		@NotBlank(message = "La URL del video es obligatoria")
		@Size(max = 255)
		String urlVideo
) {}
