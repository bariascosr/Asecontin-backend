package com.asecontin.backend.service;

import com.asecontin.backend.dto.LocalidadRequest;
import com.asecontin.backend.dto.LocalidadResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.entity.Ciudad;
import com.asecontin.backend.entity.Localidad;
import com.asecontin.backend.repository.CiudadRepository;
import com.asecontin.backend.repository.LocalidadRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
public class LocalidadService {

	private static final Logger log = LoggerFactory.getLogger(LocalidadService.class);

	private final LocalidadRepository localidadRepository;
	private final CiudadRepository ciudadRepository;

	public LocalidadService(LocalidadRepository localidadRepository, CiudadRepository ciudadRepository) {
		this.localidadRepository = localidadRepository;
		this.ciudadRepository = ciudadRepository;
	}

	/** Lista todas las localidades ordenadas por nombre (para selector público). */
	public Flux<LocalidadResponse> listarTodas() {
		return localidadRepository.findAll()
				.sort(Comparator.comparing(Localidad::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.flatMap(this::toResponse);
	}

	/** Lista localidades por ciudad (para filtros o selector por ciudad). */
	public Flux<LocalidadResponse> listarPorCiudad(Long ciudadId) {
		return localidadRepository.findByCiudadIdOrderByNombre(ciudadId)
				.flatMap(this::toResponse);
	}

	public Mono<PageResponse<LocalidadResponse>> listar(int page, int size) {
		Flux<Localidad> flux = localidadRepository.findAll(Sort.by("nombre"));
		Mono<List<LocalidadResponse>> content = flux
				.skip((long) page * size)
				.take(size)
				.flatMap(this::toResponse)
				.collectList();
		Mono<Long> total = localidadRepository.count();
		return Mono.zip(content, total).map(t -> PageResponse.of(t.getT1(), t.getT2(), page, size));
	}

	public Mono<LocalidadResponse> obtenerPorId(Long id) {
		return localidadRepository.findById(id)
				.flatMap(this::toResponse)
				.doOnNext(r -> log.debug("Localidad obtenida: id={}", id))
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Localidad no encontrada: id={}", id);
					return Mono.error(new IllegalArgumentException("Localidad no encontrada"));
				}));
	}

	public Mono<LocalidadResponse> crear(LocalidadRequest request) {
		log.debug("Creando localidad: {} (ciudadId={})", request.nombre(), request.ciudadId());
		return ciudadRepository.findById(request.ciudadId())
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Crear localidad: ciudad no encontrada id={}", request.ciudadId());
					return Mono.error(new IllegalArgumentException("Ciudad no encontrada"));
				}))
				.flatMap(ciudad -> localidadRepository
						.existsByNombreIgnoreCaseAndCiudadId(request.nombre().trim(), request.ciudadId())
						.flatMap(existe -> {
							if (Boolean.TRUE.equals(existe)) {
								log.warn("Localidad no creada: ya existe '{}' en esa ciudad", request.nombre());
								return Mono.error(new IllegalStateException("Ya existe una localidad con ese nombre en esa ciudad"));
							}
							Localidad loc = new Localidad();
							loc.setNombre(request.nombre().trim());
							loc.setCiudadId(request.ciudadId());
							return localidadRepository.save(loc).flatMap(this::toResponse)
									.doOnNext(r -> log.info("Localidad creada: id={}, nombre={}", r.id(), r.nombre()));
						}));
	}

	public Mono<LocalidadResponse> actualizar(Long id, LocalidadRequest request) {
		return localidadRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Localidad no encontrada para actualizar: id={}", id);
					return Mono.error(new IllegalArgumentException("Localidad no encontrada"));
				}))
				.flatMap(loc -> ciudadRepository.findById(request.ciudadId())
						.switchIfEmpty(Mono.defer(() -> {
							log.warn("Actualizar localidad: ciudad no encontrada id={}", request.ciudadId());
							return Mono.error(new IllegalArgumentException("Ciudad no encontrada"));
						}))
						.flatMap(ciudad -> localidadRepository
								.existsByNombreIgnoreCaseAndCiudadIdAndIdLocalidadNot(request.nombre().trim(), request.ciudadId(), id)
								.flatMap(existe -> {
									if (Boolean.TRUE.equals(existe)) {
										log.warn("Localidad no actualizada: nombre duplicado en esa ciudad");
										return Mono.error(new IllegalStateException("Ya existe otra localidad con ese nombre en esa ciudad"));
									}
									loc.setNombre(request.nombre().trim());
									loc.setCiudadId(request.ciudadId());
									return localidadRepository.save(loc).flatMap(this::toResponse)
											.doOnNext(r -> log.info("Localidad actualizada: id={}", id));
								})));
	}

	public Mono<Void> eliminar(Long id) {
		return localidadRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Localidad no encontrada para eliminar: id={}", id);
					return Mono.error(new IllegalArgumentException("Localidad no encontrada"));
				}))
				.flatMap(localidadRepository::delete)
				.doOnSuccess(v -> log.info("Localidad eliminada: id={}", id));
	}

	private Mono<LocalidadResponse> toResponse(Localidad loc) {
		Mono<String> ciudadNombre = ciudadRepository.findById(loc.getCiudadId())
				.map(Ciudad::getNombre)
				.defaultIfEmpty("");
		return ciudadNombre.map(nombre -> new LocalidadResponse(
				loc.getIdLocalidad(),
				loc.getNombre(),
				loc.getCiudadId(),
				nombre
		));
	}
}
