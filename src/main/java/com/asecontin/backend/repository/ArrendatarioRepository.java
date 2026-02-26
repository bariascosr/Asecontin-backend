package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Arrendatario;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ArrendatarioRepository extends R2dbcRepository<Arrendatario, Long> {

	Flux<Arrendatario> findByCedula(String cedula);

	Flux<Arrendatario> findByFechaExpedicion(LocalDate fechaExpedicion);

	Flux<Arrendatario> findByCedulaAndFechaExpedicion(String cedula, LocalDate fechaExpedicion);
}
