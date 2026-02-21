package com.asecontin.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para agregar una imagen (URL del almacenamiento externo).
 * Si esPrincipal es true, esta imagen será la de muestra en listados; cuenta dentro del máximo de 7.
 */
public record ImagenRequest(
		@NotBlank(message = "La URL de la imagen es obligatoria")
		@Size(max = 255)
		String urlImagen,

		/** Si true, se marca como imagen principal del inmueble (solo una por inmueble). */
		Boolean esPrincipal
) {}
