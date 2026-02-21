package com.asecontin.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("API Plataforma Inmobiliaria")
						.version("1.0")
						.description("API REST del backend: auth, administración de inmuebles, estados, imágenes, videos, blog y endpoints públicos. "
								+ "Las respuestas exitosas usan el formato estándar { \"success\": true, \"data\": ... }. "
								+ "Los errores 4xx/5xx devuelven { \"success\": false, \"message\": \"...\", \"code\": \"...\" }."))
				.components(new Components()
						.addSecuritySchemes("bearerAuth",
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.description("Token obtenido de POST /api/auth/login o POST /api/auth/register")));
	}
}
