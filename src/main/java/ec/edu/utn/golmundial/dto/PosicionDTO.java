package ec.edu.utn.golmundial.dto;

/**
 * Representa la posición de una selección dentro
 * de la tabla de su grupo.
 */
public class PosicionDTO {

    private int posicion;

    private Long seleccionId;
    private String codigoFifa;
    private String seleccion;
    private String grupo;

    private int jugados;
    private int ganados;
    private int empatados;
    private int perdidos;

    private int golesFavor;
    private int golesContra;
    private int diferenciaGoles;

    private int puntos;

    public PosicionDTO() {
    }

    public PosicionDTO(
            int posicion,
            Long seleccionId,
            String codigoFifa,
            String seleccion,
            String grupo,
            int jugados,
            int ganados,
            int empatados,
            int perdidos,
            int golesFavor,
            int golesContra,
            int diferenciaGoles,
            int puntos
    ) {
        this.posicion = posicion;
        this.seleccionId = seleccionId;
        this.codigoFifa = codigoFifa;
        this.seleccion = seleccion;
        this.grupo = grupo;
        this.jugados = jugados;
        this.ganados = ganados;
        this.empatados = empatados;
        this.perdidos = perdidos;
        this.golesFavor = golesFavor;
        this.golesContra = golesContra;
        this.diferenciaGoles = diferenciaGoles;
        this.puntos = puntos;
    }

    public int getPosicion() {
        return posicion;
    }

    public Long getSeleccionId() {
        return seleccionId;
    }

    public String getCodigoFifa() {
        return codigoFifa;
    }

    public String getSeleccion() {
        return seleccion;
    }

    public String getGrupo() {
        return grupo;
    }

    public int getJugados() {
        return jugados;
    }

    public int getGanados() {
        return ganados;
    }

    public int getEmpatados() {
        return empatados;
    }

    public int getPerdidos() {
        return perdidos;
    }

    public int getGolesFavor() {
        return golesFavor;
    }

    public int getGolesContra() {
        return golesContra;
    }

    public int getDiferenciaGoles() {
        return diferenciaGoles;
    }

    public int getPuntos() {
        return puntos;
    }
}
