package com.asecontin.backend.repository;

import com.asecontin.backend.entity.TipoInmueble;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TipoInmuebleRepository extends R2dbcRepository<TipoInmueble, Long> {

	Mono<Boolean> existsById(Long id);
}
