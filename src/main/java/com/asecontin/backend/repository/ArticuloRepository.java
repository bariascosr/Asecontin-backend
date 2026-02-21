package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Articulo;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ArticuloRepository extends R2dbcRepository<Articulo, Long> {}
