package cl.duoc.wepay_ms_auditoria.dto;

import java.time.LocalDateTime;

/**
 * Lo que se guarda de cada evento de orders.events.
 * usuarioOid es el dueño del pedido; actorOid/actorRol es quien hizo el cambio.
 * Los eventos antiguos (antes de registrar el actor) llegan con esos campos en null.
 */
public record DatosEvento(
        String eventId,
        LocalDateTime fechaEvento,
        String usuarioOid,
        String tipoEvento,
        String entidadId,
        String origen,
        String payload,
        String actorOid,
        String actorRol,
        String estadoAnterior,
        String estadoNuevo) {
}
