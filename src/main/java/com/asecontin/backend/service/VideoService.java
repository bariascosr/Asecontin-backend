package com.asecontin.backend.service;

import com.asecontin.backend.config.MediaProperties;
import com.asecontin.backend.dto.VideoRequest;
import com.asecontin.backend.dto.VideoResponse;
import com.asecontin.backend.entity.Video;
import com.asecontin.backend.repository.InmuebleRepository;
import com.asecontin.backend.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

@Service
public class VideoService {

	private static final int MAX_VIDEOS_POR_INMUEBLE = 1;
	private static final Set<String> ALLOWED_VIDEO_EXTENSIONS = Set.of(
			"mp4", "webm", "mov", "avi", "mkv", "m4v", "ogv", "3gp"
	);
	private static final Logger log = LoggerFactory.getLogger(VideoService.class);

	private final VideoRepository videoRepository;
	private final InmuebleRepository inmuebleRepository;
	private final MediaStorageService mediaStorageService;
	private final MediaProperties mediaProperties;

	public VideoService(VideoRepository videoRepository, InmuebleRepository inmuebleRepository,
			MediaStorageService mediaStorageService, MediaProperties mediaProperties) {
		this.videoRepository = videoRepository;
		this.inmuebleRepository = inmuebleRepository;
		this.mediaStorageService = mediaStorageService;
		this.mediaProperties = mediaProperties;
	}

	public Flux<VideoResponse> listarPorInmueble(Long inmuebleId) {
		log.debug("Listando videos del inmueble {}", inmuebleId);
		return inmuebleRepository.findById(inmuebleId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Inmueble no encontrado")))
				.flatMapMany(v -> videoRepository.findByInmuebleIdOrderByIdVideo(inmuebleId))
				.map(this::toResponse);
	}

	public Mono<VideoResponse> crear(Long inmuebleId, VideoRequest request) {
		return inmuebleRepository.findById(inmuebleId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Inmueble no encontrado")))
				.flatMap(i -> videoRepository.countByInmuebleId(inmuebleId)
						.flatMap(count -> {
							if (count >= MAX_VIDEOS_POR_INMUEBLE) {
								log.warn("Límite de videos alcanzado para inmueble {} (máx. {})", inmuebleId, MAX_VIDEOS_POR_INMUEBLE);
								return Mono.error(new IllegalStateException("Este inmueble ya tiene el máximo de " + MAX_VIDEOS_POR_INMUEBLE + " video(s). Elimine el existente para agregar otro."));
							}
							Video vid = new Video();
							vid.setInmuebleId(inmuebleId);
							vid.setUrlVideo(request.urlVideo().trim());
							return videoRepository.save(vid).map(this::toResponse);
						}))
				.doOnSuccess(r -> log.info("Video {} agregado al inmueble {}", r != null ? r.id() : null, inmuebleId));
	}

	/**
	 * Sube un archivo de video y lo asocia al inmueble. Guarda en disco y registra la ruta en BD.
	 * Máximo 1 video por inmueble. La respuesta incluye la URL pública.
	 */
	public Mono<VideoResponse> crearFromFile(Long inmuebleId, FilePart file) {
		return inmuebleRepository.findById(inmuebleId)
				.switchIfEmpty(Mono.error(new IllegalArgumentException("Inmueble no encontrado")))
				.flatMap(i -> videoRepository.countByInmuebleId(inmuebleId)
						.flatMap(count -> {
							if (count >= MAX_VIDEOS_POR_INMUEBLE) {
								return Mono.error(new IllegalStateException("Este inmueble ya tiene el máximo de " + MAX_VIDEOS_POR_INMUEBLE + " video(s). Elimine el existente para agregar otro."));
							}
							String subfolder = "videos/inmueble-" + inmuebleId;
							return mediaStorageService.save(file, subfolder, ALLOWED_VIDEO_EXTENSIONS);
						}))
				.flatMap(relativePath -> {
					Video vid = new Video();
					vid.setInmuebleId(inmuebleId);
					vid.setUrlVideo(relativePath);
					return videoRepository.save(vid).map(this::toResponse);
				})
				.doOnSuccess(r -> log.info("Video {} subido para inmueble {}", r != null ? r.id() : null, inmuebleId));
	}

	public Mono<Void> eliminar(Long inmuebleId, Long videoId) {
		return videoRepository.existsByInmuebleIdAndIdVideo(inmuebleId, videoId)
				.flatMap(existe -> {
					if (Boolean.FALSE.equals(existe)) {
						log.warn("Eliminación de video {} rechazada: no pertenece al inmueble {}", videoId, inmuebleId);
						return Mono.error(new IllegalArgumentException("Video no encontrado o no pertenece al inmueble"));
					}
					return videoRepository.findById(videoId)
							.flatMap(video -> {
								String path = video.getUrlVideo();
								Mono<Void> deleteFile = (path != null && !path.startsWith("http"))
										? mediaStorageService.delete(path).then()
										: Mono.empty();
								return deleteFile.then(videoRepository.deleteById(videoId));
							})
							.doOnSuccess(v -> log.info("Video {} eliminado del inmueble {}", videoId, inmuebleId));
				});
	}

	/** URL pública para este video. Si está en disco, baseUrl + /api/public/medios/video/id; si es URL externa, la devuelve tal cual. */
	public String getPublicUrl(Video v) {
		if (v == null) return null;
		String url = v.getUrlVideo();
		if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
			return url;
		}
		String base = mediaProperties.getBaseUrlNormalized();
		return (base != null ? base : "") + "/api/public/medios/video/" + v.getIdVideo();
	}

	private VideoResponse toResponse(Video v) {
		return new VideoResponse(v.getIdVideo(), v.getInmuebleId(), getPublicUrl(v));
	}
}
