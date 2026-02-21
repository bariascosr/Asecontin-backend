package com.asecontin.backend.dto;

import java.time.LocalDateTime;

public record ConfiguracionInmobiliariaResponse(
		Long id,
		String mision,
		String vision,
		String terminosCondiciones,
		String politicaTratamientoDatos,
		String descripcion,
		LocalDateTime fechaCreacion,
		LocalDateTime fechaActualizacion
) {}
