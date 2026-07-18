package ec.edu.utn.golmundial.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Información pública de un partido (Corregido para JSF e Integración Backend).
 */
public class PartidoDTO implements Serializable {

    private Long id;
    private Integer numeroPartidoFifa;

    private String fase;
    private String grupo;

    private Long seleccionLocalId;
    private String seleccionLocal;

    private Long seleccionVisitanteId;
    private String seleccionVisitante;

    private String fechaHoraUtc;
    private String fechaHoraEt;

    private Long sedeId;
    private String sede;
    private String ciudad;
    private String pais;

    private String estado;

    private Integer golesLocal;
    private Integer golesVisitante;

    private BigDecimal cuotaLocal;
    private BigDecimal cuotaEmpate;
    private BigDecimal cuotaVisitante;

    private boolean resultadoNotificado;

    // Constructor vacío (Requerido por JSF y frameworks de deserialización)
    public PartidoDTO() {
    }

    // Constructor completo original (Requerido por ConsultaTorneoService)
    public PartidoDTO(
            Long id,
            Integer numeroPartidoFifa,
            String fase,
            String grupo,
            Long seleccionLocalId,
            String seleccionLocal,
            Long seleccionVisitanteId,
            String seleccionVisitante,
            String fechaHoraUtc,
            String fechaHoraEt,
            Long sedeId,
            String sede,
            String ciudad,
            String pais,
            String estado,
            Integer golesLocal,
            Integer golesVisitante,
            BigDecimal cuotaLocal,
            BigDecimal cuotaEmpate,
            BigDecimal cuotaVisitante,
            boolean resultadoNotificado
    ) {
        this.id = id;
        this.numeroPartidoFifa = numeroPartidoFifa;
        this.fase = fase;
        this.grupo = grupo;
        this.seleccionLocalId = seleccionLocalId;
        this.seleccionLocal = seleccionLocal;
        this.seleccionVisitanteId = seleccionVisitanteId;
        this.seleccionVisitante = seleccionVisitante;
        this.fechaHoraUtc = fechaHoraUtc;
        this.fechaHoraEt = fechaHoraEt;
        this.sedeId = sedeId;
        this.sede = sede;
        this.ciudad = ciudad;
        this.pais = pais;
        this.estado = estado;
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
        this.cuotaLocal = cuotaLocal;
        this.cuotaEmpate = cuotaEmpate;
        this.cuotaVisitante = cuotaVisitante;
        this.resultadoNotificado = resultadoNotificado;
    }

    // --- GETTERS Y SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumeroPartidoFifa() { return numeroPartidoFifa; }
    public void setNumeroPartidoFifa(Integer numeroPartidoFifa) { this.numeroPartidoFifa = numeroPartidoFifa; }

    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public Long getSeleccionLocalId() { return seleccionLocalId; }
    public void setSeleccionLocalId(Long seleccionLocalId) { this.seleccionLocalId = seleccionLocalId; }

    public String getSeleccionLocal() { return seleccionLocal; }
    public void setSeleccionLocal(String seleccionLocal) { this.seleccionLocal = seleccionLocal; }

    public Long getSeleccionVisitanteId() { return seleccionVisitanteId; }
    public void setSeleccionVisitanteId(Long seleccionVisitanteId) { this.seleccionVisitanteId = seleccionVisitanteId; }

    public String getSeleccionVisitante() { return seleccionVisitante; }
    public void setSeleccionVisitante(String seleccionVisitante) { this.seleccionVisitante = seleccionVisitante; }

    public String getFechaHoraUtc() { return fechaHoraUtc; }
    public void setFechaHoraUtc(String fechaHoraUtc) { this.fechaHoraUtc = fechaHoraUtc; }

    public String getFechaHoraEt() { return fechaHoraEt; }
    public void setFechaHoraEt(String fechaHoraEt) { this.fechaHoraEt = fechaHoraEt; }

    public Long getSedeId() { return sedeId; }
    public void setSedeId(Long sedeId) { this.sedeId = sedeId; }

    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getGolesLocal() { return golesLocal; }
    public void setGolesLocal(Integer golesLocal) { this.golesLocal = golesLocal; }

    public Integer getGolesVisitante() { return golesVisitante; }
    public void setGolesVisitante(Integer golesVisitante) { this.golesVisitante = golesVisitante; }

    public BigDecimal getCuotaLocal() { return cuotaLocal; }
    public void setCuotaLocal(BigDecimal cuotaLocal) { this.cuotaLocal = cuotaLocal; }

    public BigDecimal getCuotaEmpate() { return cuotaEmpate; }
    public void setCuotaEmpate(BigDecimal cuotaEmpate) { this.cuotaEmpate = cuotaEmpate; }

    public BigDecimal getCuotaVisitante() { return cuotaVisitante; }
    public void setCuotaVisitante(BigDecimal cuotaVisitante) { this.cuotaVisitante = cuotaVisitante; }

    public boolean isResultadoNotificado() { return resultadoNotificado; }
    public void setResultadoNotificado(boolean resultadoNotificado) { this.resultadoNotificado = resultadoNotificado; }
}
