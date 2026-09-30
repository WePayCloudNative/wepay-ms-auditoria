package cl.duoc.wepay_ms_auditoria.controller;

import cl.duoc.wepay_ms_auditoria.dto.EventoAuditoriaResponse;
import cl.duoc.wepay_ms_auditoria.service.AuditoriaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    // Obtener todos los eventos históricos
    @GetMapping
    public List<EventoAuditoriaResponse> obtenerTodos() {
        return auditoriaService.obtenerTodos();
    }

    // Obtener el historial de un pedido específico (Ej: /api/audit/entidad/15)
    @GetMapping("/entidad/{entidadId}")
    public List<EventoAuditoriaResponse> obtenerPorEntidad(@PathVariable String entidadId) {
        return auditoriaService.obtenerTimelinePorEntidad(entidadId);
    }

    // Obtener el historial de acciones de un usuario (Ej: /api/audit/usuario/oid-azure)
    @GetMapping("/usuario/{oid}")
    public List<EventoAuditoriaResponse> obtenerPorUsuario(@PathVariable String oid) {
        return auditoriaService.obtenerTimelinePorUsuario(oid);
    }
}