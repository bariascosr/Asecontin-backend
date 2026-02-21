package com.asecontin.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de medios (imágenes/videos) en disco.
 * base-url se usa para construir las URLs públicas que devuelve la API.
 */
@ConfigurationProperties(prefix = "app.media")
public record MediaProperties(String uploadDir, String baseUrl) {

	public String getBaseUrlNormalized() {
		return baseUrl != null && baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}
}
