package cl.duoc.wepay_ms_auditoria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.duoc.wepay_ms_auditoria.dto.DatosEvento;
import cl.duoc.wepay_ms_auditoria.entity.EventoAuditoria;
import cl.duoc.wepay_ms_auditoria.repository.EventoAuditoriaRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuditoriaServiceTest {

    private final EventoAuditoriaRepository repositorio = mock(EventoAuditoriaRepository.class);
    private final AuditoriaService servicio = new AuditoriaService(repositorio);

    private static DatosEvento datos(String eventId) {
        return new DatosEvento(eventId, LocalDateTime.parse("2026-11-24T20:00:00"), "oid-cliente",
                "OrderCancelled", "15", "wepay-ms-pedidos", "{}", "oid-admin", "ADMIN", "ACEPTADO", "CANCELADO");
    }

    @Test
    void guardaElEventoConSuActor() {
        assertTrue(servicio.registrarEvento(datos("e-1")));

        ArgumentCaptor<EventoAuditoria> guardado = ArgumentCaptor.forClass(EventoAuditoria.class);
        verify(repositorio).saveAndFlush(guardado.capture());
        assertEquals("e-1", guardado.getValue().getEventId());
        assertEquals("oid-admin", guardado.getValue().getActorOid());
        assertEquals("ACEPTADO", guardado.getValue().getEstadoAnterior());
        assertEquals("CANCELADO", guardado.getValue().getEstadoNuevo());
    }

    @Test
    void unEventoRepetidoNoSeGuardaDosVeces() {
        when(repositorio.existsByEventId("e-1")).thenReturn(true);

        assertFalse(servicio.registrarEvento(datos("e-1")));
        verify(repositorio, never()).saveAndFlush(any());
    }

    @Test
    void sinEventIdSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> servicio.registrarEvento(datos(" ")));
    }
}
