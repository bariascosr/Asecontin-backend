package com.asecontin.backend.service;

import com.asecontin.backend.dto.AuthResponse;
import com.asecontin.backend.dto.LoginRequest;
import com.asecontin.backend.dto.RegisterRequest;
import com.asecontin.backend.entity.Usuario;
import com.asecontin.backend.repository.UsuarioRepository;
import com.asecontin.backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

	private static final Logger log = LoggerFactory.getLogger(AuthService.class);

	private static final String ROL_ADMIN = "ADMINISTRADOR";

	private final UsuarioRepository usuarioRepository;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;

	public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.jwtService = jwtService;
		this.passwordEncoder = passwordEncoder;
	}

	public Mono<AuthResponse> login(LoginRequest request) {
		log.debug("Login intent para email: {}", request.email() != null ? request.email() : "(null)");
		return usuarioRepository.findByEmail(request.email())
				.filter(usuario -> passwordEncoder.matches(request.password(), usuario.getPassword()))
				.map(usuario -> {
					String token = jwtService.createToken(usuario.getEmail(), usuario.getRol());
					AuthResponse resp = new AuthResponse(token, usuario.getEmail(), usuario.getNombre(), usuario.getRol());
					log.info("Login exitoso: {}", usuario.getEmail());
					return resp;
				})
				.switchIfEmpty(Mono.defer(() -> {
					log.warn("Login fallido: credenciales inválidas para email {}", request.email());
					return Mono.error(new IllegalArgumentException("Credenciales inválidas"));
				}));
	}

	/**
	 * Registra el primer administrador. Solo permite registro si aún no existe ningún usuario.
	 */
	public Mono<AuthResponse> registerFirstAdmin(RegisterRequest request) {
		log.debug("Intento de registro de primer administrador: {}", request.email());
		return usuarioRepository.countBy()
				.flatMap(count -> {
					if (count > 0) {
						log.warn("Registro rechazado: ya existe un administrador");
						return Mono.error(new IllegalStateException("Ya existe un administrador registrado"));
					}
					Usuario usuario = new Usuario();
					usuario.setNombre(request.nombre());
					usuario.setEmail(request.email());
					usuario.setPassword(passwordEncoder.encode(request.password()));
					usuario.setRol(ROL_ADMIN);
					return usuarioRepository.save(usuario)
							.map(saved -> {
								String token = jwtService.createToken(saved.getEmail(), saved.getRol());
								log.info("Primer administrador registrado: {}", saved.getEmail());
								return new AuthResponse(token, saved.getEmail(), saved.getNombre(), saved.getRol());
							});
				});
	}
}
