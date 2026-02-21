package com.asecontin.backend.service;

import com.asecontin.backend.config.MediaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.UUID;

/**
 * Guarda archivos en disco bajo app.media.upload-dir y los sirve por path relativo.
 * Pensado para imágenes y videos subidos por multipart.
 */
@Service
public class MediaStorageService {

	private static final Logger log = LoggerFactory.getLogger(MediaStorageService.class);

	private final Path uploadRoot;

	public MediaStorageService(MediaProperties mediaProperties) {
		String dir = mediaProperties.uploadDir() != null ? mediaProperties.uploadDir() : "uploads";
		this.uploadRoot = Paths.get(dir).toAbsolutePath().normalize();
	}

	/**
	 * Guarda el archivo en subfolder con nombre único (UUID + extensión).
	 * @param filePart parte multipart del request
	 * @param subfolder ej. "imagenes/inmueble-1"
	 * @param allowedExtensions ej. Set.of("jpg", "jpeg", "png", "gif", "webp")
	 * @return path relativo para guardar en BD, ej. "imagenes/inmueble-1/abc-uuid.jpg"
	 */
	public Mono<String> save(FilePart filePart, String subfolder, Set<String> allowedExtensions) {
		String originalFilename = filePart.filename();
		if (originalFilename == null || originalFilename.isBlank()) {
			return Mono.error(new IllegalArgumentException("Nombre de archivo vacío"));
		}
		String ext = null;
		int lastDot = originalFilename.lastIndexOf('.');
		if (lastDot >= 0 && lastDot < originalFilename.length() - 1) {
			ext = originalFilename.substring(lastDot + 1).toLowerCase();
		}
		if (ext == null || !allowedExtensions.contains(ext)) {
			return Mono.error(new IllegalArgumentException("Extensión no permitida. Permitidas: " + allowedExtensions));
		}
		String filename = UUID.randomUUID().toString() + "." + ext;
		String relativePath = subfolder + "/" + filename;
		Path fullPath = uploadRoot.resolve(relativePath).normalize();
		if (!fullPath.startsWith(uploadRoot)) {
			return Mono.error(new IllegalArgumentException("Path no permitido"));
		}
		return Mono.fromCallable(() -> {
					Files.createDirectories(fullPath.getParent());
					return Files.newOutputStream(fullPath, StandardOpenOption.CREATE_NEW);
				})
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(out -> Flux.using(
								() -> out,
								outputStream -> DataBufferUtils.write(filePart.content(), outputStream)
										.doOnNext(DataBufferUtils::release),
								MediaStorageService::closeQuietly)
						.subscribeOn(Schedulers.boundedElastic())
						.then(Mono.just(relativePath)))
				.doOnSuccess(r -> log.debug("Archivo guardado: {}", relativePath));
	}

	/**
	 * Obtiene un Resource para servir el archivo. El path debe ser relativo y no contener "..".
	 */
	public Mono<Resource> getResource(String relativePath) {
		if (relativePath == null || relativePath.isBlank() || relativePath.contains("..")) {
			return Mono.error(new IllegalArgumentException("Path no válido"));
		}
		Path path = uploadRoot.resolve(relativePath).normalize();
		if (!path.startsWith(uploadRoot)) {
			return Mono.error(new IllegalArgumentException("Path no permitido"));
		}
		return Mono.fromCallable(() -> {
					if (!Files.isRegularFile(path)) {
						throw new IllegalArgumentException("Archivo no encontrado");
					}
					return (Resource) new org.springframework.core.io.FileSystemResource(path.toFile());
				})
				.subscribeOn(Schedulers.boundedElastic());
	}

	/**
	 * Elimina el archivo del disco si existe. Path relativo.
	 */
	public Mono<Void> delete(String relativePath) {
		if (relativePath == null || relativePath.isBlank() || relativePath.contains("..")) {
			return Mono.empty();
		}
		Path path = uploadRoot.resolve(relativePath).normalize();
		if (!path.startsWith(uploadRoot)) {
			return Mono.empty();
		}
		return Mono.fromRunnable(() -> {
					try {
						Files.deleteIfExists(path);
						log.debug("Archivo eliminado: {}", relativePath);
					} catch (Exception e) {
						log.warn("Error eliminando archivo {}: {}", relativePath, e.getMessage());
					}
				})
				.subscribeOn(Schedulers.boundedElastic())
				.then();
	}

	private static void closeQuietly(OutputStream out) {
		try {
			if (out != null) out.close();
		} catch (IOException e) {
			// ignore
		}
	}
}
