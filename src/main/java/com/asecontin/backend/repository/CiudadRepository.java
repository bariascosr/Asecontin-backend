package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Ciudad;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CiudadRepository extends R2dbcRepository<Ciudad, Long> {

	Mono<Boolean> existsByNombreIgnoreCase(String nombre);
}
