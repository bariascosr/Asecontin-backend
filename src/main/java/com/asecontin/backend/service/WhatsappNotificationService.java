package com.asecontin.backend.service;

import com.asecontin.backend.config.WhatsappProperties;
import com.asecontin.backend.dto.ContactoRequest;
import com.asecontin.backend.dto.InmuebleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Envía notificaciones al agente por WhatsApp Cloud API (Meta) cuando se recibe un contacto.
 * El número del destinatario se configura en {@link WhatsappProperties#getAgentPhone()}.
 */
@Service
public class WhatsappNotificationService {

	private static final Logger log = LoggerFactory.getLogger(WhatsappNotificationService.class);
	private static final String GRAPH_BASE_URL = "https://graph.facebook.com";

	private final WhatsappProperties whatsappProperties;
	private final WebClient webClient;
	private final InmuebleService inmuebleService;

	public WhatsappNotificationService(WhatsappProperties whatsappProperties, WebClient.Builder webClientBuilder,
			InmuebleService inmuebleService) {
		this.whatsappProperties = whatsappProperties;
		this.webClient = webClientBuilder.build();
		this.inmuebleService = inmuebleService;
	}

	/**
	 * Envía al agente (número configurado) un mensaje con los datos del contacto y, si hay inmuebleId, datos del inmueble.
	 * Si WhatsApp no está configurado (token o phone-number-id vacíos), no hace nada.
	 */
	public Mono<Void> notifyContactToAgent(ContactoRequest request) {
		if (!whatsappProperties.isEnabled()) {
			log.debug("WhatsApp no configurado (access-token o phone-number-id vacíos); no se envía notificación.");
			return Mono.empty();
		}

		Mono<InmuebleResponse> inmuebleMono = request.inmuebleId() != null
				? inmuebleService.obtenerPorId(request.inmuebleId()).onErrorResume(e -> {
					log.debug("No se pudo cargar inmueble {} para la notificación: {}", request.inmuebleId(), e.getMessage());
					return Mono.empty();
				})
				: Mono.empty();

		return inmuebleMono
				.map(Optional::of)
				.defaultIfEmpty(Optional.empty())
				.flatMap(optInmueble -> {
					String body = buildMessageBody(request, optInmueble.orElse(null));
					return sendMessage(body);
				});
	}

	private Mono<Void> sendMessage(String body) {
		return sendToPhone(whatsappProperties.getAgentPhone(), body)
				.doOnSuccess(v -> log.info("Notificación de contacto enviada por WhatsApp al agente."));
	}

	/**
	 * Envía un mensaje de texto por WhatsApp a un número (solo dígitos, con código de país).
	 * Si WhatsApp no está configurado, no hace nada.
	 */
	public Mono<Void> sendToPhone(String phone, String messageBody) {
		if (!whatsappProperties.isEnabled()) {
			log.debug("WhatsApp no configurado; no se envía mensaje.");
			return Mono.empty();
		}
		if (phone == null || phone.isBlank() || messageBody == null || messageBody.isBlank()) {
			return Mono.empty();
		}
		String to = phone.replaceAll("\\D", "");
		if (to.length() < 10) {
			log.warn("Número de teléfono inválido para WhatsApp: {}", phone);
			return Mono.empty();
		}
		String url = GRAPH_BASE_URL + "/" + whatsappProperties.getApiVersion() + "/"
				+ whatsappProperties.phoneNumberId() + "/messages";
		Map<String, Object> payload = Map.of(
				"messaging_product", "whatsapp",
				"recipient_type", "individual",
				"to", to,
				"type", "text",
				"text", Map.of(
						"preview_url", false,
						"body", messageBody
				)
		);
		return webClient.post()
				.uri(url)
				.header("Authorization", "Bearer " + whatsappProperties.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(payload)
				.retrieve()
				.bodyToMono(Void.class)
				.doOnError(e -> log.warn("Error al enviar WhatsApp a {}: {}", to, e.getMessage()))
				.onErrorResume(e -> Mono.empty());
	}

	/**
	 * Envía un mensaje de plantilla (template) por WhatsApp. Para mensajes iniciados por la empresa
	 * (ej. recordatorio de pago) es necesario usar una plantilla aprobada en Meta Business Manager.
	 * @param phone Número con código de país (solo dígitos).
	 * @param templateName Nombre de la plantilla (ej. recordatorio_pago).
	 * @param languageCode Código de idioma (ej. es).
	 * @param bodyParameters Parámetros en orden para el body de la plantilla ({{1}}, {{2}}, ...).
	 */
	public Mono<Void> sendTemplateToPhone(String phone, String templateName, String languageCode, List<String> bodyParameters) {
		if (!whatsappProperties.isEnabled()) {
			log.debug("WhatsApp no configurado; no se envía plantilla.");
			return Mono.empty();
		}
		if (phone == null || phone.isBlank() || templateName == null || templateName.isBlank()) {
			return Mono.empty();
		}
		String to = phone.replaceAll("\\D", "");
		if (to.length() < 10) {
			log.warn("Número de teléfono inválido para WhatsApp: {}", phone);
			return Mono.empty();
		}
		String lang = languageCode != null && !languageCode.isBlank() ? languageCode.strip() : "es";
		List<Map<String, String>> parameters = (bodyParameters != null ? bodyParameters : List.<String>of()).stream()
				.map(p -> Map.<String, String>of("type", "text", "text", p != null ? p : ""))
				.collect(Collectors.toList());
		Map<String, Object> bodyComponent = Map.of(
				"type", "body",
				"parameters", parameters
		);
		Map<String, Object> template = Map.of(
				"name", templateName.strip(),
				"language", Map.of("code", lang),
				"components", List.of(bodyComponent)
		);
		Map<String, Object> payload = Map.of(
				"messaging_product", "whatsapp",
				"recipient_type", "individual",
				"to", to,
				"type", "template",
				"template", template
		);
		String url = GRAPH_BASE_URL + "/" + whatsappProperties.getApiVersion() + "/"
				+ whatsappProperties.phoneNumberId() + "/messages";
		return webClient.post()
				.uri(url)
				.header("Authorization", "Bearer " + whatsappProperties.accessToken())
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue(payload)
				.retrieve()
				.bodyToMono(Void.class)
				.doOnError(e -> log.warn("Error al enviar plantilla WhatsApp a {}: {}", to, e.getMessage()))
				.onErrorResume(e -> Mono.empty());
	}

	private String buildMessageBody(ContactoRequest request, InmuebleResponse inmueble) {
		StringBuilder sb = new StringBuilder();
		sb.append("📩 *Nuevo contacto*\n\n");
		sb.append("Nombre: ").append(request.nombre()).append("\n");
		sb.append("Email: ").append(request.email()).append("\n");
		sb.append("Mensaje: ").append(request.mensaje()).append("\n");

		if (inmueble != null) {
			sb.append("\n🏠 *Inmueble consultado*\n");
			sb.append("ID: ").append(inmueble.id()).append("\n");
			if (inmueble.titulo() != null && !inmueble.titulo().isBlank()) {
				sb.append("Título: ").append(inmueble.titulo()).append("\n");
			}
			if (inmueble.direccion() != null && !inmueble.direccion().isBlank()) {
				sb.append("Dirección: ").append(inmueble.direccion()).append("\n");
			}
			if (inmueble.precioVenta() != null && inmueble.precioVenta().compareTo(java.math.BigDecimal.ZERO) > 0) {
				sb.append("Precio venta: ").append(formatPrecio(inmueble.precioVenta())).append("\n");
			}
			if (inmueble.valorArriendo() != null && inmueble.valorArriendo().compareTo(java.math.BigDecimal.ZERO) > 0) {
				sb.append("Valor arriendo: ").append(formatPrecio(inmueble.valorArriendo())).append("\n");
			}
			if (inmueble.tipo() != null && !inmueble.tipo().isBlank()) {
				sb.append("Tipo: ").append(inmueble.tipo()).append("\n");
			}
			if (inmueble.ciudadNombre() != null && !inmueble.ciudadNombre().isBlank()) {
				sb.append("Ciudad: ").append(inmueble.ciudadNombre());
				if (inmueble.localidadNombre() != null && !inmueble.localidadNombre().isBlank()) {
					sb.append(" - ").append(inmueble.localidadNombre());
				}
				sb.append("\n");
			} else if (inmueble.localidadNombre() != null && !inmueble.localidadNombre().isBlank()) {
				sb.append("Localidad: ").append(inmueble.localidadNombre()).append("\n");
			}
			if (inmueble.areaM2() != null && inmueble.areaM2().compareTo(BigDecimal.ZERO) > 0) {
				sb.append("Área: ").append(inmueble.areaM2().stripTrailingZeros().toPlainString()).append(" m²\n");
			}
			if (inmueble.habitaciones() != null && inmueble.habitaciones() > 0) {
				sb.append("Habitaciones: ").append(inmueble.habitaciones()).append("\n");
			}
			if (inmueble.banos() != null && inmueble.banos() > 0) {
				sb.append("Baños: ").append(inmueble.banos()).append("\n");
			}
			if (inmueble.estrato() != null && inmueble.estrato() > 0) {
				sb.append("Estrato: ").append(inmueble.estrato()).append("\n");
			}
			if (inmueble.sectorNombre() != null && !inmueble.sectorNombre().isBlank()) {
				sb.append("Sector: ").append(inmueble.sectorNombre()).append("\n");
			}
		} else if (request.inmuebleId() != null) {
			sb.append("Inmueble ID: ").append(request.inmuebleId()).append("\n");
		}

		return sb.toString();
	}

	private static String formatPrecio(BigDecimal precio) {
		if (precio == null) return "";
		return "$" + precio.stripTrailingZeros().toPlainString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1.");
	}
}
