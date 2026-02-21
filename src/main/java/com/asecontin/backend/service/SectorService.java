package com.asecontin.backend.service;

import com.asecontin.backend.dto.SectorResponse;
import com.asecontin.backend.entity.Sector;
import com.asecontin.backend.repository.SectorRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
public class SectorService {

	private final SectorRepository sectorRepository;

	public SectorService(SectorRepository sectorRepository) {
		this.sectorRepository = sectorRepository;
	}

	public Flux<SectorResponse> listarTodos() {
		return sectorRepository.findAll()
				.sort(Comparator.comparing(Sector::getNombre, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
				.map(this::toResponse);
	}

	public Mono<Boolean> existePorId(Long id) {
		if (id == null) return Mono.just(false);
		return sectorRepository.existsById(id);
	}

	private SectorResponse toResponse(Sector s) {
		return new SectorResponse(s.getIdSector(), s.getNombre());
	}
}
