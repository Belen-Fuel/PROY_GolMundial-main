package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Registra acciones administrativas realizadas
 * sobre la información del torneo.
 */
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(
            name = "accion",
            nullable = false,
            length = 80
    )
    private String accion;

    @NotBlank
    @Column(
            name = "entidad",
            nullable = false,
            length = 80
    )
    private String entidad;

    @Column(name = "entidad_id")
    private Long entidadId;

    @Column(
            name = "usuario_referencia",
            length = 120
    )
    private String usuarioReferencia;

    @Column(
            name = "detalle",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String detalle;

    @Column(
            name = "fecha_hora_utc",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaHoraUtc;

    public Auditoria() {
    }

    public Auditoria(
            String accion,
            String entidad,
            Long entidadId,
            String usuarioReferencia,
            String detalle
    ) {
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.usuarioReferencia = usuarioReferencia;
        this.detalle = detalle;
    }

    @PrePersist
    public void antesDeInsertar() {
        if (fechaHoraUtc == null) {
            fechaHoraUtc = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getUsuarioReferencia() {
        return usuarioReferencia;
    }

    public String getDetalle() {
        return detalle;
    }

    public OffsetDateTime getFechaHoraUtc() {
        return fechaHoraUtc;
    }
}
