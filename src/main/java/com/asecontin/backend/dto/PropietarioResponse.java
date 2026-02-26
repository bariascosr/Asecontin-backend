package com.asecontin.backend.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta de propietario. inmuebles se llena en listado admin, por ID, por cédula o por fecha de expedición (admin y público).
 * En admin incluye toda la información de cada inmueble (InmuebleResponse).
 */
public record PropietarioResponse(
		Long id,
		String nombres,
		String apellidos,
		String cedula,
		LocalDate fechaExpedicion,
		/** Listado de inmuebles asociados con información completa. Null si no se solicitó. */
		List<InmuebleResponse> inmuebles
) {}
