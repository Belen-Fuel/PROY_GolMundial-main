package ec.edu.utn.golmundial.dto;

public class EventoIntegracionDTO {

    private Long id;
    private String tipoEvento;
    private String entidad;
    private Long entidadId;
    private String payloadJson;
    private String estado;
    private int intentos;
    private String ultimoError;
    private String fechaCreacionUtc;
    private String fechaActualizacionUtc;

    public EventoIntegracionDTO() {
    }

    public EventoIntegracionDTO(
            Long id,
            String tipoEvento,
            String entidad,
            Long entidadId,
            String payloadJson,
            String estado,
            int intentos,
            String ultimoError,
            String fechaCreacionUtc,
            String fechaActualizacionUtc
    ) {
        this.id = id;
        this.tipoEvento = tipoEvento;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.payloadJson = payloadJson;
        this.estado = estado;
        this.intentos = intentos;
        this.ultimoError = ultimoError;
        this.fechaCreacionUtc = fechaCreacionUtc;
        this.fechaActualizacionUtc = fechaActualizacionUtc;
    }

    public Long getId() {
        return id;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getEntidad() {
        return entidad;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public String getEstado() {
        return estado;
    }

    public int getIntentos() {
        return intentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public String getFechaCreacionUtc() {
        return fechaCreacionUtc;
    }

    public String getFechaActualizacionUtc() {
        return fechaActualizacionUtc;
    }
}
