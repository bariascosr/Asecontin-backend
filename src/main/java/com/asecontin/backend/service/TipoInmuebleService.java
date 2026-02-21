package com.asecontin.backend.service;

import com.asecontin.backend.dto.TipoInmuebleResponse;
import com.asecontin.backend.entity.TipoInmueble;
import com.asecontin.backend.repository.TipoInmuebleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
public class TipoInmuebleService {

	private final TipoInmuebleRepository tipoInmuebleRepository;

	public TipoInmuebleService(TipoInmuebleRepository tipoInmuebleRepository) {
		this.tipoInmuebleRepository = tipoInmuebleRepository;
	}

	public Flux<TipoInmuebleResponse> listarTodos() {
		return tipoInmuebleRepository.findAll()
				.sort(Comparator.comparing(TipoInmueble::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.map(this::toResponse);
	}

	public Mono<Boolean> existePorId(Long id) {
		if (id == null) return Mono.just(false);
		return tipoInmuebleRepository.existsById(id);
	}

	private TipoInmuebleResponse toResponse(TipoInmueble t) {
		return new TipoInmuebleResponse(t.getIdTipo(), t.getNombre());
	}
}
