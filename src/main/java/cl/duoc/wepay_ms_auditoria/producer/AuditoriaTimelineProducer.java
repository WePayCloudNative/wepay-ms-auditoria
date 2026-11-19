
package cl.duoc.wepay_ms_auditoria.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaTimelineProducer {

    private static final String TOPICO = "audit.timeline";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public AuditoriaTimelineProducer(
            KafkaTemplate<String, String> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publicarEvento(
            String entidadId,
            String mensaje) {

        if (entidadId == null || entidadId.isBlank()) {
            throw new IllegalArgumentException(
                    "El evento debe tener una entidadId");
        }

        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException(
                    "El mensaje de auditoria no puede estar vacio");
        }

        kafkaTemplate.send(
                TOPICO,
                entidadId,
                mensaje
        );
    }
}
