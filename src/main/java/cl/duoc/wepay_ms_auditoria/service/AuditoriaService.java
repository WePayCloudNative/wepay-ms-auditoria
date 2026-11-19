package cl.duoc.wepay_ms_auditoria.service;

import cl.duoc.wepay_ms_auditoria.dto.EventoAuditoriaResponse;
import cl.duoc.wepay_ms_auditoria.entity.EventoAuditoria;
import cl.duoc.wepay_ms_auditoria.repository.EventoAuditoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final EventoAuditoriaRepository repository;

    public AuditoriaService(EventoAuditoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrarEvento(
            String eventId,
            LocalDateTime fechaEvento,
            String usuarioOid,
            String tipoEvento,
            String entidadId,
            String origen,
            String payload) {

        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException(
                    "El evento Kafka debe tener un eventId");
        }

        if (repository.existsByEventId(eventId)) {
            System.out.println(
                    "Evento duplicado ignorado: " + eventId);
            return;
        }

        EventoAuditoria evento = new EventoAuditoria(
                usuarioOid,
                tipoEvento,
                entidadId,
                fechaEvento,
                origen,
                payload);

        evento.setEventId(eventId);

        repository.saveAndFlush(evento);

        System.out.println(
                "Evento registrado correctamente: " + eventId);
    }

    // Métodos de solo lectura para el panel de administración
    public List<EventoAuditoriaResponse> obtenerTodos() {
        return repository.findAll().stream()
                .map(this::convertirADto)
                .toList();
    }

    public List<EventoAuditoriaResponse> obtenerTimelinePorEntidad(String entidadId) {
        return repository.findByEntidadIdOrderByFechaEventoDesc(entidadId).stream()
                .map(this::convertirADto)
                .toList();
    }

    public List<EventoAuditoriaResponse> obtenerTimelinePorUsuario(String usuarioOid) {
        return repository.findByUsuarioOidOrderByFechaEventoDesc(usuarioOid).stream()
                .map(this::convertirADto)
                .toList();
    }

    private EventoAuditoriaResponse convertirADto(EventoAuditoria e) {
        return new EventoAuditoriaResponse(
                e.getId(),
                e.getUsuarioOid(),
                e.getTipoEvento(),
                e.getEntidadId(),
                e.getFechaEvento(),
                e.getOrigen(),
                e.getPayload());
    }
}