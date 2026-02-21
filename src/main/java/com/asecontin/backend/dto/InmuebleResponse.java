package com.asecontin.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InmuebleResponse(
		Long id,
		String titulo,
		String descripcion,
		BigDecimal precio,
		String direccion,
		Long localidadId,
		String localidadNombre,
		String ciudadNombre,
		String tipo,
		Long estadoId,
		String estadoNombre,
		String etiquetas,
		Integer parqueaderos,
		Long sectorId,
		String sectorNombre,
		BigDecimal areaM2,
		Integer habitaciones,
		Integer banos,
		Integer estrato,
		BigDecimal valorAdministracion,
		Integer anoConstruccion,
		Boolean amoblado,
		Integer piso,
		LocalDateTime fechaCreacion,
		LocalDateTime fechaActualizacion,
		/** URL de la imagen principal (de muestra en listados). Null si no hay imagen marcada como principal. */
		String imagenPrincipal
) {}
