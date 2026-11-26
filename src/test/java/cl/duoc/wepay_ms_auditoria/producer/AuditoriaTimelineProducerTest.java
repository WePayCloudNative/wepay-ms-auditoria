package cl.duoc.wepay_ms_auditoria.producer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import cl.duoc.wepay_ms_auditoria.dto.EventoAuditadoDTO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class AuditoriaTimelineProducerTest {

    private final ObjectMapper json = JsonMapper.builder().build();

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);

    private final AuditoriaTimelineProducer productor = new AuditoriaTimelineProducer(kafka, json, "audit.timeline");

    @Test
    void publicaElDtoComoJsonConElPedidoComoKey() {
        productor.publicar(new EventoAuditadoDTO("e-1", "OrderAccepted", 15L, "oid-cliente", "oid-admin",
                "ADMIN", "CREADO", "ACEPTADO", LocalDateTime.parse("2026-11-26T03:00:00"),
                LocalDateTime.parse("2026-11-26T03:00:01"), "wepay-ms-auditoria"));

        ArgumentCaptor<String> mensaje = ArgumentCaptor.forClass(String.class);
        verify(kafka).send(eq("audit.timeline"), eq("15"), mensaje.capture());
        JsonNode enviado = json.readTree(mensaje.getValue());
        assertEquals("OrderAccepted", enviado.path("tipoEvento").asText());
        assertEquals(15, enviado.path("pedidoId").asInt());
        assertEquals("ACEPTADO", enviado.path("estadoNuevo").asText());
    }

    @Test
    void sinPedidoNoSePublica() {
        assertThrows(IllegalArgumentException.class, () -> productor.publicar(new EventoAuditadoDTO(
                "e-1", "OrderAccepted", null, null, null, null, null, null, null, null, null)));
    }
}
