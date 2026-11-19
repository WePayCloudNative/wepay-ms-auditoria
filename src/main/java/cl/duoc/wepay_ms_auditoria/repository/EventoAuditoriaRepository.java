package cl.duoc.wepay_ms_auditoria.repository;

import cl.duoc.wepay_ms_auditoria.entity.EventoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, Long> {

    boolean existsByEventId(String eventId);

    // Permite ver la línea de tiempo completa de un pedido específico
    List<EventoAuditoria> findByEntidadIdOrderByFechaEventoDesc(String entidadId);

    // Permite ver todas las acciones realizadas por un usuario específico
    List<EventoAuditoria> findByUsuarioOidOrderByFechaEventoDesc(String usuarioOid);
}