package cl.duoc.wepay_ms_auditoria.repository;

import cl.duoc.wepay_ms_auditoria.entity.EventoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    boolean existsByEventId(String eventId);

    // Permite ver la línea de tiempo completa de un pedido específico
    List<EventoAuditoria> findByEntidadIdOrderByFechaEventoDesc(String entidadId);

    // Permite ver todas las acciones realizadas por un usuario específico
    List<EventoAuditoria> findByUsuarioOidOrderByFechaEventoDesc(String usuarioOid);

    // Filtrar eventos por tipo
    List<EventoAuditoria> findByTipoEventoOrderByFechaEventoDesc(
            String tipoEvento);

    // Filtrar eventos por rango de fechas
    List<EventoAuditoria> findByFechaEventoBetweenOrderByFechaEventoDesc(
            LocalDateTime desde,
            LocalDateTime hasta);

    // Filtrar por tipo de evento y rango de fechas
    List<EventoAuditoria> findByTipoEventoAndFechaEventoBetweenOrderByFechaEventoDesc(
            String tipoEvento,
            LocalDateTime desde,
            LocalDateTime hasta);

}