package com.asecontin.backend.service;

import com.asecontin.backend.dto.ActualizarUsuarioRequest;
import com.asecontin.backend.dto.UsuarioResponse;
import com.asecontin.backend.entity.Usuario;
import com.asecontin.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UsuarioService {

	private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Mono<UsuarioResponse> obtenerPorEmail(String email) {
		return usuarioRepository.findByEmail(email)
				.map(this::toResponse)
				.doOnNext(r -> log.debug("Usuario obtenido: email={}", email))
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Usuario no encontrado: email={}", email);
					return Mono.error(new IllegalArgumentException("Usuario no encontrado"));
				}));
	}

	public Mono<UsuarioResponse> actualizar(String emailActual, ActualizarUsuarioRequest request) {
		log.debug("Actualizando usuario: email={}", emailActual);
		return usuarioRepository.findByEmail(emailActual)
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Usuario no encontrado para actualizar: email={}", emailActual);
					return Mono.error(new IllegalArgumentException("Usuario no encontrado"));
				}))
				.flatMap(usuario -> {
					if (request.nombre() != null && !request.nombre().isBlank()) {
						usuario.setNombre(request.nombre().trim());
					}
					if (request.email() != null && !request.email().isBlank()) {
						String nuevoEmail = request.email().trim();
						if (!nuevoEmail.equalsIgnoreCase(emailActual)) {
							return usuarioRepository.findByEmail(nuevoEmail)
									.flatMap(existing -> {
										log.warn("Actualizar usuario: email ya en uso {}", nuevoEmail);
										return Mono.<Usuario>error(new IllegalStateException("El email ya está en uso"));
									})
									.switchIfEmpty(Mono.defer(() -> {
										usuario.setEmail(nuevoEmail);
										return Mono.just(usuario);
									}));
						}
					}
					if (request.isCambioPassword()) {
						if (request.passwordActual() == null || request.passwordActual().isBlank()) {
							return Mono.error(new IllegalArgumentException("La contraseña actual es obligatoria para cambiar la contraseña"));
						}
						if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
							return Mono.error(new IllegalArgumentException("Contraseña actual incorrecta"));
						}
						usuario.setPassword(passwordEncoder.encode(request.nuevaPassword()));
					}
					return Mono.just(usuario);
				})
				.flatMap(usuarioRepository::save)
				.map(this::toResponse)
				.doOnNext(r -> log.info("Usuario actualizado: id={}", r.id()));
	}

	private UsuarioResponse toResponse(Usuario u) {
		return new UsuarioResponse(u.getIdUsuario(), u.getNombre(), u.getEmail(), u.getRol());
	}
}
