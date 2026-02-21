package com.asecontin.backend.repository;

import com.asecontin.backend.entity.Usuario;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface UsuarioRepository extends R2dbcRepository<Usuario, Long> {

	Mono<Usuario> findByEmail(String email);

	Mono<Long> countBy();
}
