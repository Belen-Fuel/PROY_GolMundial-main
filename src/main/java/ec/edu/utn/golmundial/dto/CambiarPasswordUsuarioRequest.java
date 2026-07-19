package ec.edu.utn.golmundial.dto;

/**
 * Solicitud administrativa para restablecer
 * la contraseña de un usuario.
 */
public class CambiarPasswordUsuarioRequest {

    private String nuevaPassword;
    private Boolean obligarCambio;

    public CambiarPasswordUsuarioRequest() {
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(
            String nuevaPassword
    ) {
        this.nuevaPassword = nuevaPassword;
    }

    public Boolean getObligarCambio() {
        return obligarCambio;
    }

    public void setObligarCambio(
            Boolean obligarCambio
    ) {
        this.obligarCambio = obligarCambio;
    }
}
