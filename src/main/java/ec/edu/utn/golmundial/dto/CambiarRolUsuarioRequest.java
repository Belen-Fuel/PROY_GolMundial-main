package ec.edu.utn.golmundial.dto;

/**
 * Solicitud para modificar el rol de una cuenta.
 */
public class CambiarRolUsuarioRequest {

    private String rol;

    public CambiarRolUsuarioRequest() {
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
