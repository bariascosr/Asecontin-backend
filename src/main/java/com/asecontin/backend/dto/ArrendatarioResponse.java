package com.asecontin.backend.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta de arrendatario. inmuebles se llena en listado admin, por ID, por cédula o por fecha de expedición (admin y público).
 * En admin incluye toda la información de cada inmueble (InmuebleResponse). En público solo inmuebles en estado Arrendado.
 */
public record ArrendatarioResponse(
		Long id,
		String nombres,
		String apellidos,
		String cedula,
		LocalDate fechaExpedicion,
		/** Teléfono para notificaciones (ej. WhatsApp). Opcional. */
		String telefono,
		/** Listado de inmuebles asociados con información completa. Null si no se solicitó. */
		List<InmuebleResponse> inmuebles
) {}
