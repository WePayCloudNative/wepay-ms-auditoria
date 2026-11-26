package cl.duoc.wepay_ms_auditoria.consumer;

import cl.duoc.wepay_ms_auditoria.dto.DatosEvento;
import cl.duoc.wepay_ms_auditoria.dto.EventoAuditadoDTO;
import cl.duoc.wepay_ms_auditoria.dto.PedidoEventoDTO;
import cl.duoc.wepay_ms_auditoria.producer.AuditoriaTimelineProducer;
import cl.duoc.wepay_ms_auditoria.service.AuditoriaService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Capa de mensajería: recibe de Kafka, convierte a DTO, valida y delega.
 * La lógica de negocio (guardar, evitar duplicados) está en AuditoriaService.
 *
 * Si algo falla (JSON mal formado, datos faltantes, error de Oracle), la excepción sale de aquí
 * y KafkaErrorConfig reintenta 2 veces y luego manda el mensaje a orders.events.auditoria.DLT.
 */
@Component
public class AuditoriaConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaConsumer.class);

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

        if (mensaje == null || mensaje.isBlank()) {
            log.warn("Mensaje vacío ignorado");
            return;
        }

        PedidoEventoDTO evento = objectMapper.readValue(mensaje, PedidoEventoDTO.class);
        evento.validar();

        log.info("Evento recibido: {} del pedido #{} (eventId {})",
                evento.tipoEvento(), evento.pedidoId(), evento.eventId());

        // Guarda el evento; devuelve false si ya estaba (mismo eventId)
        boolean eventoNuevo = auditoriaService.registrarEvento(aDatos(evento, mensaje));

        if (eventoNuevo) {
            timelineProducer.publicar(EventoAuditadoDTO.desde(evento, LocalDateTime.now()));
        } else {
            log.info("Evento duplicado: no se vuelve a publicar en audit.timeline ({})", evento.eventId());
        }
    }

    /** Lo que se guarda en Oracle: los datos del evento más el JSON original como respaldo. */
    static DatosEvento aDatos(PedidoEventoDTO evento, String mensaje) {
        return new DatosEvento(
                evento.eventId(),
                evento.fechaEvento(),
                evento.usuarioOid(),
                evento.tipoEvento(),
                String.valueOf(evento.pedidoId()),
                "wepay-ms-pedidos",
                mensaje,
                evento.actorOid(),
                evento.actorRol(),
                evento.estadoAnterior(),
                evento.estado());
    }
}
