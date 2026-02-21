package com.asecontin.backend.controller;

import com.asecontin.backend.repository.ImagenRepository;
import com.asecontin.backend.repository.VideoRepository;
import com.asecontin.backend.service.MediaStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Locale;

/**
 * Sirve archivos de imágenes y videos almacenados en disco (Opción A).
 * Rutas públicas; no requieren JWT. Las URLs son las que devuelve la API en listados y detalle.
 */
@RestController
@RequestMapping("/api/public/medios")
@Tag(name = "Medios públicos", description = "Obtener imagen o video por ID (para usar en img/video src)")
public class PublicMediosController {

	private final ImagenRepository imagenRepository;
	private final VideoRepository videoRepository;
	private final MediaStorageService mediaStorageService;

	public PublicMediosController(ImagenRepository imagenRepository, VideoRepository videoRepository,
			MediaStorageService mediaStorageService) {
		this.imagenRepository = imagenRepository;
		this.videoRepository = videoRepository;
		this.mediaStorageService = mediaStorageService;
	}

	@GetMapping("/imagen/{id}")
	@Operation(summary = "Obtener imagen por ID", description = "Devuelve el archivo de imagen. Usar la URL en <img src=\"...\">.")
	public Mono<ResponseEntity<Resource>> servirImagen(@PathVariable Long id) {
		return imagenRepository.findById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Imagen no encontrada")))
				.flatMap(imagen -> serveIfLocal(imagen.getUrlImagen(), id, true));
	}

	@GetMapping("/video/{id}")
	@Operation(summary = "Obtener video por ID", description = "Devuelve el archivo de video. Usar la URL en <video src=\"...\">.")
	public Mono<ResponseEntity<Resource>> servirVideo(@PathVariable Long id) {
		return videoRepository.findById(id)
				.switchIfEmpty(Mono.error(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Video no encontrado")))
				.flatMap(video -> serveIfLocal(video.getUrlVideo(), id, false));
	}

	private Mono<ResponseEntity<Resource>> serveIfLocal(String pathOrUrl, Long id, boolean isImage) {
		if (pathOrUrl == null || pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
			return Mono.error(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,
					isImage ? "Imagen alojada externamente; no se sirve desde este servidor" : "Video alojado externamente; no se sirve desde este servidor"));
		}
		return mediaStorageService.getResource(pathOrUrl)
				.map(resource -> ResponseEntity.ok()
						.contentType(mediaTypeFromPath(pathOrUrl))
						.header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
						.body(resource))
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage())));
	}

	private static MediaType mediaTypeFromPath(String path) {
		if (path == null) return MediaType.APPLICATION_OCTET_STREAM;
		String lower = path.toLowerCase(Locale.ROOT);
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
		if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
		if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
		if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
		if (lower.endsWith(".mp4")) return MediaType.parseMediaType("video/mp4");
		if (lower.endsWith(".webm")) return MediaType.parseMediaType("video/webm");
		if (lower.endsWith(".mov")) return MediaType.parseMediaType("video/quicktime");
		if (lower.endsWith(".avi")) return MediaType.parseMediaType("video/x-msvideo");
		if (lower.endsWith(".mkv")) return MediaType.parseMediaType("video/x-matroska");
		if (lower.endsWith(".m4v")) return MediaType.parseMediaType("video/x-m4v");
		if (lower.endsWith(".ogv")) return MediaType.parseMediaType("video/ogg");
		if (lower.endsWith(".3gp")) return MediaType.parseMediaType("video/3gpp");
		return MediaType.APPLICATION_OCTET_STREAM;
	}
}
