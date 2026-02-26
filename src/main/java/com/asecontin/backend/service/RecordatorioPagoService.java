package com.asecontin.backend.service;

import com.asecontin.backend.config.WhatsappProperties;
import com.asecontin.backend.entity.Arrendatario;
import com.asecontin.backend.entity.Inmueble;
import com.asecontin.backend.repository.ArrendatarioRepository;
import com.asecontin.backend.repository.InmuebleArrendatarioRepository;
import com.asecontin.backend.repository.InmuebleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * Job programado: el día 5 de cada mes envía por WhatsApp a los arrendatarios un recordatorio
 * de que el pago del arriendo vence ese día y que, si no se realiza, tendrá un recargo configurable (p. ej. 5%).
 * Solo se notifica a arrendatarios que tengan teléfono registrado y al menos un inmueble asociado con valor de arriendo.
 */
@Service
public class RecordatorioPagoService {

	private static final Logger log = LoggerFactory.getLogger(RecordatorioPagoService.class);

	/** Porcentaje de recargo si no se paga el día 5 (ej. 5 = 5%). */
	private static final int PORCENTAJE_RECARGO = 5;

	private final ArrendatarioRepository arrendatarioRepository;
	private final InmuebleArrendatarioRepository inmuebleArrendatarioRepository;
	private final InmuebleRepository inmuebleRepository;
	private final WhatsappNotificationService whatsappNotificationService;
	private final WhatsappProperties whatsappProperties;

	public RecordatorioPagoService(ArrendatarioRepository arrendatarioRepository,
			InmuebleArrendatarioRepository inmuebleArrendatarioRepository,
			InmuebleRepository inmuebleRepository,
			WhatsappNotificationService whatsappNotificationService,
			WhatsappProperties whatsappProperties) {
		this.arrendatarioRepository = arrendatarioRepository;
		this.inmuebleArrendatarioRepository = inmuebleArrendatarioRepository;
		this.inmuebleRepository = inmuebleRepository;
		this.whatsappNotificationService = whatsappNotificationService;
		this.whatsappProperties = whatsappProperties;
	}

	/**
	 * Se ejecuta el día 5 de cada mes a las 08:00 (horario del servidor).
	 * Envía recordatorio por WhatsApp a cada arrendatario con teléfono e inmuebles en arriendo.
	 */
	@Scheduled(cron = "${app.recordatorio-pago.cron:0 0 8 5 * ?}")
	public void enviarRecordatoriosDia5() {
		log.info("Iniciando envío de recordatorios de pago (día 5 del mes).");
		ejecutarRecordatorio()
				.doOnSuccess(v -> log.info("Envío de recordatorios de pago finalizado."))
				.doOnError(e -> log.error("Error en envío de recordatorios de pago: {}", e.getMessage()))
				.onErrorResume(e -> Mono.empty())
				.block();
	}

	/**
	 * Ejecuta el envío de recordatorios. Puede invocarse también de forma manual (ej. desde un endpoint admin).
	 */
	public Mono<Void> ejecutarRecordatorio() {
		return arrendatarioRepository.findAll()
				.filter(a -> a.getTelefono() != null && !a.getTelefono().isBlank())
				.flatMap(this::enviarRecordatorioSiTieneInmuebles)
				.then();
	}

