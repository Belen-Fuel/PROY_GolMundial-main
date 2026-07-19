package ec.edu.utn.golmundial.dto;

/**
 * Solicitud para activar o desactivar una cuenta.
 */
public class CambiarEstadoUsuarioRequest {

    private Boolean activo;

    public CambiarEstadoUsuarioRequest() {
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
