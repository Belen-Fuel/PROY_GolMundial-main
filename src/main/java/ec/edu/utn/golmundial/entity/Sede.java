package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Representa una sede del Mundial 2026.
 *
 * El identificador se conserva exactamente como aparece
 * en el seed proporcionado por el docente.
 */
@Entity
@Table(
        name = "sedes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sede_nombre",
                        columnNames = "nombre"
                )
        }
)
public class Sede {

    @Id
    @NotNull
    @Column(
            name = "id",
            nullable = false
    )
    private Long id;

    @NotBlank
    @Size(max = 180)
    @Column(
            name = "nombre",
            nullable = false,
            length = 180
    )
    private String nombre;

    @NotBlank
    @Size(max = 100)
    @Column(
            name = "ciudad",
            nullable = false,
            length = 100
    )
    private String ciudad;

    @NotBlank
    @Size(max = 80)
    @Column(
            name = "pais",
            nullable = false,
            length = 80
    )
    private String pais;

    @Min(1)
    @Column(name = "capacidad_aprox")
    private Integer capacidadAprox;

    public Sede() {
    }

    public Sede(
            Long id,
            String nombre,
            String ciudad,
            String pais,
            Integer capacidadAprox
    ) {
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.pais = pais;
        this.capacidadAprox = capacidadAprox;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Integer getCapacidadAprox() {
        return capacidadAprox;
    }

    public void setCapacidadAprox(Integer capacidadAprox) {
        this.capacidadAprox = capacidadAprox;
    }
}
