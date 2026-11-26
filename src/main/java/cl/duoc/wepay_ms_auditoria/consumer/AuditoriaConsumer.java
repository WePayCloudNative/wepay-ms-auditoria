
package cl.duoc.wepay_ms_auditoria.consumer;

import cl.duoc.wepay_ms_auditoria.dto.DatosEvento;
import cl.duoc.wepay_ms_auditoria.service.AuditoriaService;

import java.time.LocalDateTime;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import cl.duoc.wepay_ms_auditoria.producer.AuditoriaTimelineProducer;

@Component
public class AuditoriaConsumer {

    private final AuditoriaService auditoriaService;
    private final ObjectMapper objectMapper;
    private final AuditoriaTimelineProducer timelineProducer;

    public AuditoriaConsumer(
            AuditoriaService auditoriaService,
            ObjectMapper objectMapper,
            AuditoriaTimelineProducer timelineProducer) {

        this.auditoriaService = auditoriaService;
        this.objectMapper = objectMapper;
        this.timelineProducer = timelineProducer;
    }

    @KafkaListener(topics = "${app.kafka.topic.orders}", groupId = "${spring.kafka.consumer.group-id}")
    public void recibirEvento(String mensaje) {

        // Evitar procesar mensajes vacíos
        if (mensaje == null || mensaje.isBlank()) {
            return;
        }

        // Convertir el JSON recibido desde Kafka
        JsonNode evento = objectMapper.readTree(mensaje);

        // Obtener el identificador único del evento
        String eventId = evento.path("eventId").asText();

        String tipoEvento = evento.path("tipoEvento").asText();

        String usuarioOid = evento.path("usuarioOid").asText();

        String pedidoId = evento.path("pedidoId").asText();

        String fechaEventoTexto = evento.path("fechaEvento").asText();

        LocalDateTime fechaEvento = LocalDateTime.parse(fechaEventoTexto);

        System.out.println(
                "=== EVENTO RECIBIDO EN AUDITORIA ===");

        System.out.println("EventId: " + eventId);
        System.out.println("Tipo: " + tipoEvento);
        System.out.println("Usuario: " + usuarioOid);
        System.out.println("Pedido: " + pedidoId);

        // Guardar el evento, validando duplicados en el servicio

        boolean eventoNuevo = auditoriaService.registrarEvento(
                leerDatos(evento, fechaEvento, mensaje));

        if (eventoNuevo) {

            timelineProducer.publicarEvento(
                    pedidoId,
                    mensaje);

            System.out.println(
                    "Publicacion solicitada en audit.timeline: " + eventId);

        } else {

            System.out.println(
                    "Evento duplicado: no se vuelve a publicar en audit.timeline");
        }

    }

    /**
     * Arma lo que se guarda a partir del JSON de Kafka.
     * Los campos del actor son opcionales: los eventos antiguos no los traen.
     */
    static DatosEvento leerDatos(JsonNode evento, LocalDateTime fechaEvento, String mensaje) {
        return new DatosEvento(
                texto(evento, "eventId"),
                fechaEvento,
                texto(evento, "usuarioOid"),
                texto(evento, "tipoEvento"),
                texto(evento, "pedidoId"),
                "wepay-ms-pedidos",
                mensaje,
                texto(evento, "actorOid"),
                texto(evento, "actorRol"),
                texto(evento, "estadoAnterior"),
                texto(evento, "estado"));
    }

    /** Texto del campo, o null si no viene, viene en null o vacío. */
    private static String texto(JsonNode evento, String campo) {
        JsonNode valor = evento.path(campo);
        if (valor.isMissingNode() || valor.isNull()) {
            return null;
        }
        String texto = valor.asText();
        return texto.isBlank() ? null : texto;
    }
}
