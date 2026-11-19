package cl.duoc.wepay_ms_auditoria.controller;

import cl.duoc.wepay_ms_auditoria.dto.EventoAuditoriaResponse;
import cl.duoc.wepay_ms_auditoria.service.AuditoriaService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    // Obtener el historial de acciones de un usuario (Ej:
    // /api/audit/usuario/oid-azure)
    @GetMapping("/usuario/{oid}")
    public List<EventoAuditoriaResponse> obtenerPorUsuario(@PathVariable String oid) {
        return auditoriaService.obtenerTimelinePorUsuario(oid);
    }

    @GetMapping("/filter")
    public List<EventoAuditoriaResponse> filtrarEventos(
            @RequestParam(required = false) String tipoEvento,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {

        // Exigir ambas fechas cuando se utiliza un rango
        if ((desde == null) != (hasta == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debe indicar ambas fechas: desde y hasta");
        }

        // Verificar que el rango sea correcto
        if (desde != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha desde no puede ser posterior a hasta");
        }

        return auditoriaService.filtrarEventos(
                tipoEvento,
                desde,
                hasta);
    }

}