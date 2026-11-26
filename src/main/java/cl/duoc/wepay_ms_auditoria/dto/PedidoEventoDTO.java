package cl.duoc.wepay_ms_auditoria.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * Evento que ms-pedidos publica en orders.events, leído como objeto tipado.
 * Se ignoran los campos que auditoría no usa (por ejemplo, los productos).
 * actorOid, actorRol y estadoAnterior vienen null en eventos antiguos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoEventoDTO(
        String eventId,
        String tipoEvento,
        Long pedidoId,
        String usuarioOid,
        Double total,
        String estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaEvento,
        String actorOid,
        String actorRol,
        String estadoAnterior) {

    /** Un evento sin estos datos no se puede auditar: se rechaza y termina en la DLT. */
    public void validar() {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("El evento no contiene eventId");
        }
        if (tipoEvento == null || tipoEvento.isBlank()) {
            throw new IllegalArgumentException("El evento " + eventId + " no contiene tipoEvento");
        }
        if (pedidoId == null) {
            throw new IllegalArgumentException("El evento " + eventId + " no contiene pedidoId");
        }
        if (fechaEvento == null) {
            throw new IllegalArgumentException("El evento " + eventId + " no contiene fechaEvento");
        }
    }
}
