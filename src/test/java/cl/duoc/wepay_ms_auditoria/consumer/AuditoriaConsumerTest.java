package cl.duoc.wepay_ms_auditoria.consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.wepay_ms_auditoria.dto.DatosEvento;
import cl.duoc.wepay_ms_auditoria.producer.AuditoriaTimelineProducer;
import cl.duoc.wepay_ms_auditoria.service.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** El consumidor recibe el texto de Kafka; aquí se le pasa a mano, sin un Kafka real. */
class AuditoriaConsumerTest {

    private static final String EVENTO_NUEVO = """
            {"eventId":"e-1","tipoEvento":"OrderDispatched","pedidoId":15,
             "usuarioOid":"oid-cliente","estado":"DESPACHADO","fechaEvento":"2026-11-24T20:00:00",
             "actorOid":"oid-admin","actorRol":"ADMIN","estadoAnterior":"EN_PREPARACION"}
            """;

    // Así eran los eventos antes de registrar el actor
    private static final String EVENTO_ANTIGUO = """
            {"eventId":"e-0","tipoEvento":"OrderCreated","pedidoId":14,
             "usuarioOid":"oid-cliente","estado":"CREADO","fechaEvento":"2026-11-23T10:00:00"}
            """;

    private final ObjectMapper json = JsonMapper.builder().build();
    private AuditoriaService servicio;
    private AuditoriaTimelineProducer timeline;
    private AuditoriaConsumer consumidor;

    @BeforeEach
    void preparar() {
        servicio = mock(AuditoriaService.class);
        timeline = mock(AuditoriaTimelineProducer.class);
        consumidor = new AuditoriaConsumer(servicio, json, timeline);
    }

    private DatosEvento guardado() {
        ArgumentCaptor<DatosEvento> datos = ArgumentCaptor.forClass(DatosEvento.class);
        verify(servicio).registrarEvento(datos.capture());
        return datos.getValue();
    }

    @Test
    void guardaQuienHizoElCambioYElCambioDeEstado() {
        when(servicio.registrarEvento(any())).thenReturn(true);

        consumidor.recibirEvento(EVENTO_NUEVO);

        DatosEvento datos = guardado();
        assertEquals("oid-cliente", datos.usuarioOid());
        assertEquals("oid-admin", datos.actorOid());
        assertEquals("ADMIN", datos.actorRol());
        assertEquals("EN_PREPARACION", datos.estadoAnterior());
        assertEquals("DESPACHADO", datos.estadoNuevo());
        assertEquals("15", datos.entidadId());
    }

    @Test
    void aceptaEventosAntiguosSinActor() {
        when(servicio.registrarEvento(any())).thenReturn(true);

        consumidor.recibirEvento(EVENTO_ANTIGUO);

        DatosEvento datos = guardado();
        assertNull(datos.actorOid());
        assertNull(datos.actorRol());
        assertNull(datos.estadoAnterior());
        assertEquals("CREADO", datos.estadoNuevo());
    }

    @Test
    void unEventoNuevoSePublicaEnElTimeline() {
        when(servicio.registrarEvento(any())).thenReturn(true);

        consumidor.recibirEvento(EVENTO_NUEVO);

        verify(timeline).publicarEvento("15", EVENTO_NUEVO);
    }

    @Test
    void unEventoDuplicadoNoSeVuelveAPublicar() {
        when(servicio.registrarEvento(any())).thenReturn(false);

        consumidor.recibirEvento(EVENTO_NUEVO);

        verify(timeline, never()).publicarEvento(anyString(), anyString());
    }

    @Test
    void ignoraMensajesVacios() {
        consumidor.recibirEvento("  ");

        verify(servicio, never()).registrarEvento(any());
    }
}
