package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Inmueble;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Consultas dinámicas de inmuebles con filtros opcionales (incluye rangos).
 * estadoIdsIn: cuando no es null ni vacío, filtra por estado_id IN (lista). Si es vacío y estadoId está presente, se usa estadoId.
 */
public interface InmuebleRepositoryCustom {

	Flux<Inmueble> findByFilters(
			Optional<Long> estadoId,
			List<Long> estadoIdsIn,
			Optional<Long> localidadId,
			Optional<Long> tipoId,
			Optional<BigDecimal> precioMin,
			Optional<BigDecimal> precioMax,
			Optional<BigDecimal> areaMin,
			Optional<BigDecimal> areaMax,
			Optional<Integer> habitacionesMin,
			Optional<Integer> habitacionesMax,
			Optional<Integer> banosMin,
			Optional<Integer> banosMax,
			Optional<Integer> estratoMin,
			Optional<Integer> estratoMax,
			Optional<Integer> parqueaderosMin,
			Optional<Integer> parqueaderosMax,
			Sort sort);

	Mono<Long> countByFilters(
			Optional<Long> estadoId,
			List<Long> estadoIdsIn,
			Optional<Long> localidadId,
			Optional<Long> tipoId,
			Optional<BigDecimal> precioMin,
			Optional<BigDecimal> precioMax,
			Optional<BigDecimal> areaMin,
			Optional<BigDecimal> areaMax,
			Optional<Integer> habitacionesMin,
			Optional<Integer> habitacionesMax,
			Optional<Integer> banosMin,
			Optional<Integer> banosMax,
			Optional<Integer> estratoMin,
			Optional<Integer> estratoMax,
			Optional<Integer> parqueaderosMin,
			Optional<Integer> parqueaderosMax);
}
