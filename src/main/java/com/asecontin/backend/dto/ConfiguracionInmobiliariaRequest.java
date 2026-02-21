package com.asecontin.backend.dto;

import jakarta.validation.constraints.Size;

public record ConfiguracionInmobiliariaRequest(
		@Size(max = 5000)
		String mision,
		@Size(max = 5000)
		String vision,
		@Size(max = 10000)
		String terminosCondiciones,
		@Size(max = 10000)
		String politicaTratamientoDatos,
		@Size(max = 5000)
		String descripcion
) {}
