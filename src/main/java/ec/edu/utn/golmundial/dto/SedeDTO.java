package ec.edu.utn.golmundial.dto;

import java.io.Serializable;

public class SedeDTO implements Serializable {

    private Long id;
    private String nombre;
    private String ciudad;
    private String pais;
    private Integer capacidadAprox;

    // Constructor vacío obligatorio
    public SedeDTO() {
    }

    // Constructor completo
    public SedeDTO(Long id, String nombre, String ciudad, String pais, Integer capacidadAprox) {
        this.id = id;
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.pais = pais;
        this.capacidadAprox = capacidadAprox;
    }

    // --- Métodos de Acceso (Getters y Setters) ---

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
