package ec.edu.utn.golmundial.dto;

/**
 * Respuesta generada después de registrar
 * el resultado oficial de un partido.
 */
public class ResultadoPartidoDTO {

    private Long partidoId;
    private Integer numeroPartidoFifa;

    private Long seleccionLocalId;
    private String seleccionLocal;

    private Long seleccionVisitanteId;
    private String seleccionVisitante;

    private Integer golesLocal;
    private Integer golesVisitante;

    private String resultado1X2;
    private String estado;

    private boolean resultadoNotificado;
    private String fechaActualizacionUtc;

    private String mensaje;

    public ResultadoPartidoDTO() {
    }

    public ResultadoPartidoDTO(
            Long partidoId,
            Integer numeroPartidoFifa,
            Long seleccionLocalId,
            String seleccionLocal,
            Long seleccionVisitanteId,
            String seleccionVisitante,
            Integer golesLocal,
            Integer golesVisitante,
            String resultado1X2,
            String estado,
            boolean resultadoNotificado,
            String fechaActualizacionUtc,
            String mensaje
    ) {
        this.partidoId = partidoId;
        this.numeroPartidoFifa = numeroPartidoFifa;
        this.seleccionLocalId = seleccionLocalId;
        this.seleccionLocal = seleccionLocal;
        this.seleccionVisitanteId = seleccionVisitanteId;
        this.seleccionVisitante = seleccionVisitante;
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
        this.resultado1X2 = resultado1X2;
        this.estado = estado;
        this.resultadoNotificado = resultadoNotificado;
        this.fechaActualizacionUtc = fechaActualizacionUtc;
        this.mensaje = mensaje;
    }

    public Long getPartidoId() {
        return partidoId;
    }

    public Integer getNumeroPartidoFifa() {
        return numeroPartidoFifa;
    }

    public Long getSeleccionLocalId() {
        return seleccionLocalId;
    }

    public String getSeleccionLocal() {
        return seleccionLocal;
    }

    public Long getSeleccionVisitanteId() {
        return seleccionVisitanteId;
    }

    public String getSeleccionVisitante() {
        return seleccionVisitante;
    }

    public Integer getGolesLocal() {
        return golesLocal;
    }

    public Integer getGolesVisitante() {
        return golesVisitante;
    }

    public String getResultado1X2() {
        return resultado1X2;
    }

    public String getEstado() {
        return estado;
    }

    public boolean isResultadoNotificado() {
        return resultadoNotificado;
    }

    public String getFechaActualizacionUtc() {
        return fechaActualizacionUtc;
    }

    public String getMensaje() {
        return mensaje;
    }
}
