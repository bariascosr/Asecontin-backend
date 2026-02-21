package com.asecontin.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Formato estándar de respuesta exitosa del servidor.
 *
 * @param success siempre true en respuestas exitosas
 * @param data    payload de la respuesta
 * @param message mensaje opcional (ej. "Inmueble creado correctamente")
 */
@Schema(description = "Respuesta estándar de éxito: success=true, data con el payload y message opcional")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
		@Schema(description = "Siempre true en respuestas exitosas", example = "true") boolean success,
		@Schema(description = "Datos de la respuesta (objeto o lista según el endpoint)") T data,
		@Schema(description = "Mensaje opcional", example = "Inmueble creado correctamente") String message
) {
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, data, null);
	}

	public static <T> ApiResponse<T> success(T data, String message) {
		return new ApiResponse<>(true, data, message);
	}
}
