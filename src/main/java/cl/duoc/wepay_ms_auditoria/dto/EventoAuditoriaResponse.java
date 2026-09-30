package cl.duoc.wepay_ms_auditoria.dto;

import java.time.LocalDateTime;

public record EventoAuditoriaResponse(
        Long id,
        String usuarioOid,
        String tipoEvento,
        String entidadId,
        LocalDateTime fechaEvento,
        String origen,
        String payload
) {}