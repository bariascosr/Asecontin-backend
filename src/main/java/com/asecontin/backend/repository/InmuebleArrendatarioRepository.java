package com.asecontin.backend.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Operaciones sobre la tabla de enlace imb.inmueble_arrendatario (N:M entre inmueble y arrendatario).
 */
public interface InmuebleArrendatarioRepository {

	Flux<Long> findInmuebleIdsByArrendatarioId(Long arrendatarioId);

	Mono<Void> asociar(Long inmuebleId, Long arrendatarioId);

	Mono<Void> desasociar(Long inmuebleId, Long arrendatarioId);

	Mono<Boolean> existsByInmuebleIdAndArrendatarioId(Long inmuebleId, Long arrendatarioId);
}
