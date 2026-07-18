package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Representa una fase del Mundial 2026.
 *
 * Ejemplos:
 * GRUPOS, DIECISEISAVOS, OCTAVOS, CUARTOS,
 * SEMIFINALES, TERCER_PUESTO y FINAL.
 */
@Entity
@Table(name = "fases")
public class Fase {

    /**
     * Código único de la fase.
     *
     * Ejemplo: GRUPOS.
     */
    @Id
    @NotBlank
    @Size(max = 30)
    @Column(
            name = "codigo",
            nullable = false,
            length = 30
    )
    private String codigo;

    /**
     * Nombre descriptivo de la fase.
     */
    @NotBlank
    @Size(max = 120)
    @Column(
            name = "nombre",
            nullable = false,
            length = 120
    )
    private String nombre;

    /**
     * Rango informativo de fechas proporcionado por el seed.
     *
     * Ejemplo: 2026-06-11 a 2026-06-27.
     */
    @Size(max = 80)
    @Column(
            name = "fechas",
            length = 80
    )
    private String fechas;

    public Fase() {
    }

    public Fase(String codigo, String nombre, String fechas) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.fechas = fechas;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechas() {
        return fechas;
    }

    public void setFechas(String fechas) {
        this.fechas = fechas;
    }
}
