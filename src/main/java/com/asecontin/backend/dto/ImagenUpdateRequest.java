package com.asecontin.backend.dto;

/**
 * Request para actualizar una imagen (por ahora solo marcar como principal).
 */
public record ImagenUpdateRequest(
		/** Si true, esta imagen pasa a ser la imagen principal del inmueble. */
		Boolean esPrincipal
) {}
