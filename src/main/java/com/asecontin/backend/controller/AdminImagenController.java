package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.ImagenRequest;
import com.asecontin.backend.dto.ImagenResponse;
import com.asecontin.backend.dto.ImagenUpdateRequest;
import com.asecontin.backend.service.ImagenService;
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
 * CRUD de imágenes por inmueble. Las URLs apuntan a almacenamiento externo (S3/GCP). Requiere JWT.
 */
@RestController
@RequestMapping("/api/admin/inmuebles/{inmuebleId}/imagenes")
@Tag(name = "Imágenes por inmueble", description = "Agregar y listar imágenes (URLs) de un inmueble")
@SecurityRequirement(name = "bearerAuth")
public class AdminImagenController {

	private final ImagenService imagenService;

	public AdminImagenController(ImagenService imagenService) {
		this.imagenService = imagenService;
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Listar imágenes del inmueble")
	public Mono<ApiResponse<List<ImagenResponse>>> listar(@PathVariable Long inmuebleId) {
		return imagenService.listarPorInmueble(inmuebleId)
				.collectList()
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Agregar imagen por URL", description = "Registra la URL de una imagen externa. Máximo 7 por inmueble. Opcional: esPrincipal=true para usarla como imagen de muestra en listados.")
	public Mono<ApiResponse<ImagenResponse>> crear(@PathVariable Long inmuebleId, @Valid @RequestBody ImagenRequest request) {
		return imagenService.crear(inmuebleId, request)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Subir una imagen", description = "Sube un archivo de imagen (multipart). Partes: 'file' (archivo), 'esPrincipal' (opcional, true/false). Formatos: jpg, png, gif, webp.")
	public Mono<ApiResponse<ImagenResponse>> subir(@PathVariable Long inmuebleId,
			@RequestPart("file") FilePart file,
			@RequestPart(value = "esPrincipal", required = false) String esPrincipal) {
		boolean principal = "true".equalsIgnoreCase(esPrincipal != null ? esPrincipal.trim() : "");
		return imagenService.crearFromFile(inmuebleId, file, principal)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@PostMapping(value = "/upload-batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Subir hasta 7 imágenes a la vez", description = "Sube varias imágenes en una sola petición. Partes: 'files' (varios archivos, mismo nombre), 'esPrincipalIndex' (opcional, índice 0-based de cuál será la imagen principal). Formatos: jpg, png, gif, webp. Respuesta: lista de imágenes creadas con sus URLs públicas.")
	public Mono<ApiResponse<List<ImagenResponse>>> subirVarias(@PathVariable Long inmuebleId,
			@RequestPart("files") List<FilePart> files,
			@RequestPart(value = "esPrincipalIndex", required = false) String esPrincipalIndex) {
		Integer principalIndex = null;
		if (esPrincipalIndex != null && !esPrincipalIndex.isBlank()) {
			try {
				principalIndex = Integer.parseInt(esPrincipalIndex.trim());
			} catch (NumberFormatException ignored) { }
		}
		return imagenService.crearFromFiles(inmuebleId, files, principalIndex)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage())))
				.onErrorResume(IllegalStateException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage())));
	}

	@PutMapping(value = "/{imagenId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Marcar imagen como principal", description = "Si esPrincipal es true, esta imagen será la de muestra en listados. Solo una por inmueble.")
	public Mono<ApiResponse<ImagenResponse>> actualizar(@PathVariable Long inmuebleId, @PathVariable Long imagenId, @RequestBody ImagenUpdateRequest request) {
		if (request == null || !Boolean.TRUE.equals(request.esPrincipal())) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permite esPrincipal: true para marcar esta imagen como principal"));
		}
		return imagenService.marcarComoPrincipal(inmuebleId, imagenId)
				.map(ApiResponse::success)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}

	@DeleteMapping("/{imagenId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Eliminar imagen")
	public Mono<Void> eliminar(@PathVariable Long inmuebleId, @PathVariable Long imagenId) {
		return imagenService.eliminar(inmuebleId, imagenId)
				.onErrorResume(IllegalArgumentException.class, e -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage())));
	}
}
