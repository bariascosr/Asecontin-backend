package com.asecontin.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request para actualizar el perfil del administrador.
 * Si se envía nuevaPassword, passwordActual es obligatorio.
 */
public record ActualizarUsuarioRequest(
		@Size(max = 100, message = "El nombre no puede superar 100 caracteres")
		String nombre,

		@Email(message = "Email no válido")
		@Size(max = 150)
		String email,

		/** Requerido solo si se desea cambiar la contraseña. */
		String passwordActual,

		@Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
		String nuevaPassword
) {
	/** Indica si se está solicitando un cambio de contraseña. */
	public boolean isCambioPassword() {
		return nuevaPassword != null && !nuevaPassword.isBlank();
	}
}
