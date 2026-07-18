package ec.edu.utn.golmundial.dto;

/**
 * Información pública de una cuenta.
 *
 * Nunca expone el hash, la sal ni otros datos
 * sensibles de la contraseña.
 */
public class UsuarioDTO {

    private Long id;
    private String username;
    private String nombre;
    private String rol;
    private boolean activo;
    private boolean cambioPasswordObligatorio;
    private String fechaCreacionUtc;
    private String fechaActualizacionUtc;

    public UsuarioDTO() {
    }

    public UsuarioDTO(
            Long id,
            String username,
            String nombre,
            String rol,
            boolean activo,
            boolean cambioPasswordObligatorio,
            String fechaCreacionUtc,
            String fechaActualizacionUtc
    ) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.rol = rol;
        this.activo = activo;
        this.cambioPasswordObligatorio =
                cambioPasswordObligatorio;
        this.fechaCreacionUtc = fechaCreacionUtc;
        this.fechaActualizacionUtc =
                fechaActualizacionUtc;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public boolean isCambioPasswordObligatorio() {
        return cambioPasswordObligatorio;
    }

    public String getFechaCreacionUtc() {
        return fechaCreacionUtc;
    }

    public String getFechaActualizacionUtc() {
        return fechaActualizacionUtc;
    }
}
