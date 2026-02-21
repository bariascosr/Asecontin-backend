package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Estado;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface EstadoRepository extends R2dbcRepository<Estado, Long> {

	Mono<Boolean> existsByNombreEstadoIgnoreCase(String nombreEstado);

	Mono<Boolean> existsByNombreEstadoIgnoreCaseAndIdEstadoNot(String nombreEstado, Long idExcluir);
}
