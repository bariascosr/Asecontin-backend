package com.asecontin.backend.dto;

import java.math.BigDecimal;

/**
 * Resumen de inmueble incluyendo valor de arriendo. Para consulta pública de propietario/arrendatario por cédula.
 */
public record InmuebleResumenConValorArriendo(
		Long id,
		String titulo,
		String direccion,
		String tipo,
		String ciudadNombre,
		String localidadNombre,
		/** Valor de arriendo en COP. Null si no aplica. */
		BigDecimal valorArriendo
) {}
