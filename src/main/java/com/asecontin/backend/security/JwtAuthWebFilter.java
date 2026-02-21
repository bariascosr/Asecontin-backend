package com.asecontin.backend.security;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Stream;

@Component
public class JwtAuthWebFilter implements ServerAuthenticationConverter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthWebFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	public Mono<Authentication> convert(ServerWebExchange exchange) {
		return Mono.fromCallable(() -> extractToken(exchange.getRequest().getHeaders()))
				.filter(token -> !token.isBlank())
				.flatMap(this::validateAndCreateAuth)
				.onErrorResume(e -> Mono.empty());
	}

	private String extractToken(HttpHeaders headers) {
		String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
		if (auth != null && auth.startsWith(BEARER_PREFIX)) {
			return auth.substring(BEARER_PREFIX.length()).trim();
		}
		return "";
	}

	private Mono<Authentication> validateAndCreateAuth(String token) {
		try {
			Claims claims = jwtService.validateAndGetClaims(token);
			String email = jwtService.getEmailFromClaims(claims);
			String rol = claims.get("rol", String.class);
			List<SimpleGrantedAuthority> authorities = Stream.ofNullable(rol)
					.map(r -> new SimpleGrantedAuthority("ROLE_" + r))
					.toList();
			return Mono.just((Authentication) new UsernamePasswordAuthenticationToken(email, null, authorities));
		} catch (Exception e) {
			return Mono.empty();
		}
	}
}
