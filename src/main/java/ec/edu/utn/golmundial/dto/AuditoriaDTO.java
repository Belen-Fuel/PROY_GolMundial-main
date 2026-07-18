package ec.edu.utn.golmundial.dto;

public class AuditoriaDTO {

    private Long id;
    private String accion;
    private String entidad;
    private Long entidadId;
    private String usuarioReferencia;
    private String detalle;
    private String fechaHoraUtc;

    public AuditoriaDTO() {
    }

    public AuditoriaDTO(
            Long id,
            String accion,
            String entidad,
            Long entidadId,
            String usuarioReferencia,
            String detalle,
            String fechaHoraUtc
    ) {
        this.id = id;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.usuarioReferencia = usuarioReferencia;
        this.detalle = detalle;
        this.fechaHoraUtc = fechaHoraUtc;
    }

    public Long getId() {
        return id;
    }

    public String getAccion() {
        return accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getUsuarioReferencia() {
        return usuarioReferencia;
    }

    public String getDetalle() {
        return detalle;
    }

    public String getFechaHoraUtc() {
        return fechaHoraUtc;
    }
}
