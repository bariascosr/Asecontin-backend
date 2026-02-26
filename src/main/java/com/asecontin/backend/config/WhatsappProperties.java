package com.asecontin.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración para notificaciones por WhatsApp Cloud API (Meta).
 * Número del agente (destinatario): fácilmente modificable vía app.whatsapp.agent-phone.
 */
@ConfigurationProperties(prefix = "app.whatsapp")
public record WhatsappProperties(
		/** Número del agente/destinatario (solo dígitos, sin + ni espacios). Por defecto: +57 310 853 1903 */
		String agentPhone,
		/** Token de acceso de Meta (Bearer). Si está vacío, no se envía notificación. */
		String accessToken,
		/** ID del número de teléfono de WhatsApp Business en Meta. */
		String phoneNumberId,
		/** Versión de la API de Graph (ej. v21.0). */
		String apiVersion,
		/** Nombre de la plantilla aprobada para recordatorio de pago (ej. recordatorio_pago). Si está vacío, se usa mensaje de texto libre (ventana 24h). */
		String templateRecordatorioPago,
		/** Código de idioma de la plantilla (ej. es, en). Por defecto es. */
		String templateRecordatorioPagoLanguage
) {
	private static final String DEFAULT_AGENT_PHONE = "573108531903";
	private static final String DEFAULT_API_VERSION = "v21.0";
	private static final String DEFAULT_TEMPLATE_LANGUAGE = "es";

	public String getAgentPhone() {
		return agentPhone != null && !agentPhone.isBlank() ? agentPhone.strip() : DEFAULT_AGENT_PHONE;
	}

	public String getApiVersion() {
		return apiVersion != null && !apiVersion.isBlank() ? apiVersion.strip() : DEFAULT_API_VERSION;
	}

	public String getTemplateRecordatorioPagoLanguage() {
		return templateRecordatorioPagoLanguage != null && !templateRecordatorioPagoLanguage.isBlank()
				? templateRecordatorioPagoLanguage.strip() : DEFAULT_TEMPLATE_LANGUAGE;
	}

	/** Indica si la integración está configurada (token y phone-number-id presentes). */
	public boolean isEnabled() {
		return accessToken != null && !accessToken.isBlank()
				&& phoneNumberId != null && !phoneNumberId.isBlank();
	}

	/** Indica si hay plantilla configurada para recordatorio de pago (mensajes iniciados por la empresa). */
	public boolean isTemplateRecordatorioPagoEnabled() {
		return templateRecordatorioPago != null && !templateRecordatorioPago.isBlank();
	}
}
