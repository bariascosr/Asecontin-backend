package com.asecontin.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record InmuebleRequest(
		@NotBlank(message = "El título es obligatorio")
		@Size(max = 150)
		String titulo,

		@Size(max = 5000)
		String descripcion,

		@NotNull(message = "El precio es obligatorio")
		@DecimalMin(value = "0", inclusive = false, message = "El precio debe ser mayor que 0")
		@Digits(integer = 12, fraction = 2)
		BigDecimal precio,

		@NotBlank(message = "La dirección es obligatoria")
		@Size(max = 255)
		String direccion,

		@NotNull(message = "La localidad es obligatoria (use GET /api/public/localidades para la lista)")
		Long localidadId,

		@NotNull(message = "El tipo de inmueble es obligatorio (use GET /api/public/tipos-inmueble para la lista)")
		Long tipoId,

		@NotNull(message = "El estado del inmueble es obligatorio")
		Long estadoId,

		@Size(max = 100)
		String etiquetas,

		@Min(0)
		Integer parqueaderos,

		Long sectorId,

		@DecimalMin("0")
		@Digits(integer = 8, fraction = 2)
		BigDecimal areaM2,

		@Min(0)
		Integer habitaciones,

		@Min(0)
		Integer banos,

		@Min(1) @Max(6)
		Integer estrato,

		@DecimalMin("0")
		@Digits(integer = 10, fraction = 2)
		BigDecimal valorAdministracion,

		@Min(1900) @Max(2100)
		Integer anoConstruccion,

		Boolean amoblado,

		@Min(0)
		Integer piso
) {}
