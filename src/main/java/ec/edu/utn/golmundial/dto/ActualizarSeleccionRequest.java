package ec.edu.utn.golmundial.dto;

/**
 * Datos utilizados para editar una selección.
 */
public class ActualizarSeleccionRequest {

    private String codigoFifa;
    private String nombre;
    private String grupo;
    private String confederacion;
    private Boolean esAnfitrion;
    private String clasificacion;

    public ActualizarSeleccionRequest() {
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

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    public String getConfederacion() {
        return confederacion;
    }

    public void setConfederacion(String confederacion) {
        this.confederacion = confederacion;
    }

    public Boolean getEsAnfitrion() {
        return esAnfitrion;
    }

    public void setEsAnfitrion(Boolean esAnfitrion) {
        this.esAnfitrion = esAnfitrion;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }
}
