package com.asecontin.backend.repository;

import com.asecontin.backend.entity.ConfiguracionInmobiliaria;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ConfiguracionInmobiliariaRepository extends R2dbcRepository<ConfiguracionInmobiliaria, Long> {

	Mono<ConfiguracionInmobiliaria> findFirstByOrderByIdConfigAsc();
}
