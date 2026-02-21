package com.asecontin.backend.service;

import com.asecontin.backend.dto.InmuebleDetallePublicoResponse;
import com.asecontin.backend.dto.InmuebleRequest;
import com.asecontin.backend.dto.InmuebleResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.entity.Ciudad;
import com.asecontin.backend.entity.Estado;
import com.asecontin.backend.entity.Inmueble;
import com.asecontin.backend.entity.Localidad;
import com.asecontin.backend.entity.Sector;
import com.asecontin.backend.entity.TipoInmueble;
import com.asecontin.backend.repository.CiudadRepository;
import com.asecontin.backend.repository.EstadoRepository;
import com.asecontin.backend.repository.SectorRepository;
import com.asecontin.backend.repository.ImagenRepository;
import com.asecontin.backend.repository.LocalidadRepository;
import com.asecontin.backend.repository.InmuebleRepository;
import com.asecontin.backend.repository.TipoInmuebleRepository;
import com.asecontin.backend.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InmuebleService {

	private static final Logger log = LoggerFactory.getLogger(InmuebleService.class);

	private final InmuebleRepository inmuebleRepository;
	private final EstadoRepository estadoRepository;
	private final ImagenRepository imagenRepository;
	private final VideoRepository videoRepository;
	private final ImagenService imagenService;
	private final VideoService videoService;
	private final LocalidadRepository localidadRepository;
	private final CiudadRepository ciudadRepository;
	private final TipoInmuebleService tipoInmuebleService;
	private final TipoInmuebleRepository tipoInmuebleRepository;
	private final SectorRepository sectorRepository;

	public InmuebleService(InmuebleRepository inmuebleRepository, EstadoRepository estadoRepository,
			ImagenRepository imagenRepository, VideoRepository videoRepository,
			ImagenService imagenService, VideoService videoService,
			LocalidadRepository localidadRepository, CiudadRepository ciudadRepository,
			TipoInmuebleService tipoInmuebleService, TipoInmuebleRepository tipoInmuebleRepository,
			SectorRepository sectorRepository) {
		this.inmuebleRepository = inmuebleRepository;
		this.estadoRepository = estadoRepository;
		this.imagenRepository = imagenRepository;
		this.videoRepository = videoRepository;
		this.imagenService = imagenService;
		this.videoService = videoService;
		this.localidadRepository = localidadRepository;
		this.ciudadRepository = ciudadRepository;
		this.tipoInmuebleService = tipoInmuebleService;
		this.tipoInmuebleRepository = tipoInmuebleRepository;
		this.sectorRepository = sectorRepository;
	}

	public Mono<PageResponse<InmuebleResponse>> listar(
			Optional<Long> estadoId,
			Optional<Long> localidadId,
			Optional<Long> tipoId,
			Optional<BigDecimal> precioMin,
			Optional<BigDecimal> precioMax,
			Optional<BigDecimal> areaMin,
			Optional<BigDecimal> areaMax,
			Optional<Integer> habitacionesMin,
			Optional<Integer> habitacionesMax,
			Optional<Integer> banosMin,
			Optional<Integer> banosMax,
			Optional<Integer> estratoMin,
			Optional<Integer> estratoMax,
			Optional<Integer> parqueaderosMin,
			Optional<Integer> parqueaderosMax,
			int page,
			int size) {
		Sort sort = Sort.by(Sort.Direction.DESC, "fechaCreacion");
		Flux<Inmueble> flux = inmuebleRepository.findByFilters(
				estadoId, localidadId, tipoId, precioMin, precioMax,
				areaMin, areaMax, habitacionesMin, habitacionesMax,
				banosMin, banosMax, estratoMin, estratoMax,
				parqueaderosMin, parqueaderosMax, sort);
		Mono<Long> countMono = inmuebleRepository.countByFilters(
				estadoId, localidadId, tipoId, precioMin, precioMax,
				areaMin, areaMax, habitacionesMin, habitacionesMax,
				banosMin, banosMax, estratoMin, estratoMax,
				parqueaderosMin, parqueaderosMax);
		Mono<List<InmuebleResponse>> content = flux
				.skip((long) page * size)
				.take(size)
				.flatMap(this::toResponse)
				.collectList();
		return Mono.zip(content, countMono).map(t -> PageResponse.of(t.getT1(), t.getT2(), page, size));
	}

	public Mono<InmuebleResponse> obtenerPorId(Long id) {
		return inmuebleRepository.findById(id)
				.flatMap(this::toResponse)
				.doOnNext(r -> log.debug("Inmueble obtenido: id={}", id))
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Inmueble no encontrado: id={}", id);
					return Mono.error(new IllegalArgumentException("Inmueble no encontrado"));
				}));
	}

	/**
	 * Detalle para API pública: inmueble + listas de URLs de imágenes y videos.
	 */
	public Mono<InmuebleDetallePublicoResponse> obtenerDetallePublico(Long id) {
		return inmuebleRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Detalle público: inmueble no encontrado id={}", id);
					return Mono.error(new IllegalArgumentException("Inmueble no encontrado"));
				}))
				.flatMap(inmueble -> {
					Mono<InmuebleResponse> base = toResponse(inmueble);
					Mono<List<String>> imagenes = imagenRepository.findByInmuebleIdOrderByIdImagen(id)
							.map(imagenService::getPublicUrl)
							.collectList();
					Mono<List<String>> videos = videoRepository.findByInmuebleIdOrderByIdVideo(id)
							.map(videoService::getPublicUrl)
							.collectList();
					return Mono.zip(base, imagenes, videos)
							.map(t -> new InmuebleDetallePublicoResponse(
									t.getT1().id(), t.getT1().titulo(), t.getT1().descripcion(), t.getT1().precio(),
									t.getT1().direccion(), t.getT1().localidadId(), t.getT1().localidadNombre(), t.getT1().ciudadNombre(),
									t.getT1().tipo(), t.getT1().estadoId(),
									t.getT1().estadoNombre(), t.getT1().etiquetas(),
									t.getT1().parqueaderos(), t.getT1().sectorId(), t.getT1().sectorNombre(),
									t.getT1().areaM2(), t.getT1().habitaciones(), t.getT1().banos(), t.getT1().estrato(),
									t.getT1().valorAdministracion(), t.getT1().anoConstruccion(), t.getT1().amoblado(), t.getT1().piso(),
									t.getT1().fechaCreacion(), t.getT1().fechaActualizacion(), t.getT2(), t.getT3()));
				})
				.doOnNext(d -> log.debug("Detalle público obtenido: inmueble id={}", id));
	}

	public Mono<InmuebleResponse> crear(InmuebleRequest request) {
		log.debug("Creando inmueble: titulo={}", request.titulo());
		return estadoRepository.findById(request.estadoId())
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Crear inmueble: estado no encontrado id={}", request.estadoId());
					return Mono.error(new IllegalArgumentException("Estado no encontrado"));
				}))
				.flatMap(estado -> tipoInmuebleService.existePorId(request.tipoId())
						.flatMap(tipoExiste -> Boolean.FALSE.equals(tipoExiste)
								? Mono.<InmuebleResponse>error(new IllegalArgumentException("Tipo de inmueble no válido. Use GET /api/public/tipos-inmueble para la lista."))
								: localidadRepository.existsById(request.localidadId())))
				.flatMap(localidadExiste -> {
					if (Boolean.FALSE.equals(localidadExiste)) {
						log.warn("Crear inmueble: localidad no encontrada id={}", request.localidadId());
						return Mono.<InmuebleRequest>error(new IllegalArgumentException("Localidad no válida. Use GET /api/public/localidades para ver las localidades disponibles."));
					}
					if (request.sectorId() != null) {
						return sectorRepository.existsById(request.sectorId())
								.flatMap(existe -> Boolean.FALSE.equals(existe)
										? Mono.<InmuebleRequest>error(new IllegalArgumentException("Sector no válido. Use GET /api/public/sectores para la lista."))
										: Mono.just(request));
					}
					return Mono.just(request);
				})
				.flatMap(req -> {
					Inmueble inmueble = toEntity(req);
					LocalDateTime now = LocalDateTime.now();
					inmueble.setFechaCreacion(now);
					inmueble.setFechaActualizacion(now);
					return inmuebleRepository.save(inmueble).flatMap(this::toResponse)
							.doOnNext(r -> log.info("Inmueble creado: id={}, titulo={}", r.id(), r.titulo()));
				});
	}

	public Mono<InmuebleResponse> actualizar(Long id, InmuebleRequest request) {
		return inmuebleRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Inmueble no encontrado para actualizar: id={}", id);
					return Mono.error(new IllegalArgumentException("Inmueble no encontrado"));
				}))
				.flatMap(existing -> tipoInmuebleService.existePorId(request.tipoId())
						.flatMap(tipoOk -> Boolean.FALSE.equals(tipoOk)
								? Mono.<InmuebleResponse>error(new IllegalArgumentException("Tipo de inmueble no válido. Use GET /api/public/tipos-inmueble para la lista."))
								: localidadRepository.existsById(request.localidadId())))
				.flatMap(localidadOk -> {
					if (Boolean.FALSE.equals(localidadOk)) {
						log.warn("Actualizar inmueble: localidad no encontrada id={}", request.localidadId());
						return Mono.<InmuebleResponse>error(new IllegalArgumentException("Localidad no válida. Use GET /api/public/localidades para ver las localidades disponibles."));
					}
					if (request.sectorId() != null) {
						return sectorRepository.existsById(request.sectorId())
								.flatMap(existe -> Boolean.FALSE.equals(existe)
										? Mono.<InmuebleResponse>error(new IllegalArgumentException("Sector no válido. Use GET /api/public/sectores para la lista."))
										: Mono.just(true));
					}
					return Mono.just(true);
				})
				.flatMap(ignored -> inmuebleRepository.findById(id)
						.switchIfEmpty(Mono.defer(() -> {
							log.warn("Inmueble no encontrado para actualizar: id={}", id);
							return Mono.error(new IllegalArgumentException("Inmueble no encontrado"));
						}))
						.flatMap(existing -> estadoRepository.findById(request.estadoId())
						.switchIfEmpty(Mono.defer(() -> {
							log.warn("Actualizar inmueble: estado no encontrado id={}", request.estadoId());
							return Mono.error(new IllegalArgumentException("Estado no encontrado"));
						}))
						.flatMap(estado -> {
							existing.setTitulo(request.titulo().trim());
							existing.setDescripcion(request.descripcion() != null ? request.descripcion().trim() : null);
							existing.setPrecio(request.precio());
							existing.setDireccion(request.direccion().trim());
							existing.setLocalidadId(request.localidadId());
							existing.setTipoId(request.tipoId());
							existing.setEstadoId(request.estadoId());
							existing.setEtiquetas(request.etiquetas() != null ? request.etiquetas().trim() : null);
							existing.setParqueaderos(request.parqueaderos() != null ? request.parqueaderos() : 0);
							existing.setSectorId(request.sectorId());
							existing.setAreaM2(request.areaM2());
							existing.setHabitaciones(request.habitaciones());
							existing.setBanos(request.banos());
							existing.setEstrato(request.estrato());
							existing.setValorAdministracion(request.valorAdministracion());
							existing.setAnoConstruccion(request.anoConstruccion());
							existing.setAmoblado(request.amoblado() != null ? request.amoblado() : false);
							existing.setPiso(request.piso());
							existing.setFechaActualizacion(LocalDateTime.now());
							return inmuebleRepository.save(existing).flatMap(this::toResponse)
									.doOnNext(r -> log.info("Inmueble actualizado: id={}", id));
						})));
	}

	public Mono<Void> eliminar(Long id) {
		return inmuebleRepository.findById(id)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Inmueble no encontrado para eliminar: id={}", id);
					return Mono.error(new IllegalArgumentException("Inmueble no encontrado"));
				}))
				.flatMap(inmuebleRepository::delete)
				.doOnSuccess(v -> log.info("Inmueble eliminado: id={}", id));
	}

	private Inmueble toEntity(InmuebleRequest r) {
		Inmueble i = new Inmueble();
		i.setTitulo(r.titulo().trim());
		i.setDescripcion(r.descripcion() != null ? r.descripcion().trim() : null);
		i.setPrecio(r.precio());
		i.setDireccion(r.direccion().trim());
		i.setLocalidadId(r.localidadId());
		i.setTipoId(r.tipoId());
		i.setEstadoId(r.estadoId());
		i.setEtiquetas(r.etiquetas() != null ? r.etiquetas().trim() : null);
		i.setParqueaderos(r.parqueaderos() != null ? r.parqueaderos() : 0);
		i.setSectorId(r.sectorId());
		i.setAreaM2(r.areaM2());
		i.setHabitaciones(r.habitaciones());
		i.setBanos(r.banos());
		i.setEstrato(r.estrato());
		i.setValorAdministracion(r.valorAdministracion());
		i.setAnoConstruccion(r.anoConstruccion());
		i.setAmoblado(r.amoblado() != null ? r.amoblado() : false);
		i.setPiso(r.piso());
		return i;
	}

	private Mono<InmuebleResponse> toResponse(Inmueble i) {
		Mono<String> nombreEstado = estadoRepository.findById(i.getEstadoId())
				.map(Estado::getNombreEstado)
				.defaultIfEmpty("");
		Mono<String> nombreTipo = i.getTipoId() != null
				? tipoInmuebleRepository.findById(i.getTipoId()).map(TipoInmueble::getNombre).defaultIfEmpty("")
				: Mono.just("");
		Mono<String> nombreSector = i.getSectorId() != null
				? sectorRepository.findById(i.getSectorId()).map(Sector::getNombre).defaultIfEmpty("")
				: Mono.just("");
		Mono<String> imagenPrincipal = imagenRepository.findByInmuebleIdAndEsPrincipalTrue(i.getIdInmueble())
				.next()
				.map(imagenService::getPublicUrl)
				.defaultIfEmpty("");
		Mono<String> localidadNombre = i.getLocalidadId() != null
				? localidadRepository.findById(i.getLocalidadId()).map(Localidad::getNombre).defaultIfEmpty("")
				: Mono.just("");
		Mono<String> ciudadNombre = i.getLocalidadId() != null
				? localidadRepository.findById(i.getLocalidadId())
						.flatMap(loc -> ciudadRepository.findById(loc.getCiudadId()).map(Ciudad::getNombre))
						.defaultIfEmpty("")
				: Mono.just("");
		return Mono.zip(nombreEstado, nombreTipo, nombreSector, imagenPrincipal, localidadNombre, ciudadNombre)
				.map(t -> new InmuebleResponse(
						i.getIdInmueble(),
						i.getTitulo(),
						i.getDescripcion(),
						i.getPrecio(),
						i.getDireccion(),
						i.getLocalidadId(),
						t.getT5(),
						t.getT6(),
						t.getT2(),
						i.getEstadoId(),
						t.getT1(),
						i.getEtiquetas(),
						i.getParqueaderos() != null ? i.getParqueaderos() : 0,
						i.getSectorId(),
						t.getT3(),
						i.getAreaM2(),
						i.getHabitaciones(),
						i.getBanos(),
						i.getEstrato(),
						i.getValorAdministracion(),
						i.getAnoConstruccion(),
						i.getAmoblado() != null ? i.getAmoblado() : false,
						i.getPiso(),
						i.getFechaCreacion(),
						i.getFechaActualizacion(),
						t.getT4().isEmpty() ? null : t.getT4()
				));
	}
}
