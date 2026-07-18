package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Representa un partido del Mundial 2026.
 */
@Entity
@Table(
        name = "partidos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_partido_numero_fifa",
                        columnNames = "numero_partido_fifa"
                )
        }
)
public class Partido {

    /**
     * Identificador original del seed.
     *
     * Debe conservarse porque el backend UTNGolCoin
     * utilizará este ID para referenciar las predicciones.
     */
    @Id
    @NotNull
    @Column(
            name = "id",
            nullable = false
    )
    private Long id;

    @NotNull
    @Min(1)
    @Column(
            name = "numero_partido_fifa",
            nullable = false
    )
    private Integer numeroPartidoFifa;

    @NotNull
    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "fase_codigo",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_partido_fase"
            )
    )
    private Fase fase;

    /**
     * Es nulo en las fases eliminatorias.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "grupo_codigo",
            foreignKey = @ForeignKey(
                    name = "fk_partido_grupo"
            )
    )
    private Grupo grupo;

    /**
     * Puede ser nulo temporalmente en eliminatorias
     * mientras no se conozca el clasificado.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "seleccion_local_id",
            foreignKey = @ForeignKey(
                    name = "fk_partido_seleccion_local"
            )
    )
    private Seleccion seleccionLocal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "seleccion_visitante_id",
            foreignKey = @ForeignKey(
                    name = "fk_partido_seleccion_visitante"
            )
    )
    private Seleccion seleccionVisitante;

    @NotNull
    @Column(
            name = "fecha_hora_utc",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaHoraUtc;

    /**
     * Fecha proporcionada por el seed en hora del Este.
     */
    @NotNull
    @Column(
            name = "fecha_hora_et",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaHoraEt;

    @NotNull
    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "sede_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_partido_sede"
            )
    )
    private Sede sede;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoPartido estado = EstadoPartido.PROGRAMADO;

    @Min(0)
    @Column(name = "goles_local")
    private Integer golesLocal;

    @Min(0)
    @Column(name = "goles_visitante")
    private Integer golesVisitante;

    @DecimalMin("1.00")
    @Column(
            name = "cuota_local",
            precision = 8,
            scale = 2
    )
    private BigDecimal cuotaLocal;

    @DecimalMin("1.00")
    @Column(
            name = "cuota_empate",
            precision = 8,
            scale = 2
    )
    private BigDecimal cuotaEmpate;

    @DecimalMin("1.00")
    @Column(
            name = "cuota_visitante",
            precision = 8,
            scale = 2
    )
    private BigDecimal cuotaVisitante;

    /**
     * Indica si el resultado ya fue enviado al backend
     * UTNGolCoin para liquidar las predicciones.
     */
    @Column(
            name = "resultado_notificado",
            nullable = false
    )
    private boolean resultadoNotificado = false;

    @Column(
            name = "fecha_actualizacion",
            nullable = false,
            columnDefinition = "TIMESTAMP WITH TIME ZONE"
    )
    private OffsetDateTime fechaActualizacion;

    public Partido() {
    }

    @PrePersist
    public void antesDeInsertar() {
        fechaActualizacion = OffsetDateTime.now(ZoneOffset.UTC);

        if (estado == null) {
            estado = EstadoPartido.PROGRAMADO;
        }
    }

    @PreUpdate
    public void antesDeActualizar() {
        fechaActualizacion = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumeroPartidoFifa() {
        return numeroPartidoFifa;
    }

    public void setNumeroPartidoFifa(Integer numeroPartidoFifa) {
        this.numeroPartidoFifa = numeroPartidoFifa;
    }

    public Fase getFase() {
        return fase;
    }

    public void setFase(Fase fase) {
        this.fase = fase;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public Seleccion getSeleccionLocal() {
        return seleccionLocal;
    }

    public void setSeleccionLocal(Seleccion seleccionLocal) {
        this.seleccionLocal = seleccionLocal;
    }

    public Seleccion getSeleccionVisitante() {
        return seleccionVisitante;
    }

    public void setSeleccionVisitante(Seleccion seleccionVisitante) {
        this.seleccionVisitante = seleccionVisitante;
    }

    public OffsetDateTime getFechaHoraUtc() {
        return fechaHoraUtc;
    }

    public void setFechaHoraUtc(OffsetDateTime fechaHoraUtc) {
        this.fechaHoraUtc = fechaHoraUtc;
    }

    public OffsetDateTime getFechaHoraEt() {
        return fechaHoraEt;
    }

    public void setFechaHoraEt(OffsetDateTime fechaHoraEt) {
        this.fechaHoraEt = fechaHoraEt;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public EstadoPartido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPartido estado) {
        this.estado = estado;
    }

    public Integer getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(Integer golesLocal) {
        this.golesLocal = golesLocal;
    }

    public Integer getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(Integer golesVisitante) {
        this.golesVisitante = golesVisitante;
    }

    public BigDecimal getCuotaLocal() {
        return cuotaLocal;
    }

    public void setCuotaLocal(BigDecimal cuotaLocal) {
        this.cuotaLocal = cuotaLocal;
    }

    public BigDecimal getCuotaEmpate() {
        return cuotaEmpate;
    }

    public void setCuotaEmpate(BigDecimal cuotaEmpate) {
        this.cuotaEmpate = cuotaEmpate;
    }

    public BigDecimal getCuotaVisitante() {
        return cuotaVisitante;
    }

    public void setCuotaVisitante(BigDecimal cuotaVisitante) {
        this.cuotaVisitante = cuotaVisitante;
    }

    public boolean isResultadoNotificado() {
        return resultadoNotificado;
    }

    public void setResultadoNotificado(boolean resultadoNotificado) {
        this.resultadoNotificado = resultadoNotificado;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(
            OffsetDateTime fechaActualizacion
    ) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
