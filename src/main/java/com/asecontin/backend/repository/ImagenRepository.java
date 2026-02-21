package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Imagen;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ImagenRepository extends R2dbcRepository<Imagen, Long> {

	Flux<Imagen> findByInmuebleIdOrderByIdImagen(Long inmuebleId);

	/** Primera imagen marcada como principal del inmueble (para listados). */
	Flux<Imagen> findByInmuebleIdAndEsPrincipalTrue(Long inmuebleId);

	Mono<Boolean> existsByInmuebleIdAndIdImagen(Long inmuebleId, Long idImagen);

	Mono<Long> countByInmuebleId(Long inmuebleId);
}
