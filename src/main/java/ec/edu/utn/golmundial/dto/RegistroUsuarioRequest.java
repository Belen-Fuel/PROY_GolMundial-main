package ec.edu.utn.golmundial.dto;

/**
 * Datos recibidos durante el registro público.
 */
public class RegistroUsuarioRequest {

    private String username;
    private String nombre;
    private String password;

    public RegistroUsuarioRequest() {
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
}
