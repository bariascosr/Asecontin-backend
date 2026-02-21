package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Video;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VideoRepository extends R2dbcRepository<Video, Long> {

	Flux<Video> findByInmuebleIdOrderByIdVideo(Long inmuebleId);

	Mono<Boolean> existsByInmuebleIdAndIdVideo(Long inmuebleId, Long idVideo);

	Mono<Long> countByInmuebleId(Long inmuebleId);
}
