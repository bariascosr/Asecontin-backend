package com.asecontin.backend.service;

import com.asecontin.backend.dto.EstadoRequest;
import com.asecontin.backend.dto.EstadoResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.entity.Estado;
import com.asecontin.backend.repository.EstadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class EstadoService {

	private static final Logger log = LoggerFactory.getLogger(EstadoService.class);

	private final EstadoRepository estadoRepository;

	public EstadoService(EstadoRepository estadoRepository) {
		this.estadoRepository = estadoRepository;
	}

	public Mono<PageResponse<EstadoResponse>> listar(int page, int size) {
		log.debug("Listando estados: page={}, size={}", page, size);
		var flux = estadoRepository.findAll(Sort.by("nombreEstado")).map(this::toResponse);
		Mono<List<EstadoResponse>> content = flux.skip((long) page * size).take(size).collectList();
		Mono<Long> total = estadoRepository.count();
		return Mono.zip(content, total).map(t -> PageResponse.of(t.getT1(), t.getT2(), page, size));
	}

	/** Lista todos los estados ordenados por nombre (para catálogo público sin paginación). */
	public Flux<EstadoResponse> listarTodos() {
		return estadoRepository.findAll(Sort.by("nombreEstado")).map(this::toResponse);
	}

	public Mono<EstadoResponse> obtenerPorId(Long id) {
		return estadoRepository.findById(id)
				.map(this::toResponse)
				.doOnNext(r -> log.debug("Estado obtenido: id={}", id))
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Estado no encontrado: id={}", id);
					return Mono.error(new IllegalArgumentException("Estado no encontrado"));
				}));
	}

	public Mono<EstadoResponse> crear(EstadoRequest request) {
		log.debug("Creando estado: {}", request.nombreEstado());
		return estadoRepository.existsByNombreEstadoIgnoreCase(request.nombreEstado().trim())
				.flatMap(existe -> {
					if (Boolean.TRUE.equals(existe)) {
						log.warn("Estado no creado: ya existe nombre '{}'", request.nombreEstado());
						return Mono.error(new IllegalStateException("Ya existe un estado con ese nombre"));
					}
					Estado estado = new Estado();
					estado.setNombreEstado(request.nombreEstado().trim());
					return estadoRepository.save(estado).map(this::toResponse)
							.doOnNext(r -> log.info("Estado creado: id={}, nombre={}", r.id(), r.nombreEstado()));
				});
	}

	public Mono<EstadoResponse> actualizar(Long id, EstadoRequest request) {
		return estadoRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Estado no encontrado para actualizar: id={}", id);
					return Mono.error(new IllegalArgumentException("Estado no encontrado"));
				}))
				.flatMap(estado -> estadoRepository
						.existsByNombreEstadoIgnoreCaseAndIdEstadoNot(request.nombreEstado().trim(), id)
						.flatMap(existe -> {
							if (Boolean.TRUE.equals(existe)) {
								log.warn("Estado no actualizado: nombre duplicado '{}'", request.nombreEstado());
								return Mono.error(new IllegalStateException("Ya existe otro estado con ese nombre"));
							}
							estado.setNombreEstado(request.nombreEstado().trim());
							return estadoRepository.save(estado).map(this::toResponse)
									.doOnNext(r -> log.info("Estado actualizado: id={}", id));
						}));
	}

	public Mono<Void> eliminar(Long id) {
		return estadoRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Estado no encontrado para eliminar: id={}", id);
					return Mono.error(new IllegalArgumentException("Estado no encontrado"));
				}))
				.flatMap(estadoRepository::delete)
				.doOnSuccess(v -> log.info("Estado eliminado: id={}", id))
				.onErrorMap(org.springframework.dao.DataIntegrityViolationException.class,
						e -> {
							log.warn("No se puede eliminar estado id={}: en uso por inmuebles", id);
							return new IllegalStateException("No se puede eliminar el estado porque está en uso por uno o más inmuebles");
						});
	}

	private EstadoResponse toResponse(Estado e) {
		return new EstadoResponse(e.getIdEstado(), e.getNombreEstado());
	}
}
