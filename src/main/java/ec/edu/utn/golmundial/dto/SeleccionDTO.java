package ec.edu.utn.golmundial.dto;

/**
 * Información pública de una selección.
 */
public class SeleccionDTO {

    private Long id;
    private String codigoFifa;
    private String nombre;
    private String grupo;
    private String confederacion;
    private boolean anfitrion;
    private String clasificacion;

    public SeleccionDTO() {
    }

    public SeleccionDTO(
            Long id,
            String codigoFifa,
            String nombre,
            String grupo,
            String confederacion,
            boolean anfitrion,
            String clasificacion
    ) {
        this.id = id;
        this.codigoFifa = codigoFifa;
        this.nombre = nombre;
        this.grupo = grupo;
        this.confederacion = confederacion;
        this.anfitrion = anfitrion;
        this.clasificacion = clasificacion;
    }

    public Long getId() {
        return id;
    }

    public String getCodigoFifa() {
        return codigoFifa;
    }

    public String getNombre() {
        return nombre;
    }

    public String getGrupo() {
        return grupo;
    }

    public String getConfederacion() {
        return confederacion;
    }

    public boolean isAnfitrion() {
        return anfitrion;
    }

    public String getClasificacion() {
        return clasificacion;
    }
}
