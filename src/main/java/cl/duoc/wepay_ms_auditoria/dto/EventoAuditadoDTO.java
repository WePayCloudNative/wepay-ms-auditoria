package cl.duoc.wepay_ms_auditoria.dto;

import java.time.LocalDateTime;

/**
 * Lo que auditoría publica en audit.timeline después de registrar un evento en Oracle.
 * ms-pedidos lo consume para marcar el pedido como auditado.
 */
public record EventoAuditadoDTO(
        String eventId,
        String tipoEvento,
        Long pedidoId,
        String usuarioOid,
        String actorOid,
        String actorRol,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaEvento,
        LocalDateTime fechaAuditoria,
        String origen) {

    public static EventoAuditadoDTO desde(PedidoEventoDTO evento, LocalDateTime fechaAuditoria) {
        return new EventoAuditadoDTO(
                evento.eventId(),
                evento.tipoEvento(),
                evento.pedidoId(),
                evento.usuarioOid(),
                evento.actorOid(),
                evento.actorRol(),
                evento.estadoAnterior(),
                evento.estado(),
                evento.fechaEvento(),
                fechaAuditoria,
                "wepay-ms-auditoria");
    }
}
