package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Localidad;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LocalidadRepository extends R2dbcRepository<Localidad, Long> {

	Mono<Boolean> existsByNombreIgnoreCaseAndCiudadId(String nombre, Long ciudadId);

	Mono<Boolean> existsByNombreIgnoreCaseAndCiudadIdAndIdLocalidadNot(String nombre, Long ciudadId, Long idExcluir);

	Flux<Localidad> findByCiudadIdOrderByNombre(Long ciudadId);
}
