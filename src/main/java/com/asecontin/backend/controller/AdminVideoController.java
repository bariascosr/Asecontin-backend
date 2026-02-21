package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.VideoRequest;
import com.asecontin.backend.dto.VideoResponse;
import com.asecontin.backend.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * CRUD de videos por inmueble. Las URLs apuntan a almacenamiento externo (S3/GCP). Requiere JWT.
 */
@RestController
@RequestMapping("/api/admin/inmuebles/{inmuebleId}/videos")
@Tag(name = "Videos por inmueble", description = "Agregar y listar videos (URLs) de un inmueble")
@SecurityRequirement(name = "bearerAuth")
public class AdminVideoController {

	private final VideoService videoService;

	public AdminVideoController(VideoService videoService) {
		this.videoService = videoService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar videos del inmueble")
	public Mono<ApiResponse<List<VideoResponse>>> listar(@PathVariable Long inmuebleId) {
		return videoService.listarPorInmueble(inmuebleId)
				.collectList()
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Agregar video por URL", description = "Registra la URL de un video externo. Máximo 1 video por inmueble.")
	public Mono<ApiResponse<VideoResponse>> crear(@PathVariable Long inmuebleId, @Valid @RequestBody VideoRequest request) {
		return videoService.crear(inmuebleId, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Subir video", description = "Sube un archivo de video (multipart). Parte: 'file'. Formatos: mp4, webm, mov, avi, mkv, m4v, ogv, 3gp. Máximo 1 video por inmueble. La respuesta incluye la URL pública.")
	public Mono<ApiResponse<VideoResponse>> subir(@PathVariable Long inmuebleId, @RequestPart("file") FilePart file) {
		return videoService.crearFromFile(inmuebleId, file)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@DeleteMapping("/{videoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar video")
	public Mono<Void> eliminar(@PathVariable Long inmuebleId, @PathVariable Long videoId) {
		return videoService.eliminar(inmuebleId, videoId)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
