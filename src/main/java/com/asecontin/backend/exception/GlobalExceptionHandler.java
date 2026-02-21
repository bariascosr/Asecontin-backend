package com.asecontin.backend.exception;

import com.asecontin.backend.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejo global de excepciones: evita caídas del servicio y devuelve respuestas estándar al cliente.
 * Cualquier excepción no controlada se captura, se registra y se responde con 500.
 * Si la respuesta ya está comprometida (p. ej. tras empezar a enviar un video con 206), no se escribe
 * cuerpo de error para evitar HttpMessageNotWritableException.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private static final String ERROR_INTERNO = "Error interno del servidor. Intente más tarde.";

	private static Mono<ResponseEntity<ErrorResponse>> skipIfCommitted(ServerWebExchange exchange, Mono<ResponseEntity<ErrorResponse>> response) {
		if (exchange.getResponse().isCommitted()) {
			log.debug("Respuesta ya comprometida; no se escribe cuerpo de error.");
			return Mono.empty();
		}
		return response;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(IllegalArgumentException ex, ServerWebExchange exchange) {
		log.warn("Petición rechazada (cliente): {}", ex.getMessage());
		return skipIfCommitted(exchange, Mono.just(ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(ex.getMessage(), "BAD_REQUEST"))));
	}

	@ExceptionHandler(IllegalStateException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleIllegalState(IllegalStateException ex, ServerWebExchange exchange) {
		log.warn("Conflicto de negocio: {}", ex.getMessage());
		return skipIfCommitted(exchange, Mono.just(ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.of(ex.getMessage(), "CONFLICT"))));
	}

	@ExceptionHandler(WebExchangeBindException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleValidation(WebExchangeBindException ex, ServerWebExchange exchange) {
		List<ErrorResponse.CampoError> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> new ErrorResponse.CampoError(fe.getField(), fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido"))
				.collect(Collectors.toList());
		String message = "Errores de validación. Revise los campos indicados.";
		log.warn("Validación fallida: {}", errors);
		return skipIfCommitted(exchange, Mono.just(ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ErrorResponse.of(message, "VALIDATION_ERROR", errors))));
	}

	/**
	 * Cliente cerró la conexión durante streaming (video/imagen) o antes de recibir la respuesta. No es un error de aplicación.
	 */
	@ExceptionHandler(IOException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleIOException(IOException ex, ServerWebExchange exchange) {
		String msg = ex.getMessage() != null ? ex.getMessage() : "";
		if (msg.contains("Broken pipe") || msg.contains("Connection reset")) {
			log.debug("Cliente cerró la conexión ({}).", msg);
			return Mono.empty();
		}
		log.warn("Error de E/S: {}", ex.getMessage());
		return skipIfCommitted(exchange, Mono.just(ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(ERROR_INTERNO, "IO_ERROR"))));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public Mono<ResponseEntity<ErrorResponse>> handleResponseStatus(ResponseStatusException ex, ServerWebExchange exchange) {
		String message = ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		log.warn("Respuesta de estado {}: {}", status, message);
		return skipIfCommitted(exchange, Mono.just(ResponseEntity
				.status(status)
				.body(ErrorResponse.of(message, ex.getStatusCode().toString()))));
	}

	@ExceptionHandler(Exception.class)
	public Mono<ResponseEntity<ErrorResponse>> handleAny(Exception ex, ServerWebExchange exchange) {
		Throwable cause = ex.getCause();
		String msg = (ex.getMessage() != null ? ex.getMessage() : "") + (cause != null && cause.getMessage() != null ? cause.getMessage() : "");
		if (msg.contains("Broken pipe") || msg.contains("Connection reset")) {
			log.debug("Cliente cerró la conexión (excepción envuelta).");
			return Mono.empty();
		}
		log.error("Excepción no controlada. Evitando caída del servicio.", ex);
		return skipIfCommitted(exchange, Mono.just(ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(ERROR_INTERNO, "INTERNAL_ERROR"))));
	}
}
