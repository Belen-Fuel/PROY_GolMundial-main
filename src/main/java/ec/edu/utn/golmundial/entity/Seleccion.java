package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Representa una de las 48 selecciones participantes
 * en el Mundial 2026.
 */
@Entity
@Table(
        name = "selecciones",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_seleccion_codigo_fifa",
                        columnNames = "codigo_fifa"
                ),
                @UniqueConstraint(
                        name = "uk_seleccion_nombre",
                        columnNames = "nombre"
                )
        }
)
public class Seleccion {

    /**
     * Identificador original del seed.
     */
    @Id
    @NotNull
    @Column(
            name = "id",
            nullable = false
    )
    private Long id;

    /**
     * Código FIFA de tres letras.
     *
     * Ejemplos: ECU, MEX, ARG.
     */
    @NotBlank
    @Size(min = 3, max = 3)
    @Column(
            name = "codigo_fifa",
            nullable = false,
            length = 3
    )
    private String codigoFifa;

    @NotBlank
    @Size(max = 100)
    @Column(
            name = "nombre",
            nullable = false,
            length = 100
    )
    private String nombre;

    /**
     * Grupo al que pertenece la selección.
     */
    @NotNull
    @ManyToOne(
            fetch = FetchType.EAGER,
            optional = false
    )
    @JoinColumn(
            name = "grupo_codigo",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_seleccion_grupo"
            )
    )
    private Grupo grupo;

    @NotBlank
    @Size(max = 30)
    @Column(
            name = "confederacion",
            nullable = false,
            length = 30
    )
    private String confederacion;

    @Column(
            name = "es_anfitrion",
            nullable = false
    )
    private boolean esAnfitrion;

    /**
     * Forma mediante la cual obtuvo la clasificación.
     */
    @Size(max = 150)
    @Column(
            name = "clasificacion",
            length = 150
    )
    private String clasificacion;

    public Seleccion() {
    }

    public Seleccion(
            Long id,
            String codigoFifa,
            String nombre,
            Grupo grupo,
            String confederacion,
            boolean esAnfitrion,
            String clasificacion
    ) {
        this.id = id;
        this.codigoFifa = codigoFifa;
        this.nombre = nombre;
        this.grupo = grupo;
        this.confederacion = confederacion;
        this.esAnfitrion = esAnfitrion;
        this.clasificacion = clasificacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoFifa() {
        return codigoFifa;
    }

    public void setCodigoFifa(String codigoFifa) {
        this.codigoFifa = codigoFifa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Grupo getGrupo() {
        return grupo;
    }

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
    }

    public String getConfederacion() {
        return confederacion;
    }

    public void setConfederacion(String confederacion) {
        this.confederacion = confederacion;
    }

    public boolean isEsAnfitrion() {
        return esAnfitrion;
    }

    public void setEsAnfitrion(boolean esAnfitrion) {
        this.esAnfitrion = esAnfitrion;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }
}
