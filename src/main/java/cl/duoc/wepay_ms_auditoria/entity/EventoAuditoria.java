package cl.duoc.wepay_ms_auditoria.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_eventos")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quién (El usuario que disparó el evento)
    private String usuarioOid;

    // Qué (Ej: OrderCreated, OrderAccepted)
    private String tipoEvento;

    // A qué entidad afectó (Ej: El ID del pedido)
    private String entidadId;

    // Cuándo ocurrió
    private LocalDateTime fechaEvento;

    // Desde dónde (Ej: Detalles técnicos, IP, origen, etc.)
    private String origen;
    
    // El mensaje original en formato JSON (para trazabilidad completa)
    private String payload;

    public EventoAuditoria() {}

    public EventoAuditoria(String usuarioOid, String tipoEvento, String entidadId, LocalDateTime fechaEvento, String origen, String payload) {
        this.usuarioOid = usuarioOid;
        this.tipoEvento = tipoEvento;
        this.entidadId = entidadId;
        this.fechaEvento = fechaEvento;
        this.origen = origen;
        this.payload = payload;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuarioOid() { return usuarioOid; }
    public void setUsuarioOid(String usuarioOid) { this.usuarioOid = usuarioOid; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getEntidadId() { return entidadId; }
    public void setEntidadId(String entidadId) { this.entidadId = entidadId; }

    public LocalDateTime getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDateTime fechaEvento) { this.fechaEvento = fechaEvento; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}