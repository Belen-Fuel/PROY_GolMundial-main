package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Implementa un registro de salida u outbox.
 *
 * El evento permanece almacenado hasta que el backend
 * UTNGolCoin confirme su recepción.
 */
@Entity
@Table(name = "eventos_integracion")
public class EventoIntegracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "tipo_evento",
            nullable = false,
            length = 100
    )
    private String tipoEvento;

    @Column(
            name = "entidad",
            nullable = false,
            length = 80
    )
    private String entidad;

    @Column(
            name = "entidad_id",
            nullable = false
    )
    private Long entidadId;

    @Column(
            name = "payload_json",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoEventoIntegracion estado;

    @Column(
            name = "intentos",
            nullable = false
    )
    private int intentos;

    @Column(
            name = "ultimo_error",
            columnDefinition = "TEXT"
    )
    private String ultimoError;

    @Column(
            name = "fecha_creacion_utc",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaCreacionUtc;

    @Column(
            name = "fecha_actualizacion_utc",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaActualizacionUtc;

    public EventoIntegracion() {
    }

    public EventoIntegracion(
            String tipoEvento,
            String entidad,
            Long entidadId,
            String payloadJson
    ) {
        this.tipoEvento = tipoEvento;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.payloadJson = payloadJson;
        this.estado = EstadoEventoIntegracion.PENDIENTE;
        this.intentos = 0;
    }

    @PrePersist
    public void antesDeInsertar() {
        OffsetDateTime ahora =
                OffsetDateTime.now(ZoneOffset.UTC);

        fechaCreacionUtc = ahora;
        fechaActualizacionUtc = ahora;

        if (estado == null) {
            estado = EstadoEventoIntegracion.PENDIENTE;
        }
    }

    @PreUpdate
    public void antesDeActualizar() {
        fechaActualizacionUtc =
                OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public EstadoEventoIntegracion getEstado() {
        return estado;
    }

    public void setEstado(
            EstadoEventoIntegracion estado
    ) {
        this.estado = estado;
    }

    public int getIntentos() {
        return intentos;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }

    public OffsetDateTime getFechaCreacionUtc() {
        return fechaCreacionUtc;
    }

    public OffsetDateTime getFechaActualizacionUtc() {
        return fechaActualizacionUtc;
    }
}
