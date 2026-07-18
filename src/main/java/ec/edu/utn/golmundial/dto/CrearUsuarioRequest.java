package ec.edu.utn.golmundial.dto;

/**
 * Datos utilizados por el administrador
 * para crear una cuenta.
 */
public class CrearUsuarioRequest {

    private String username;
    private String nombre;
    private String password;
    private String rol;
    private Boolean activo;

    public CrearUsuarioRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
