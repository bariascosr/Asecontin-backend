package com.asecontin.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Formato estándar de respuesta de error. Indica al cliente qué debe corregir (4xx) o error interno (5xx).
 *
 * @param success siempre false
 * @param message mensaje claro en español para el cliente
 * @param code    código de error opcional (ej. VALIDATION_ERROR, CONFLICT, NOT_FOUND)
 * @param errors  lista de errores por campo (validación), opcional
 */
@Schema(description = "Respuesta estándar de error: success=false, message y opcionalmente code y errors por campo")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
		@Schema(description = "Siempre false en errores", example = "false") boolean success,
		@Schema(description = "Mensaje claro para el cliente", example = "El email ya está en uso") String message,
		@Schema(description = "Código de error", example = "CONFLICT", allowableValues = {"BAD_REQUEST", "CONFLICT", "NOT_FOUND", "VALIDATION_ERROR", "UNAUTHORIZED", "INTERNAL_ERROR"}) String code,
		@Schema(description = "Errores por campo (validación)") List<CampoError> errors
) {
	public static ErrorResponse of(String message, String code) {
		return new ErrorResponse(false, message, code, null);
	}

	public static ErrorResponse of(String message, String code, List<CampoError> errors) {
		return new ErrorResponse(false, message, code, errors);
	}

	@Schema(description = "Error de validación en un campo")
	public record CampoError(
			@Schema(description = "Nombre del campo", example = "email") String campo,
			@Schema(description = "Mensaje de error", example = "no debe estar vacío") String mensaje
	) {}
}
