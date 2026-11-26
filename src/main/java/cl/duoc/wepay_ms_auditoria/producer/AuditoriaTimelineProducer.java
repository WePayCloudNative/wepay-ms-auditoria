package cl.duoc.wepay_ms_auditoria.producer;

import cl.duoc.wepay_ms_auditoria.dto.EventoAuditadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Publica en audit.timeline cada evento que auditoría registró.
 * Key = número de pedido: todos los eventos de un pedido van a la misma partición, en orden.
 */
@Component
public class AuditoriaTimelineProducer {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaTimelineProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topico;

    public AuditoriaTimelineProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topic.timeline:audit.timeline}") String topico) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topico = topico;
    }

    public void publicar(EventoAuditadoDTO evento) {
        if (evento == null || evento.pedidoId() == null) {
            throw new IllegalArgumentException("El evento auditado debe tener pedidoId");
        }
        String json = objectMapper.writeValueAsString(evento);
        kafkaTemplate.send(topico, String.valueOf(evento.pedidoId()), json);
        log.info("Publicado en {}: {} del pedido #{}", topico, evento.tipoEvento(), evento.pedidoId());
    }
}
