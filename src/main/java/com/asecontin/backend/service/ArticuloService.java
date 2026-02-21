package com.asecontin.backend.service;

import com.asecontin.backend.dto.ArticuloRequest;
import com.asecontin.backend.dto.ArticuloResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.entity.Articulo;
import com.asecontin.backend.repository.ArticuloRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticuloService {

	private static final Logger log = LoggerFactory.getLogger(ArticuloService.class);

	private final ArticuloRepository articuloRepository;

	public ArticuloService(ArticuloRepository articuloRepository) {
		this.articuloRepository = articuloRepository;
	}

	public Mono<PageResponse<ArticuloResponse>> listar(int page, int size) {
		log.debug("Listando artículos: page={}, size={}", page, size);
		var flux = articuloRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaPublicacion")).map(this::toResponse);
		Mono<List<ArticuloResponse>> content = flux.skip((long) page * size).take(size).collectList();
		Mono<Long> total = articuloRepository.count();
		return Mono.zip(content, total).map(t -> PageResponse.of(t.getT1(), t.getT2(), page, size));
	}

	public Mono<ArticuloResponse> obtenerPorId(Long id) {
		return articuloRepository.findById(id)
				.map(this::toResponse)
				.doOnNext(r -> log.debug("Artículo obtenido: id={}", id))
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Artículo no encontrado: id={}", id);
					return Mono.error(new IllegalArgumentException("Artículo no encontrado"));
				}));
	}

	public Mono<ArticuloResponse> crear(ArticuloRequest request) {
		log.debug("Creando artículo: titulo={}", request.titulo());
		Articulo a = new Articulo();
		a.setTitulo(request.titulo().trim());
		a.setContenido(request.contenido().trim());
		a.setFechaPublicacion(LocalDateTime.now());
		return articuloRepository.save(a).map(this::toResponse)
				.doOnNext(r -> log.info("Artículo creado: id={}, titulo={}", r.id(), r.titulo()));
	}

	public Mono<ArticuloResponse> actualizar(Long id, ArticuloRequest request) {
		return articuloRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Artículo no encontrado para actualizar: id={}", id);
					return Mono.error(new IllegalArgumentException("Artículo no encontrado"));
				}))
				.flatMap(a -> {
					a.setTitulo(request.titulo().trim());
					a.setContenido(request.contenido().trim());
					return articuloRepository.save(a).map(this::toResponse)
							.doOnNext(r -> log.info("Artículo actualizado: id={}", id));
				});
	}

	public Mono<Void> eliminar(Long id) {
		return articuloRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Artículo no encontrado para eliminar: id={}", id);
					return Mono.error(new IllegalArgumentException("Artículo no encontrado"));
				}))
				.flatMap(articuloRepository::delete)
				.doOnSuccess(v -> log.info("Artículo eliminado: id={}", id));
	}

	private ArticuloResponse toResponse(Articulo a) {
		return new ArticuloResponse(a.getIdArticulo(), a.getTitulo(), a.getContenido(), a.getFechaPublicacion());
	}
}
