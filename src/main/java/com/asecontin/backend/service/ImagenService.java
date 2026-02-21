package com.asecontin.backend.service;

import com.asecontin.backend.config.MediaProperties;
import com.asecontin.backend.dto.ImagenRequest;
import com.asecontin.backend.dto.ImagenResponse;
import com.asecontin.backend.entity.Imagen;
import com.asecontin.backend.repository.ImagenRepository;
import com.asecontin.backend.repository.InmuebleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Service
public class ImagenService {

	private static final int MAX_IMAGENES_POR_INMUEBLE = 7;
	private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
	private static final Logger log = LoggerFactory.getLogger(ImagenService.class);

	private final ImagenRepository imagenRepository;
	private final InmuebleRepository inmuebleRepository;
	private final MediaStorageService mediaStorageService;
	private final MediaProperties mediaProperties;

	public ImagenService(ImagenRepository imagenRepository, InmuebleRepository inmuebleRepository,
			MediaStorageService mediaStorageService, MediaProperties mediaProperties) {
		this.imagenRepository = imagenRepository;
		this.inmuebleRepository = inmuebleRepository;
		this.mediaStorageService = mediaStorageService;
		this.mediaProperties = mediaProperties;
	}

	public Flux<ImagenResponse> listarPorInmueble(Long inmuebleId) {
		log.debug("Listando imágenes del inmueble {}", inmuebleId);
		return inmuebleRepository.findById(inmuebleId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Inmueble no encontrado")))
				.flatMapMany(i -> imagenRepository.findByInmuebleIdOrderByIdImagen(inmuebleId))
				.map(this::toResponse);
	}

	public Mono<ImagenResponse> crear(Long inmuebleId, ImagenRequest request) {
		boolean comoPrincipal = Boolean.TRUE.equals(request.esPrincipal());
		return inmuebleRepository.findById(inmuebleId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Inmueble no encontrado")))
				.flatMap(i -> imagenRepository.countByInmuebleId(inmuebleId)
						.flatMap(count -> {
							if (count >= MAX_IMAGENES_POR_INMUEBLE) {
								log.warn("Límite de imágenes alcanzado para inmueble {} (máx. {})", inmuebleId, MAX_IMAGENES_POR_INMUEBLE);
								return Mono.error(new IllegalStateException("Este inmueble ya tiene el máximo de " + MAX_IMAGENES_POR_INMUEBLE + " imágenes. Elimine alguna para agregar otra."));
							}
							Mono<Void> desmarcarOtras = comoPrincipal
									? imagenRepository.findByInmuebleIdOrderByIdImagen(inmuebleId)
											.flatMap(img -> { img.setEsPrincipal(false); return imagenRepository.save(img); })
											.then()
									: Mono.empty();
							return desmarcarOtras.then(Mono.defer(() -> {
								Imagen img = new Imagen();
								img.setInmuebleId(inmuebleId);
								img.setUrlImagen(request.urlImagen().trim());
								img.setEsPrincipal(comoPrincipal);
								return imagenRepository.save(img).map(this::toResponse);
							}));
						}))
				.doOnSuccess(r -> log.info("Imagen {} agregada al inmueble {} (principal: {})", r != null ? r.id() : null, inmuebleId, comoPrincipal));
	}

	/**
	 * Sube un archivo de imagen y la asocia al inmueble. Guarda en disco y registra la ruta en BD.
	 * La respuesta incluye la URL pública para usar en el front (ej. /api/public/medios/imagen/{id}).
	 */
	public Mono<ImagenResponse> crearFromFile(Long inmuebleId, FilePart file, boolean esPrincipal) {
		return inmuebleRepository.findById(inmuebleId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Inmueble no encontrado")))
				.flatMap(i -> imagenRepository.countByInmuebleId(inmuebleId)
						.flatMap(count -> {
							if (count >= MAX_IMAGENES_POR_INMUEBLE) {
								return Mono.error(new IllegalStateException("Este inmueble ya tiene el máximo de " + MAX_IMAGENES_POR_INMUEBLE + " imágenes. Elimine alguna para agregar otra."));
							}
							String subfolder = "imagenes/inmueble-" + inmuebleId;
							return mediaStorageService.save(file, subfolder, ALLOWED_IMAGE_EXTENSIONS);
						}))
				.flatMap(relativePath -> {
					Mono<Void> desmarcarOtras = esPrincipal
							? imagenRepository.findByInmuebleIdOrderByIdImagen(inmuebleId)
									.flatMap(img -> { img.setEsPrincipal(false); return imagenRepository.save(img); })
									.then()
							: Mono.empty();
					return desmarcarOtras.then(Mono.defer(() -> {
						Imagen img = new Imagen();
						img.setInmuebleId(inmuebleId);
						img.setUrlImagen(relativePath);
						img.setEsPrincipal(esPrincipal);
						return imagenRepository.save(img).map(this::toResponse);
					}));
				})
				.doOnSuccess(r -> log.info("Imagen {} subida para inmueble {} (principal: {})", r != null ? r.id() : null, inmuebleId, esPrincipal));
	}

	/**
	 * Sube hasta 7 imágenes en una sola petición. Opcional: esPrincipalIndex (0-based) indica cuál será la imagen principal.
	 * Devuelve la lista de imágenes creadas con sus URLs públicas.
	 */
	public Mono<List<ImagenResponse>> crearFromFiles(Long inmuebleId, List<FilePart> files, Integer esPrincipalIndex) {
		if (files == null || files.isEmpty()) {
			return Mono.error(new IllegalArgumentException("Debe enviar al menos una imagen"));
		}
		if (files.size() > MAX_IMAGENES_POR_INMUEBLE) {
			return Mono.error(new IllegalArgumentException("Máximo " + MAX_IMAGENES_POR_INMUEBLE + " imágenes por petición"));
		}
		int principalIndex = esPrincipalIndex != null && esPrincipalIndex >= 0 && esPrincipalIndex < files.size() ? esPrincipalIndex : -1;
		return inmuebleRepository.findById(inmuebleId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Inmueble no encontrado")))
				.flatMap(i -> imagenRepository.countByInmuebleId(inmuebleId)
						.flatMap(count -> {
							long total = count + files.size();
							if (total > MAX_IMAGENES_POR_INMUEBLE) {
								return Mono.error(new IllegalStateException("Este inmueble tiene " + count + " imagen(es). Con " + files.size() + " más superaría el máximo de " + MAX_IMAGENES_POR_INMUEBLE + ". Suba como máximo " + (MAX_IMAGENES_POR_INMUEBLE - count) + " imagen(es)."));
							}
							Mono<Void> desmarcar = principalIndex >= 0
									? imagenRepository.findByInmuebleIdOrderByIdImagen(inmuebleId)
											.flatMap(img -> { img.setEsPrincipal(false); return imagenRepository.save(img); })
											.then()
									: Mono.empty();
							String subfolder = "imagenes/inmueble-" + inmuebleId;
							return desmarcar.then(Flux.fromIterable(files)
									.index()
									.flatMap(tuple -> {
										long idx = tuple.getT1();
										FilePart file = tuple.getT2();
										boolean principal = (principalIndex >= 0 && idx == principalIndex);
										return mediaStorageService.save(file, subfolder, ALLOWED_IMAGE_EXTENSIONS)
												.flatMap(relativePath -> {
													Imagen img = new Imagen();
													img.setInmuebleId(inmuebleId);
													img.setUrlImagen(relativePath);
													img.setEsPrincipal(principal);
													return imagenRepository.save(img).map(this::toResponse);
												});
									})
									.collectList());
						}))
				.doOnSuccess(list -> log.info("{} imagen(es) subidas para inmueble {} (principalIndex: {})", list != null ? list.size() : 0, inmuebleId, principalIndex >= 0 ? principalIndex : "ninguna"));
	}

	public Mono<ImagenResponse> marcarComoPrincipal(Long inmuebleId, Long imagenId) {
		return imagenRepository.existsByInmuebleIdAndIdImagen(inmuebleId, imagenId)
				.flatMap(existe -> {
					if (Boolean.FALSE.equals(existe)) {
						log.warn("Marcar principal rechazado: imagen {} no pertenece al inmueble {}", imagenId, inmuebleId);
						return Mono.error(new IllegalArgumentException("Imagen no encontrada o no pertenece al inmueble"));
					}
					return imagenRepository.findByInmuebleIdOrderByIdImagen(inmuebleId)
							.flatMap(img -> { img.setEsPrincipal(img.getIdImagen().equals(imagenId)); return imagenRepository.save(img); })
							.filter(img -> imagenId.equals(img.getIdImagen()))
							.next()
							.map(this::toResponse);
				})
				.doOnNext(r -> log.info("Imagen {} marcada como principal del inmueble {}", imagenId, inmuebleId));
	}

	public Mono<Void> eliminar(Long inmuebleId, Long imagenId) {
		return imagenRepository.existsByInmuebleIdAndIdImagen(inmuebleId, imagenId)
				.flatMap(existe -> {
					if (Boolean.FALSE.equals(existe)) {
						log.warn("Eliminación de imagen {} rechazada: no pertenece al inmueble {}", imagenId, inmuebleId);
						return Mono.error(new IllegalArgumentException("Imagen no encontrada o no pertenece al inmueble"));
					}
					return imagenRepository.findById(imagenId)
							.flatMap(imagen -> {
								String path = imagen.getUrlImagen();
								Mono<Void> deleteFile = (path != null && !path.startsWith("http"))
										? mediaStorageService.delete(path).then()
										: Mono.empty();
								return deleteFile.then(imagenRepository.deleteById(imagenId));
							})
							.doOnSuccess(v -> log.info("Imagen {} eliminada del inmueble {}", imagenId, inmuebleId));
				});
	}

	/** URL pública para esta imagen (para usar en &lt;img src&gt;). Si está en disco, devuelve baseUrl + /api/public/medios/imagen/id; si es URL externa, la devuelve tal cual. */
	public String getPublicUrl(Imagen i) {
		if (i == null) return null;
		String url = i.getUrlImagen();
		if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
			return url;
		}
		String base = mediaProperties.getBaseUrlNormalized();
		return (base != null ? base : "") + "/api/public/medios/imagen/" + i.getIdImagen();
	}

	private ImagenResponse toResponse(Imagen i) {
		return new ImagenResponse(i.getIdImagen(), i.getInmuebleId(), getPublicUrl(i), i.getEsPrincipal());
	}
}