	private Mono<Void> enviarRecordatorioSiTieneInmuebles(Arrendatario a) {
		return inmuebleArrendatarioRepository.findInmuebleIdsByArrendatarioId(a.getIdArrendatario())
				.flatMap(inmuebleRepository::findById)
				.filter(inm -> inm.getValorArriendo() != null && inm.getValorArriendo().compareTo(BigDecimal.ZERO) > 0)
				.collectList()
				.flatMap(inmuebles -> {
					if (inmuebles.isEmpty()) {
						return Mono.<Void>empty();
					}
					if (whatsappProperties.isTemplateRecordatorioPagoEnabled()) {
						List<String> params = construirParametrosPlantilla(a, inmuebles);
						return whatsappNotificationService.sendTemplateToPhone(
										a.getTelefono(),
										whatsappProperties.templateRecordatorioPago().strip(),
										whatsappProperties.getTemplateRecordatorioPagoLanguage(),
										params)
								.doOnSuccess(v -> log.debug("Recordatorio (plantilla) enviado a arrendatario id={} ({})", a.getIdArrendatario(), a.getNombres()));
					}
					String mensaje = construirMensajeTexto(a, inmuebles);
					return whatsappNotificationService.sendToPhone(a.getTelefono(), mensaje)
							.doOnSuccess(v -> log.debug("Recordatorio (texto) enviado a arrendatario id={} ({})", a.getIdArrendatario(), a.getNombres()));
				});
	}

	/** Parámetros para la plantilla en orden: {{1}} nombre, {{2}} total, {{3}} recargo. */
	private List<String> construirParametrosPlantilla(Arrendatario a, List<Inmueble> inmuebles) {
		String nombre = (a.getNombres() != null ? a.getNombres().trim() : "").strip();
		if (nombre.isEmpty() && a.getApellidos() != null) nombre = a.getApellidos().trim();
		if (nombre.isEmpty()) nombre = "Arrendatario";
		BigDecimal total = BigDecimal.ZERO;
		for (Inmueble i : inmuebles) {
			BigDecimal va = i.getValorArriendo() != null ? i.getValorArriendo() : BigDecimal.ZERO;
			total = total.add(va);
		}
		BigDecimal recargo = total.multiply(BigDecimal.valueOf(PORCENTAJE_RECARGO)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
		String totalStr = "$" + total.stripTrailingZeros().toPlainString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1.");
		String recargoStr = "$" + recargo.stripTrailingZeros().toPlainString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1.");
		return List.of(nombre, totalStr, recargoStr);
	}

	private String construirMensajeTexto(Arrendatario a, List<Inmueble> inmuebles) {
		String nombre = (a.getNombres() != null ? a.getNombres().trim() : "").strip();
		if (nombre.isEmpty() && a.getApellidos() != null) {
			nombre = a.getApellidos().trim();
		}
		if (nombre.isEmpty()) {
			nombre = "Arrendatario";
		}
		BigDecimal total = BigDecimal.ZERO;
		StringBuilder detalle = new StringBuilder();
		for (Inmueble i : inmuebles) {
			BigDecimal va = i.getValorArriendo() != null ? i.getValorArriendo() : BigDecimal.ZERO;
			total = total.add(va);
			String titulo = i.getTitulo() != null && !i.getTitulo().isBlank() ? i.getTitulo() : "Inmueble " + i.getIdInmueble();
			detalle.append("• ").append(titulo).append(": $").append(va.stripTrailingZeros().toPlainString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1.")).append("\n");
		}
		BigDecimal recargo = total.multiply(BigDecimal.valueOf(PORCENTAJE_RECARGO)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
		String totalStr = total.stripTrailingZeros().toPlainString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1.");
		String recargoStr = recargo.stripTrailingZeros().toPlainString().replaceAll("(\\d)(?=(\\d{3})+$)", "$1.");
		return "Hola " + nombre + ".\n\n" +
				"*Recordatorio de pago de arriendo*\n\n" +
				"El pago del arriendo vence el *día 5* de cada mes. " +
				"Si no se realiza el pago ese día, se aplicará un recargo del *" + PORCENTAJE_RECARGO + "%* sobre el valor del arriendo.\n\n" +
				"*Detalle:*\n" + detalle +
				"*Total a pagar:* $" + totalStr + "\n" +
				"*Recargo si paga después del 5:* $" + recargoStr + " (" + PORCENTAJE_RECARGO + "%)\n\n" +
				"Gracias por mantener sus pagos al día.";
	}
}
