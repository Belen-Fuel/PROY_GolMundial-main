package ec.edu.utn.golmundial.dto;

/**
 * Respuesta generada después de un inicio
 * de sesión correcto.
 */
public class LoginResponse {

    private String token;
    private String tipoToken;
    private String fechaExpiracionUtc;
    private UsuarioSesionDTO usuario;
    private String mensaje;

    public LoginResponse() {
    }

    public LoginResponse(
            String token,
            String tipoToken,
            String fechaExpiracionUtc,
            UsuarioSesionDTO usuario,
            String mensaje
    ) {
        this.token = token;
        this.tipoToken = tipoToken;
        this.fechaExpiracionUtc =
                fechaExpiracionUtc;
        this.usuario = usuario;
        this.mensaje = mensaje;
    }

    public String getToken() {
        return token;
    }

    public String getTipoToken() {
        return tipoToken;
    }

    public String getFechaExpiracionUtc() {
        return fechaExpiracionUtc;
    }

    public UsuarioSesionDTO getUsuario() {
        return usuario;
    }

    public String getMensaje() {
        return mensaje;
    }
}
