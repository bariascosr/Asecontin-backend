package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Inmueble;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface InmuebleRepository extends R2dbcRepository<Inmueble, Long>, InmuebleRepositoryCustom {

	Flux<Inmueble> findByEstadoId(Long estadoId);

	Mono<Long> countByEstadoId(Long estadoId);

	Flux<Inmueble> findByLocalidadId(Long localidadId);

	Mono<Long> countByLocalidadId(Long localidadId);

	Flux<Inmueble> findByTipoId(Long tipoId);

	Mono<Long> countByTipoId(Long tipoId);

	Flux<Inmueble> findByPrecioBetween(BigDecimal minPrecio, BigDecimal maxPrecio);

	Mono<Long> countByPrecioBetween(BigDecimal minPrecio, BigDecimal maxPrecio);
}
