package com.asecontin.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Respuesta paginada estándar. Incluye la página de elementos y metadatos de paginación.
 *
 * @param content       lista de elementos de la página actual
 * @param totalElements total de elementos en el conjunto
 * @param totalPages    total de páginas
 * @param number        número de página actual (0-based)
 * @param size          tamaño de página
 * @param first         true si es la primera página
 * @param last          true si es la última página
 */
@Schema(description = "Respuesta paginada: content (lista de la página), totalElements, totalPages, number (página actual 0-based), size, first, last")
public record PageResponse<T>(
		@Schema(description = "Elementos de la página actual") List<T> content,
		@Schema(description = "Total de elementos en todo el conjunto") long totalElements,
		@Schema(description = "Total de páginas") int totalPages,
		@Schema(description = "Número de página actual (0-based)") int number,
		@Schema(description = "Tamaño de página") int size,
		@Schema(description = "True si es la primera página") boolean first,
		@Schema(description = "True si es la última página") boolean last
) {
	public static <T> PageResponse<T> of(List<T> content, long totalElements, int number, int size) {
		int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
		return new PageResponse<>(
				content,
				totalElements,
				totalPages,
				number,
				size,
				number == 0,
				number >= totalPages - 1 || totalPages == 0
		);
	}
}
