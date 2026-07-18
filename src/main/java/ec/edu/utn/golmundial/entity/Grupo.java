package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Representa uno de los doce grupos del Mundial 2026.
 *
 * Los códigos válidos inicialmente son:
 * A, B, C, D, E, F, G, H, I, J, K y L.
 */
@Entity
@Table(name = "grupos")
public class Grupo {

    /**
     * Código único del grupo.
     *
     * Ejemplo: A.
     */
    @Id
    @NotBlank
    @Size(max = 2)
    @Column(
            name = "codigo",
            nullable = false,
            length = 2
    )
    private String codigo;

    /**
     * Nombre descriptivo.
     *
     * Ejemplo: Grupo A.
     */
    @NotBlank
    @Size(max = 50)
    @Column(
            name = "nombre",
            nullable = false,
            length = 50
    )
    private String nombre;

    public Grupo() {
    }

    public Grupo(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
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
}
