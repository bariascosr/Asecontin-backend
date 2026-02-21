package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Sector;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SectorRepository extends R2dbcRepository<Sector, Long> {

	Mono<Boolean> existsById(Long id);
}
