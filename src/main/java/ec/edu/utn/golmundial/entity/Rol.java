package ec.edu.utn.golmundial.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Representa un rol de acceso dentro de la plataforma.
 *
 * Roles iniciales:
 * ADMINISTRADOR, USUARIO e INVITADO.
 */
@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rol_nombre",
                        columnNames = "nombre"
                )
        }
)
public class Rol {

    /**
     * Identificador original definido en el seed.
     */
    @Id
    @NotNull
    @Column(
            name = "id",
            nullable = false
    )
    private Long id;

    @NotBlank
    @Size(max = 40)
    @Column(
            name = "nombre",
            nullable = false,
            length = 40
    )
    private String nombre;

    @Size(max = 200)
    @Column(
            name = "descripcion",
            length = 200
    )
    private String descripcion;

    public Rol() {
    }

    public Rol(
            Long id,
            String nombre,
            String descripcion
    ) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
