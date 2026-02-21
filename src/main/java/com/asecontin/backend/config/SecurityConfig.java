package com.asecontin.backend.config;

import com.asecontin.backend.security.JwtAuthWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtAuthWebFilter jwtAuthWebFilter) {
		ReactiveAuthenticationManager authenticationManager = auth -> Mono.just(auth);

		var jwtFilter = new AuthenticationWebFilter(authenticationManager);
		jwtFilter.setServerAuthenticationConverter(jwtAuthWebFilter);
		jwtFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/admin/**"));

		return http
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
				.authorizeExchange(exchange -> exchange
						.pathMatchers("/api/auth/**").permitAll()
						.pathMatchers("/api/public/**").permitAll()
						.pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**", "/webjars/**").permitAll()
						.pathMatchers("/api/admin/**").authenticated()
						.pathMatchers("/actuator/health", "/actuator/health/**").permitAll()
						.anyExchange().denyAll())
				.build();
	}
}
