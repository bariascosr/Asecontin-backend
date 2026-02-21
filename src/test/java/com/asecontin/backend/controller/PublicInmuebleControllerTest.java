package com.asecontin.backend.controller;

import com.asecontin.backend.dto.ApiResponse;
import com.asecontin.backend.dto.InmuebleDetallePublicoResponse;
import com.asecontin.backend.dto.InmuebleResponse;
import com.asecontin.backend.dto.PageResponse;
import com.asecontin.backend.service.InmuebleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas del controlador público de inmuebles usando WebTestClient
 * vinculado al controlador (sin levantar el contexto completo).
 */
class PublicInmuebleControllerTest {

	private final InmuebleService inmuebleService = mock(InmuebleService.class);
	private final WebTestClient webTestClient = WebTestClient
			.bindToController(new PublicInmuebleController(inmuebleService))
			.build();

	@Test
	@DisplayName("GET /api/public/inmuebles devuelve 200 y página de inmuebles")
	void listarInmuebles() {
		LocalDateTime now = LocalDateTime.now();
		InmuebleResponse r = new InmuebleResponse(
				1L, "Casa test", "Desc", BigDecimal.valueOf(100000000),
				"Calle 1", 1L, "Localidad", "Bogotá", "casa", 1L, "disponible", "nuevo",
				1, null, null, null, null, null, null, null, null, false, null,
				now, now, null);
		PageResponse<InmuebleResponse> page = PageResponse.of(List.of(r), 1, 0, 20);
		when(inmuebleService.listar(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
				any(), any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(Mono.just(page));

		webTestClient.get()
				.uri("/api/public/inmuebles")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<ApiResponse<PageResponse<InmuebleResponse>>>() {})
				.value(api -> {
					assertTrue(api.success());
					PageResponse<InmuebleResponse> p = api.data();
					assertNotNull(p);
					assertEquals(1, p.totalElements());
					assertEquals(1, p.content().size());
					InmuebleResponse first = p.content().get(0);
					assertEquals(1L, first.id());
					assertEquals("Casa test", first.titulo());
				});
	}

	@Test
	@DisplayName("GET /api/public/inmuebles/{id} devuelve 200 y detalle con galería")
	void detalleInmueble() {
		LocalDateTime now = LocalDateTime.now();
		InmuebleDetallePublicoResponse detalle = new InmuebleDetallePublicoResponse(
				1L, "Casa test", "Desc", BigDecimal.valueOf(100000000),
				"Calle 1", 1L, "Localidad", "Bogotá", "casa", 1L, "disponible", "nuevo",
				1, null, null, null, null, null, null, null, null, false, null,
				now, now,
				List.of("https://example.com/img1.jpg"),
				List.of("https://example.com/vid1.mp4"));
		when(inmuebleService.obtenerDetallePublico(1L)).thenReturn(Mono.just(detalle));

		webTestClient.get()
				.uri("/api/public/inmuebles/1")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<ApiResponse<InmuebleDetallePublicoResponse>>() {})
				.value(api -> {
					assertTrue(api.success());
					InmuebleDetallePublicoResponse d = api.data();
					assertNotNull(d);
					assertEquals(1L, d.id());
					assertEquals(1, d.imagenes().size());
					assertEquals(1, d.videos().size());
				});
	}

	@Test
	@DisplayName("GET /api/public/inmuebles/{id} devuelve 404 cuando no existe")
	void detalleNoEncontrado() {
		when(inmuebleService.obtenerDetallePublico(999L))
				.thenReturn(Mono.error(new IllegalArgumentException("Inmueble no encontrado")));

		webTestClient.get()
				.uri("/api/public/inmuebles/999")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound();
	}
}
