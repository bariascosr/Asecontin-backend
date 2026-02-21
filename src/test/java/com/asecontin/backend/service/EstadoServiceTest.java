package com.asecontin.backend.service;

import com.asecontin.backend.dto.EstadoRequest;
import com.asecontin.backend.dto.EstadoResponse;
import com.asecontin.backend.entity.Estado;
import com.asecontin.backend.repository.EstadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadoServiceTest {

	@Mock
	private EstadoRepository estadoRepository;

	@InjectMocks
	private EstadoService estadoService;

	@Nested
	@DisplayName("listar")
	class Listar {

		@Test
		@DisplayName("devuelve página vacía cuando no hay estados")
		void listarVacio() {
			when(estadoRepository.findAll(any(Sort.class))).thenReturn(Flux.empty());
			when(estadoRepository.count()).thenReturn(Mono.just(0L));

			StepVerifier.create(estadoService.listar(0, 20))
					.expectNextMatches(p -> p.content().isEmpty() && p.totalElements() == 0)
					.verifyComplete();

			verify(estadoRepository).findAll(any(Sort.class));
			verify(estadoRepository).count();
		}

		@Test
		@DisplayName("devuelve página con estados ordenados por nombre")
		void listarConDatos() {
			Estado e1 = new Estado(1L, "disponible");
			Estado e2 = new Estado(2L, "vendido");
			when(estadoRepository.findAll(any(Sort.class))).thenReturn(Flux.just(e1, e2));
			when(estadoRepository.count()).thenReturn(Mono.just(2L));

			StepVerifier.create(estadoService.listar(0, 20))
					.expectNextMatches(p -> {
						List<EstadoResponse> content = p.content();
						return p.totalElements() == 2 && content.size() == 2
								&& content.get(0).id() == 1L && "disponible".equals(content.get(0).nombreEstado())
								&& content.get(1).id() == 2L && "vendido".equals(content.get(1).nombreEstado());
					})
					.verifyComplete();
		}
	}

	@Nested
	@DisplayName("crear")
	class Crear {

		@Test
		@DisplayName("crea estado cuando el nombre no existe")
		void crearOk() {
			EstadoRequest request = new EstadoRequest("nuevo");
			when(estadoRepository.existsByNombreEstadoIgnoreCase("nuevo")).thenReturn(Mono.just(false));
			Estado saved = new Estado(1L, "nuevo");
			when(estadoRepository.save(any(Estado.class))).thenReturn(Mono.just(saved));

			StepVerifier.create(estadoService.crear(request))
					.expectNextMatches(r -> r.id() == 1L && "nuevo".equals(r.nombreEstado()))
					.verifyComplete();

			verify(estadoRepository).save(any(Estado.class));
		}

		@Test
		@DisplayName("falla cuando ya existe un estado con ese nombre")
		void crearNombreDuplicado() {
			EstadoRequest request = new EstadoRequest("disponible");
			when(estadoRepository.existsByNombreEstadoIgnoreCase("disponible")).thenReturn(Mono.just(true));

			StepVerifier.create(estadoService.crear(request))
					.expectError(IllegalStateException.class)
					.verify();

			verify(estadoRepository, never()).save(any(Estado.class));
		}
	}

	@Nested
	@DisplayName("obtenerPorId")
	class ObtenerPorId {

		@Test
		@DisplayName("devuelve estado cuando existe")
		void obtenerOk() {
			Estado e = new Estado(1L, "rentado");
			when(estadoRepository.findById(1L)).thenReturn(Mono.just(e));

			StepVerifier.create(estadoService.obtenerPorId(1L))
					.expectNextMatches(r -> r.id() == 1L && "rentado".equals(r.nombreEstado()))
					.verifyComplete();
		}

		@Test
		@DisplayName("falla cuando no existe")
		void obtenerNoEncontrado() {
			when(estadoRepository.findById(999L)).thenReturn(Mono.empty());

			StepVerifier.create(estadoService.obtenerPorId(999L))
					.expectError(IllegalArgumentException.class)
					.verify();
		}
	}
}
