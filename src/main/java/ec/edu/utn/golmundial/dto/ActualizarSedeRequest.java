package ec.edu.utn.golmundial.dto;

/**
 * Datos utilizados para editar una sede existente.
 */
public class ActualizarSedeRequest {

    private String nombre;
    private String ciudad;
    private String pais;
    private Integer capacidadAprox;

    public ActualizarSedeRequest() {
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

    public void setCapacidadAprox(
            Integer capacidadAprox
    ) {
        this.capacidadAprox = capacidadAprox;
    }
}
