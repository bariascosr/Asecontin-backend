package com.asecontin.backend.service;

import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.dto.PropietarioRequest;
import com.asecontin.backend.dto.PropietarioResponse;
import com.asecontin.backend.entity.Propietario;
import com.asecontin.backend.repository.CiudadRepository;
import com.asecontin.backend.repository.EstadoRepository;
import com.asecontin.backend.repository.InmuebleRepository;
import com.asecontin.backend.repository.LocalidadRepository;
import com.asecontin.backend.repository.PropietarioRepository;
import com.asecontin.backend.repository.TipoInmuebleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class PropietarioService {

	private static final Logger log = LoggerFactory.getLogger(PropietarioService.class);

	private final PropietarioRepository propietarioRepository;
	private final InmuebleRepository inmuebleRepository;
	private final InmuebleService inmuebleService;
	private final EstadoRepository estadoRepository;
	private final TipoInmuebleRepository tipoInmuebleRepository;
	private final LocalidadRepository localidadRepository;
	private final CiudadRepository ciudadRepository;

	public PropietarioService(PropietarioRepository propietarioRepository, InmuebleRepository inmuebleRepository,
			InmuebleService inmuebleService, EstadoRepository estadoRepository, TipoInmuebleRepository tipoInmuebleRepository,
			LocalidadRepository localidadRepository, CiudadRepository ciudadRepository) {
		this.propietarioRepository = propietarioRepository;
		this.inmuebleRepository = inmuebleRepository;
		this.inmuebleService = inmuebleService;
		this.estadoRepository = estadoRepository;
		this.tipoInmuebleRepository = tipoInmuebleRepository;
		this.localidadRepository = localidadRepository;
		this.ciudadRepository = ciudadRepository;
	}

	public Mono<PageResponse<PropietarioResponse>> listar(int page, int size) {
		int safeSize = Math.min(Math.max(1, size), 100);
		int safePage = Math.max(0, page);
		Flux<Propietario> flux = propietarioRepository.findAll(Sort.by("apellidos", "nombres"));
		Mono<List<PropietarioResponse>> content = flux
				.skip((long) safePage * safeSize)
				.take(safeSize)
				.flatMap(this::conInmuebles)
				.collectList();
		Mono<Long> total = propietarioRepository.count();
		return Mono.zip(content, total).map(t -> PageResponse.of(t.getT1(), t.getT2(), safePage, safeSize));
	}

	public Mono<PropietarioResponse> obtenerPorId(Long id) {
		return propietarioRepository.findById(id)
				.flatMap(p -> conInmuebles(p))
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Propietario no encontrado: id={}", id);
					return Mono.error(new IllegalArgumentException("Propietario no encontrado"));
				}));
	}

	public Mono<PropietarioResponse> obtenerPorCedula(String cedula) {
		return propietarioRepository.findByCedula(cedula != null ? cedula.trim() : "")
				.flatMap(this::conInmuebles)
				.switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("Propietario no encontrado con cédula: " + cedula))));
	}

	public Flux<PropietarioResponse> listarPorFechaExpedicion(java.time.LocalDate fechaExpedicion) {
		return propietarioRepository.findByFechaExpedicion(fechaExpedicion)
				.flatMap(this::conInmuebles);
	}

	/** Consulta pública: por cédula y fecha de expedición (ambos requeridos). Devuelve todos los inmuebles asociados al propietario. */
	public Mono<PropietarioResponse> obtenerPorCedulaYFechaExpedicion(String cedula, java.time.LocalDate fechaExpedicion) {
		return propietarioRepository.findByCedulaAndFechaExpedicion(cedula != null ? cedula.trim() : "", fechaExpedicion)
				.flatMap(this::conInmuebles)
				.switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("Propietario no encontrado con cédula y fecha de expedición indicados"))));
	}

	public Mono<PropietarioResponse> crear(PropietarioRequest request) {
		if (request.inmuebleIds() == null || request.inmuebleIds().isEmpty()) {
			return Mono.error(new IllegalArgumentException("Debe indicar al menos un inmueble (inmuebleIds)"));
		}
		return propietarioRepository.findByCedula(request.cedula().trim())
				.hasElement()
				.flatMap(exists -> {
					if (Boolean.TRUE.equals(exists)) {
						return Mono.<Propietario>error(new IllegalStateException("Ya existe un propietario con esa cédula"));
					}
					Propietario p = new Propietario();
					p.setNombres(request.nombres().trim());
					p.setApellidos(request.apellidos().trim());
					p.setCedula(request.cedula().trim());
					p.setFechaExpedicion(request.fechaExpedicion());
					return propietarioRepository.save(p);
				})
				.flatMap(saved -> asignarInmueblesSiPresente(saved, request.inmuebleIds()))
				.flatMap(this::conInmuebles);
	}

	private Mono<Propietario> asignarInmueblesSiPresente(Propietario propietario, java.util.List<Long> inmuebleIds) {
		if (inmuebleIds == null || inmuebleIds.isEmpty()) {
			return Mono.just(propietario);
		}
		Long propietarioId = propietario.getIdPropietario();
		return Flux.fromIterable(inmuebleIds)
				.flatMap(id -> inmuebleRepository.findById(id)
						.switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("Inmueble no encontrado: " + id))))
						.doOnNext(inm -> inm.setPropietarioId(propietarioId))
						.flatMap(inmuebleRepository::save))
				.then(Mono.just(propietario));
	}

	public Mono<PropietarioResponse> actualizar(Long id, PropietarioRequest request) {
		return propietarioRepository.findById(id)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Propietario no encontrado")))
				.flatMap(existing -> {
					if (!existing.getCedula().equals(request.cedula().trim())) {
						return propietarioRepository.findByCedula(request.cedula().trim()).hasElement()
								.flatMap(dup -> dup ? Mono.<Propietario>error(new IllegalStateException("Ya existe otro propietario con esa cédula")) : Mono.just(existing));
					}
					return Mono.just(existing);
				})
				.flatMap(p -> {
					p.setNombres(request.nombres().trim());
					p.setApellidos(request.apellidos().trim());
					p.setCedula(request.cedula().trim());
					p.setFechaExpedicion(request.fechaExpedicion());
					return propietarioRepository.save(p).map(this::toResponseSinInmuebles);
				});
	}

	public Mono<Void> eliminar(Long id) {
		return propietarioRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> Mono.error(new IllegalArgumentException("Propietario no encontrado"))))
				.flatMap(propietarioRepository::delete)
				.doOnSuccess(v -> log.info("Propietario eliminado: id={}", id));
	}

	private Mono<PropietarioResponse> conInmuebles(Propietario p) {
		return inmuebleRepository.findByPropietarioId(p.getIdPropietario())
				.flatMap(inmuebleService::toResponseFromInmueble)
				.collectList()
				.map(list -> new PropietarioResponse(
						p.getIdPropietario(),
						p.getNombres(),
						p.getApellidos(),
						p.getCedula(),
						p.getFechaExpedicion(),
						list
				));
	}

	/** Solo inmuebles en estado "Arrendado" (consulta pública para propietario). Siempre devuelve respuesta con lista (vacía si no hay estado o no hay inmuebles). */
	private Mono<PropietarioResponse> conInmueblesArrendados(Propietario p) {
		return estadoRepository.findFirstByNombreEstadoIgnoreCase("Arrendado")
				.flatMap(estado -> inmuebleRepository.findByPropietarioIdAndEstadoId(p.getIdPropietario(), estado.getIdEstado())
						.flatMap(inmuebleService::toResponseFromInmueble)
						.collectList())
				.defaultIfEmpty(Collections.emptyList())
				.map(list -> new PropietarioResponse(
						p.getIdPropietario(),
						p.getNombres(),
						p.getApellidos(),
						p.getCedula(),
						p.getFechaExpedicion(),
						list
				));
	}

	private PropietarioResponse toResponseSinInmuebles(Propietario p) {
		return new PropietarioResponse(p.getIdPropietario(), p.getNombres(), p.getApellidos(), p.getCedula(), p.getFechaExpedicion(), null);
	}

}
