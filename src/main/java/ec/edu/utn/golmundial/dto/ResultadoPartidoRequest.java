package ec.edu.utn.golmundial.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Datos necesarios para registrar el marcador
 * oficial de un partido.
 */
public class ResultadoPartidoRequest {

    @NotNull(message = "Los goles del equipo local son obligatorios")
    @Min(
            value = 0,
            message = "Los goles del equipo local no pueden ser negativos"
    )
    private Integer golesLocal;

    @NotNull(message = "Los goles del equipo visitante son obligatorios")
    @Min(
            value = 0,
            message = "Los goles del equipo visitante no pueden ser negativos"
    )
    private Integer golesVisitante;

    public ResultadoPartidoRequest() {
    }

    public Integer getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(Integer golesLocal) {
        this.golesLocal = golesLocal;
    }

    public Integer getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(Integer golesVisitante) {
        this.golesVisitante = golesVisitante;
    }
}
