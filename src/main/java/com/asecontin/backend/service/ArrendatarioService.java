package com.asecontin.backend.service;

import com.asecontin.backend.dto.ArrendatarioRequest;
import com.asecontin.backend.dto.ArrendatarioResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.entity.Arrendatario;
import com.asecontin.backend.repository.ArrendatarioRepository;
import com.asecontin.backend.repository.EstadoRepository;
import com.asecontin.backend.repository.InmuebleArrendatarioRepository;
import com.asecontin.backend.repository.InmuebleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ArrendatarioService {

	private static final Logger log = LoggerFactory.getLogger(ArrendatarioService.class);

	private final ArrendatarioRepository arrendatarioRepository;
	private final InmuebleArrendatarioRepository inmuebleArrendatarioRepository;
	private final InmuebleRepository inmuebleRepository;
	private final InmuebleService inmuebleService;
	private final EstadoRepository estadoRepository;

	public ArrendatarioService(ArrendatarioRepository arrendatarioRepository,
			InmuebleArrendatarioRepository inmuebleArrendatarioRepository,
			InmuebleRepository inmuebleRepository,
			InmuebleService inmuebleService,
			EstadoRepository estadoRepository) {
		this.arrendatarioRepository = arrendatarioRepository;
		this.inmuebleArrendatarioRepository = inmuebleArrendatarioRepository;
		this.inmuebleRepository = inmuebleRepository;
		this.inmuebleService = inmuebleService;
		this.estadoRepository = estadoRepository;
	}

	public Mono<PageResponse<ArrendatarioResponse>> listar(int page, int size) {
		int safeSize = Math.min(Math.max(1, size), 100);
		int safePage = Math.max(0, page);
		Flux<Arrendatario> flux = arrendatarioRepository.findAll(Sort.by("apellidos", "nombres"));
		Mono<List<ArrendatarioResponse>> content = flux
				.skip((long) safePage * safeSize)
				.take(safeSize)
				.flatMap(this::conInmuebles)
				.collectList();
		Mono<Long> total = arrendatarioRepository.count();
		return Mono.zip(content, total).map(t -> PageResponse.of(t.getT1(), t.getT2(), safePage, safeSize));
	}

	public Mono<ArrendatarioResponse> obtenerPorId(Long id) {
		return arrendatarioRepository.findById(id)
				.flatMap(this::conInmuebles)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Arrendatario no encontrado: id={}", id);
					return Mono.error(new IllegalArgumentException("Arrendatario no encontrado"));
				}));
	}

	/** Primer arrendatario que coincida con la cédula (para consulta pública). */
	public Mono<ArrendatarioResponse> obtenerPorCedula(String cedula) {
		return arrendatarioRepository.findByCedula(cedula != null ? cedula.trim() : "")
				.next()
				.flatMap(this::conInmuebles)
				.switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("Arrendatario no encontrado con cédula: " + cedula))));
	}

	public Flux<ArrendatarioResponse> listarPorFechaExpedicion(java.time.LocalDate fechaExpedicion) {
		return arrendatarioRepository.findByFechaExpedicion(fechaExpedicion)
				.flatMap(this::conInmuebles);
	}

	/** Consulta pública: por cédula y fecha de expedición (ambos requeridos). Solo devuelve inmuebles en estado "Arrendado". */
	public Mono<ArrendatarioResponse> obtenerPorCedulaYFechaExpedicion(String cedula, java.time.LocalDate fechaExpedicion) {
		return arrendatarioRepository.findByCedulaAndFechaExpedicion(cedula != null ? cedula.trim() : "", fechaExpedicion)
				.next()
				.flatMap(this::conInmueblesSoloArrendados)
				.switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("Arrendatario no encontrado con cédula y fecha de expedición indicados"))));
	}

	public Mono<ArrendatarioResponse> crear(ArrendatarioRequest request) {
		if (request.inmuebleIds() == null || request.inmuebleIds().isEmpty()) {
			return Mono.error(new IllegalArgumentException("Debe indicar al menos un inmueble (inmuebleIds)"));
		}
		Arrendatario a = new Arrendatario();
		a.setNombres(request.nombres().trim());
		a.setApellidos(request.apellidos().trim());
		a.setCedula(request.cedula().trim());
		a.setFechaExpedicion(request.fechaExpedicion());
		if (request.telefono() != null && !request.telefono().isBlank()) {
			a.setTelefono(request.telefono().trim());
		}
		return arrendatarioRepository.save(a)
				.flatMap(saved -> asociarInmueblesSiPresente(saved, request.inmuebleIds()))
				.flatMap(this::conInmuebles);
	}

	private Mono<Arrendatario> asociarInmueblesSiPresente(Arrendatario arrendatario, java.util.List<Long> inmuebleIds) {
		if (inmuebleIds == null || inmuebleIds.isEmpty()) {
			return Mono.just(arrendatario);
		}
		Long arrendatarioId = arrendatario.getIdArrendatario();
		return Flux.fromIterable(inmuebleIds)
				.flatMap(inmuebleId -> inmuebleRepository.findById(inmuebleId)
						.switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("Inmueble no encontrado: " + inmuebleId))))
						.then(inmuebleArrendatarioRepository.asociar(inmuebleId, arrendatarioId)))
				.then(Mono.just(arrendatario));
	}

	public Mono<ArrendatarioResponse> actualizar(Long id, ArrendatarioRequest request) {
		return arrendatarioRepository.findById(id)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Arrendatario no encontrado")))
				.flatMap(a -> {
					a.setNombres(request.nombres().trim());
					a.setApellidos(request.apellidos().trim());
					a.setCedula(request.cedula().trim());
					a.setFechaExpedicion(request.fechaExpedicion());
					a.setTelefono(request.telefono() != null && !request.telefono().isBlank() ? request.telefono().trim() : null);
					return arrendatarioRepository.save(a).map(this::toResponseSinInmuebles);
				});
	}

	public Mono<Void> eliminar(Long id) {
		return arrendatarioRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("Arrendatario no encontrado"))))
				.flatMap(arrendatarioRepository::delete)
				.doOnSuccess(v -> log.info("Arrendatario eliminado: id={}", id));
	}

	public Mono<Void> asociarInmueble(Long arrendatarioId, Long inmuebleId) {
		return arrendatarioRepository.findById(arrendatarioId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Arrendatario no encontrado")))
				.then(inmuebleRepository.findById(inmuebleId).switchIfEmpty(Mono.error(new IllegalArgumentException("Inmueble no encontrado"))))
				.flatMap(i -> inmuebleArrendatarioRepository.asociar(inmuebleId, arrendatarioId));
	}

	public Mono<Void> desasociarInmueble(Long arrendatarioId, Long inmuebleId) {
		return inmuebleArrendatarioRepository.desasociar(inmuebleId, arrendatarioId);
	}

	private Mono<ArrendatarioResponse> conInmuebles(Arrendatario a) {
		return inmuebleArrendatarioRepository.findInmuebleIdsByArrendatarioId(a.getIdArrendatario())
				.flatMap(inmuebleRepository::findById)
				.flatMap(inmuebleService::toResponseFromInmueble)
				.collectList()
				.map(list -> new ArrendatarioResponse(
						a.getIdArrendatario(),
						a.getNombres(),
						a.getApellidos(),
						a.getCedula(),
						a.getFechaExpedicion(),
						a.getTelefono(),
						list.isEmpty() ? null : list
				));
	}

	/** Solo inmuebles en estado "Arrendado" (consulta pública para arrendatario). */
	private Mono<ArrendatarioResponse> conInmueblesSoloArrendados(Arrendatario a) {
		return estadoRepository.findFirstByNombreEstadoIgnoreCase("Arrendado")
				.flatMap(estado -> {
					Long arrendadoId = estado.getIdEstado();
					return inmuebleArrendatarioRepository.findInmuebleIdsByArrendatarioId(a.getIdArrendatario())
							.flatMap(inmuebleRepository::findById)
							.filter(inmueble -> arrendadoId.equals(inmueble.getEstadoId()))
							.flatMap(inmuebleService::toResponseFromInmueble)
							.collectList()
							.map(list -> new ArrendatarioResponse(
									a.getIdArrendatario(),
									a.getNombres(),
									a.getApellidos(),
									a.getCedula(),
									a.getFechaExpedicion(),
									a.getTelefono(),
									list.isEmpty() ? null : list
							));
				})
				.switchIfEmpty(Mono.fromCallable(() -> new ArrendatarioResponse(
						a.getIdArrendatario(), a.getNombres(), a.getApellidos(), a.getCedula(), a.getFechaExpedicion(), a.getTelefono(), null)));
	}

	private ArrendatarioResponse toResponseSinInmuebles(Arrendatario a) {
		return new ArrendatarioResponse(a.getIdArrendatario(), a.getNombres(), a.getApellidos(), a.getCedula(), a.getFechaExpedicion(), a.getTelefono(), null);
	}
}
