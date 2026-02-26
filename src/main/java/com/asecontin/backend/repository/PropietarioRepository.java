package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Propietario;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface PropietarioRepository extends R2dbcRepository<Propietario, Long> {

	Mono<Propietario> findByCedula(String cedula);

	Flux<Propietario> findByFechaExpedicion(LocalDate fechaExpedicion);

	Mono<Propietario> findByCedulaAndFechaExpedicion(String cedula, LocalDate fechaExpedicion);
}
