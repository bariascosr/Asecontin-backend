package com.asecontin.backend.service;

import com.asecontin.backend.dto.ConfiguracionInmobiliariaRequest;
import com.asecontin.backend.dto.ConfiguracionInmobiliariaResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.entity.ConfiguracionInmobiliaria;
import com.asecontin.backend.repository.ConfiguracionInmobiliariaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConfiguracionInmobiliariaService {

	private static final Logger log = LoggerFactory.getLogger(ConfiguracionInmobiliariaService.class);

	private final ConfiguracionInmobiliariaRepository repository;

	public ConfiguracionInmobiliariaService(ConfiguracionInmobiliariaRepository repository) {
		this.repository = repository;
	}

	/** Obtiene la configuración actual (primera fila) para la sección pública "Acerca de nosotros". */
	public Mono<ConfiguracionInmobiliariaResponse> obtenerActual() {
		return repository.findFirstByOrderByIdConfigAsc()
				.map(this::toResponse)
				.doOnNext(r -> log.debug("Configuración institucional obtenida: id={}", r.id()))
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("No hay configuración institucional");
					return Mono.error(new IllegalArgumentException("Configuración no encontrada"));
				}));
	}

	public Mono<PageResponse<ConfiguracionInmobiliariaResponse>> listar(int page, int size) {
		Flux<ConfiguracionInmobiliaria> flux = repository.findAll(Sort.by("idConfig"));
		Mono<List<ConfiguracionInmobiliariaResponse>> content = flux
				.skip((long) page * size)
				.take(size)
				.map(this::toResponse)
				.collectList();
		Mono<Long> total = repository.count();
		return Mono.zip(content, total).map(t -> PageResponse.of(t.getT1(), t.getT2(), page, size));
	}

	public Mono<ConfiguracionInmobiliariaResponse> obtenerPorId(Long id) {
		return repository.findById(id)
				.map(this::toResponse)
				.doOnNext(r -> log.debug("Configuración obtenida: id={}", id))
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Configuración no encontrada: id={}", id);
					return Mono.error(new IllegalArgumentException("Configuración no encontrada"));
				}));
	}

	public Mono<ConfiguracionInmobiliariaResponse> crear(ConfiguracionInmobiliariaRequest request) {
		log.debug("Creando configuración institucional");
		ConfiguracionInmobiliaria entity = toEntity(request);
		LocalDateTime now = LocalDateTime.now();
		entity.setFechaCreacion(now);
		entity.setFechaActualizacion(now);
		return repository.save(entity)
				.map(this::toResponse)
				.doOnNext(r -> log.info("Configuración creada: id={}", r.id()));
	}

	public Mono<ConfiguracionInmobiliariaResponse> actualizar(Long id, ConfiguracionInmobiliariaRequest request) {
		return repository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Configuración no encontrada para actualizar: id={}", id);
					return Mono.error(new IllegalArgumentException("Configuración no encontrada"));
				}))
				.flatMap(existing -> {
					applyRequest(existing, request);
					existing.setFechaActualizacion(LocalDateTime.now());
					return repository.save(existing).map(this::toResponse)
							.doOnNext(r -> log.info("Configuración actualizada: id={}", id));
				});
	}

	public Mono<Void> eliminar(Long id) {
		return repository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Configuración no encontrada para eliminar: id={}", id);
					return Mono.error(new IllegalArgumentException("Configuración no encontrada"));
				}))
				.flatMap(repository::delete)
				.doOnSuccess(v -> log.info("Configuración eliminada: id={}", id));
	}

	private ConfiguracionInmobiliaria toEntity(ConfiguracionInmobiliariaRequest r) {
		ConfiguracionInmobiliaria e = new ConfiguracionInmobiliaria();
		applyRequest(e, r);
		return e;
	}

	private void applyRequest(ConfiguracionInmobiliaria e, ConfiguracionInmobiliariaRequest r) {
		e.setMision(r.mision() != null ? r.mision().trim() : null);
		e.setVision(r.vision() != null ? r.vision().trim() : null);
		e.setTerminosCondiciones(r.terminosCondiciones() != null ? r.terminosCondiciones().trim() : null);
		e.setPoliticaTratamientoDatos(r.politicaTratamientoDatos() != null ? r.politicaTratamientoDatos().trim() : null);
		e.setDescripcion(r.descripcion() != null ? r.descripcion().trim() : null);
	}

	private ConfiguracionInmobiliariaResponse toResponse(ConfiguracionInmobiliaria e) {
		return new ConfiguracionInmobiliariaResponse(
				e.getIdConfig(),
				e.getMision(),
				e.getVision(),
				e.getTerminosCondiciones(),
				e.getPoliticaTratamientoDatos(),
				e.getDescripcion(),
				e.getFechaCreacion(),
				e.getFechaActualizacion()
		);
	}
}
