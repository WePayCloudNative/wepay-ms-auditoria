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
    public boolean registrarEvento(
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
            return false;
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
        return true;
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

    @Transactional(readOnly = true)
    public List<EventoAuditoriaResponse> filtrarEventos(
            String tipoEvento,
            LocalDateTime desde,
            LocalDateTime hasta) {

        List<EventoAuditoria> eventos;

        // Caso 1: filtrar por tipo y rango de fechas
        if (tipoEvento != null && !tipoEvento.isBlank()
                && desde != null && hasta != null) {

            eventos = repository
                    .findByTipoEventoAndFechaEventoBetweenOrderByFechaEventoDesc(
                            tipoEvento, desde, hasta);

            // Caso 2: filtrar solo por tipo de evento
        } else if (tipoEvento != null && !tipoEvento.isBlank()) {

            eventos = repository
                    .findByTipoEventoOrderByFechaEventoDesc(tipoEvento);

            // Caso 3: filtrar solo por rango de fechas
        } else if (desde != null && hasta != null) {

            eventos = repository
                    .findByFechaEventoBetweenOrderByFechaEventoDesc(
                            desde, hasta);

            // Caso 4: sin filtros
        } else {

            eventos = repository.findAll();
        }

        return eventos.stream()
                .map(this::convertirADto)
                .toList();
    }

}