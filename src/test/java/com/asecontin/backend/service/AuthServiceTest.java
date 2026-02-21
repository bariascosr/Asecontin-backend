package com.asecontin.backend.service;

import com.asecontin.backend.dto.AuthResponse;
import com.asecontin.backend.dto.LoginRequest;
import com.asecontin.backend.dto.RegisterRequest;
import com.asecontin.backend.entity.Usuario;
import com.asecontin.backend.repository.UsuarioRepository;
import com.asecontin.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private JwtService jwtService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthService authService;

	private Usuario usuario;

	@BeforeEach
	void setUp() {
		usuario = new Usuario();
		usuario.setIdUsuario(1L);
		usuario.setNombre("Admin");
		usuario.setEmail("admin@test.com");
		usuario.setPassword("$2a$10$encoded"); // BCrypt encoded
		usuario.setRol("ADMINISTRADOR");
	}

	@Nested
	@DisplayName("login")
	class Login {

		@Test
		@DisplayName("devuelve token cuando credenciales son correctas")
		void loginOk() {
			LoginRequest request = new LoginRequest("admin@test.com", "123456");
			when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Mono.just(usuario));
			when(passwordEncoder.matches("123456", usuario.getPassword())).thenReturn(true);
			when(jwtService.createToken("admin@test.com", "ADMINISTRADOR")).thenReturn("jwt.token.here");

			StepVerifier.create(authService.login(request))
					.expectNextMatches(r -> "jwt.token.here".equals(r.token())
							&& "admin@test.com".equals(r.email())
							&& "Admin".equals(r.nombre()))
					.verifyComplete();

			verify(jwtService).createToken("admin@test.com", "ADMINISTRADOR");
		}

		@Test
		@DisplayName("falla cuando contraseña es incorrecta")
		void loginWrongPassword() {
			LoginRequest request = new LoginRequest("admin@test.com", "wrong");
			when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Mono.just(usuario));
			when(passwordEncoder.matches("wrong", usuario.getPassword())).thenReturn(false);

			StepVerifier.create(authService.login(request))
					.expectError(IllegalArgumentException.class)
					.verify();

			verify(jwtService, never()).createToken(anyString(), anyString());
		}

		@Test
		@DisplayName("falla cuando usuario no existe")
		void loginUserNotFound() {
			LoginRequest request = new LoginRequest("noexiste@test.com", "123456");
			when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Mono.empty());

			StepVerifier.create(authService.login(request))
					.expectError(IllegalArgumentException.class)
					.verify();

			verify(passwordEncoder, never()).matches(anyString(), anyString());
			verify(jwtService, never()).createToken(anyString(), anyString());
		}
	}

	@Nested
	@DisplayName("registerFirstAdmin")
	class RegisterFirstAdmin {

		@Test
		@DisplayName("registra y devuelve token cuando no hay usuarios")
		void registerOk() {
			RegisterRequest request = new RegisterRequest("Admin", "admin@test.com", "123456");
			when(usuarioRepository.countBy()).thenReturn(Mono.just(0L));
			when(passwordEncoder.encode("123456")).thenReturn("encoded");
			Usuario saved = new Usuario();
			saved.setIdUsuario(1L);
			saved.setNombre("Admin");
			saved.setEmail("admin@test.com");
			saved.setRol("ADMINISTRADOR");
			when(usuarioRepository.save(any(Usuario.class))).thenReturn(Mono.just(saved));
			when(jwtService.createToken("admin@test.com", "ADMINISTRADOR")).thenReturn("new.jwt");

			StepVerifier.create(authService.registerFirstAdmin(request))
					.expectNextMatches(r -> "new.jwt".equals(r.token()) && "admin@test.com".equals(r.email()))
					.verifyComplete();

			verify(usuarioRepository).save(any(Usuario.class));
		}

		@Test
		@DisplayName("falla cuando ya existe un administrador")
		void registerAlreadyExists() {
			RegisterRequest request = new RegisterRequest("Admin", "admin@test.com", "123456");
			when(usuarioRepository.countBy()).thenReturn(Mono.just(1L));

			StepVerifier.create(authService.registerFirstAdmin(request))
					.expectError(IllegalStateException.class)
					.verify();

			verify(usuarioRepository, never()).save(any(Usuario.class));
		}
	}
}
