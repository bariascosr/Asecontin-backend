package com.asecontin.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalle de inmueble para API pública, incluyendo URLs de imágenes y videos.
 */
public record InmuebleDetallePublicoResponse(
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
		List<String> imagenes,
		List<String> videos
) {}
