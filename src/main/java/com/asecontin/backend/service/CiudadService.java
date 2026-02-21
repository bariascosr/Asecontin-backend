package com.asecontin.backend.service;

import com.asecontin.backend.dto.CiudadResponse;
import com.asecontin.backend.entity.Ciudad;
import com.asecontin.backend.repository.CiudadRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
public class CiudadService {

	private final CiudadRepository ciudadRepository;

	public CiudadService(CiudadRepository ciudadRepository) {
		this.ciudadRepository = ciudadRepository;
	}

	public Flux<CiudadResponse> listarTodas() {
		return ciudadRepository.findAll()
				.sort(Comparator.comparing(Ciudad::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.map(this::toResponse);
	}

	public Mono<Boolean> existePorNombre(String nombre) {
		if (nombre == null || nombre.isBlank()) return Mono.just(false);
		return ciudadRepository.existsByNombreIgnoreCase(nombre.trim());
	}

	private CiudadResponse toResponse(Ciudad c) {
		return new CiudadResponse(c.getIdCiudad(), c.getNombre());
	}
}
