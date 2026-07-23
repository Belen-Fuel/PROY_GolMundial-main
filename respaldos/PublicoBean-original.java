package ec.edu.utn.golmundial.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.edu.utn.golmundial.dto.GrupoDTO;
import ec.edu.utn.golmundial.dto.PartidoDTO;
import ec.edu.utn.golmundial.dto.PosicionDTO;
import ec.edu.utn.golmundial.dto.SeleccionDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Named("publicoBean")
@ViewScoped
public class PublicoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_BASE =
            "http://localhost:8080/golmundial-estadisticas/api";

    private List<PartidoDTO> partidos = new ArrayList<>();
    private List<GrupoDTO> grupos = new ArrayList<>();
    private List<PosicionDTO> posiciones = new ArrayList<>();
    private List<SeleccionDTO> selecciones = new ArrayList<>();

    private String grupoSeleccionado;

    @PostConstruct
    public void init() {
        cargarDatosPublicos();
    }

    public void cargarDatosPublicos() {
        cargarPartidos();
        cargarGrupos();
        cargarSelecciones();
        cargarPosiciones();
    }

    public void cargarPartidos() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_BASE + "/partidos")
                     .request(MediaType.APPLICATION_JSON)
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                partidos = respuesta.readEntity(
                        new GenericType<List<PartidoDTO>>() {
                        }
                );

                if (partidos == null) {
                    partidos = new ArrayList<>();
                }

                return;
            }

            partidos = new ArrayList<>();

            mostrarError(
                    respuesta,
                    "No se pudieron cargar los partidos."
            );

        } catch (Exception excepcion) {

            partidos = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible consultar los partidos."
            );

            excepcion.printStackTrace();
        }
    }

    public void cargarGrupos() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_BASE + "/grupos")
                     .request(MediaType.APPLICATION_JSON)
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                grupos = respuesta.readEntity(
                        new GenericType<List<GrupoDTO>>() {
                        }
                );

                if (grupos == null) {
                    grupos = new ArrayList<>();
                }

                return;
            }

            grupos = new ArrayList<>();

            mostrarError(
                    respuesta,
                    "No se pudieron cargar los grupos."
            );

        } catch (Exception excepcion) {

            grupos = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible consultar los grupos."
            );

            excepcion.printStackTrace();
        }
    }

    public void cargarSelecciones() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_BASE + "/selecciones")
                     .request(MediaType.APPLICATION_JSON)
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                selecciones = respuesta.readEntity(
                        new GenericType<List<SeleccionDTO>>() {
                        }
                );

                if (selecciones == null) {
                    selecciones = new ArrayList<>();
                }

                return;
            }

            selecciones = new ArrayList<>();

            mostrarError(
                    respuesta,
                    "No se pudieron cargar las selecciones."
            );

        } catch (Exception excepcion) {

            selecciones = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible consultar las selecciones."
            );

            excepcion.printStackTrace();
        }
    }

    public void cargarPosiciones() {

        try (Client cliente = ClientBuilder.newClient()) {

            WebTarget destino;

            if (grupoSeleccionado == null
                    || grupoSeleccionado.isBlank()) {

                destino = cliente.target(
                        API_BASE + "/posiciones"
                );

            } else {

                destino = cliente.target(
                        API_BASE
                                + "/grupos/"
                                + grupoSeleccionado
                                + "/posiciones"
                );
            }

            try (Response respuesta = destino
                    .request(MediaType.APPLICATION_JSON)
                    .get()) {

                if (respuesta.getStatus()
                        == Response.Status.OK.getStatusCode()) {

                    posiciones = respuesta.readEntity(
                            new GenericType<List<PosicionDTO>>() {
                            }
                    );

                    if (posiciones == null) {
                        posiciones = new ArrayList<>();
                    }

                    return;
                }

                posiciones = new ArrayList<>();

                mostrarError(
                        respuesta,
                        "No se pudieron cargar las posiciones."
                );
            }

        } catch (Exception excepcion) {

            posiciones = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible consultar las posiciones."
            );

            excepcion.printStackTrace();
        }
    }

    public void filtrarPosiciones() {
        cargarPosiciones();
    }

    public void limpiarFiltroGrupo() {
        grupoSeleccionado = null;
        cargarPosiciones();
    }

    public List<PartidoDTO> getProximosPartidos() {

        if (partidos == null || partidos.isEmpty()) {
            return new ArrayList<>();
        }

        return partidos.stream()
                .filter(partido ->
                        partido.getEstado() == null
                                || !"FINALIZADO".equalsIgnoreCase(
                                        partido.getEstado()
                                )
                )
                .limit(6)
                .toList();
    }

    private void mostrarError(
            Response respuesta,
            String mensajePredeterminado
    ) {

        String detalle = mensajePredeterminado;

        try {

            if (respuesta.hasEntity()) {
                detalle = respuesta.readEntity(String.class);
            }

        } catch (Exception ignored) {
        }

        mostrarMensaje(
                FacesMessage.SEVERITY_ERROR,
                "Error",
                detalle
        );
    }

    private void mostrarMensaje(
            FacesMessage.Severity severidad,
            String titulo,
            String detalle
    ) {

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (contexto != null) {

            contexto.addMessage(
                    null,
                    new FacesMessage(
                            severidad,
                            titulo,
                            detalle
                    )
            );
        }
    }

    public List<PartidoDTO> getPartidos() {
        return partidos;
    }

    public void setPartidos(
            List<PartidoDTO> partidos
    ) {
        this.partidos = partidos;
    }

    public List<GrupoDTO> getGrupos() {
        return grupos;
    }

    public void setGrupos(
            List<GrupoDTO> grupos
    ) {
        this.grupos = grupos;
    }

    public List<PosicionDTO> getPosiciones() {
        return posiciones;
    }

    public void setPosiciones(
            List<PosicionDTO> posiciones
    ) {
        this.posiciones = posiciones;
    }

    public List<SeleccionDTO> getSelecciones() {
        return selecciones;
    }

    public void setSelecciones(
            List<SeleccionDTO> selecciones
    ) {
        this.selecciones = selecciones;
    }

    public String getGrupoSeleccionado() {
        return grupoSeleccionado;
    }

    public void setGrupoSeleccionado(
            String grupoSeleccionado
    ) {
        this.grupoSeleccionado =
                grupoSeleccionado;
    }
    public List<PosicionDTO> posicionesPorGrupo(
        String codigoGrupo
    ) {

        if (codigoGrupo == null
                || codigoGrupo.isBlank()
                || posiciones == null) {

            return new ArrayList<>();
        }

        return posiciones.stream()
                .filter(posicion ->
                        posicion.getGrupo() != null
                                && posicion.getGrupo()
                                .equalsIgnoreCase(
                                        codigoGrupo
                                )
                )
                .sorted(
                        java.util.Comparator
                                .comparingInt(
                                        PosicionDTO::getPosicion
                                )
                )
                .toList();
    }
   public String obtenerCodigoBandera(String codigoFifa) {

    if (codigoFifa == null || codigoFifa.isBlank()) {
        return "un";
    }

    return switch (codigoFifa.trim().toUpperCase()) {
        case "CPV" -> "cv";
        case "CIV" -> "ci";
        case "JOR" -> "jo";
        case "COD" -> "cd";

        case "ALG" -> "dz";
        case "ARG" -> "ar";
        case "AUS" -> "au";
        case "AUT" -> "at";
        case "BEL" -> "be";
        case "BIH" -> "ba";
        case "BRA" -> "br";
        case "CAN" -> "ca";
        case "CHI" -> "cl";
        case "CHN" -> "cn";
        case "CMR" -> "cm";
        case "COL" -> "co";
        case "CRC" -> "cr";
        case "CRO" -> "hr";
        case "CTA", "CUW" -> "cw";
        case "CZE" -> "cz";
        case "DEN" -> "dk";
        case "ECU" -> "ec";
        case "EGY" -> "eg";
        case "ENG" -> "gb-eng";
        case "ESP" -> "es";
        case "FRA" -> "fr";
        case "GER" -> "de";
        case "GHA" -> "gh";
        case "HAI" -> "ht";
        case "HON" -> "hn";
        case "IRN" -> "ir";
        case "IRQ" -> "iq";
        case "ITA" -> "it";
        case "JPN" -> "jp";
        case "KOR" -> "kr";
        case "KSA" -> "sa";
        case "MAR" -> "ma";
        case "MEX" -> "mx";
        case "NED" -> "nl";
        case "NGA" -> "ng";
        case "NOR" -> "no";
        case "NZL" -> "nz";
        case "PAN" -> "pa";
        case "PAR" -> "py";
        case "PER" -> "pe";
        case "POL" -> "pl";
        case "POR" -> "pt";
        case "QAT" -> "qa";
        case "ROU" -> "ro";
        case "RSA" -> "za";
        case "SCO" -> "gb-sct";
        case "SEN" -> "sn";
        case "SRB" -> "rs";
        case "SUI" -> "ch";
        case "SWE" -> "se";
        case "TUN" -> "tn";
        case "TUR" -> "tr";
        case "URU" -> "uy";
        case "USA" -> "us";
        case "UZB" -> "uz";
        case "VEN" -> "ve";
        case "WAL" -> "gb-wls";

        default -> "un";
    };
}
    public String obtenerBanderaPorNombre(String seleccion) {

        if (seleccion == null) {
            return "un";
        }

        return switch (seleccion.toUpperCase()) {

            case "ECUADOR" -> "ec";
            case "ARGENTINA" -> "ar";
            case "BRASIL" -> "br";
            case "MÉXICO", "MEXICO" -> "mx";
            case "ESTADOS UNIDOS" -> "us";
            case "CANADÁ", "CANADA" -> "ca";
            case "ESPAÑA", "ESPANA" -> "es";
            case "FRANCIA" -> "fr";
            case "ALEMANIA" -> "de";
            case "ITALIA" -> "it";
            case "PORTUGAL" -> "pt";
            case "INGLATERRA" -> "gb-eng";
            case "GALES" -> "gb-wls";
            case "PAÍSES BAJOS", "PAISES BAJOS" -> "nl";
            case "JAPÓN", "JAPON" -> "jp";
            case "COREA DEL SUR" -> "kr";
            case "MARRUECOS" -> "ma";
            case "URUGUAY" -> "uy";
            case "COLOMBIA" -> "co";
            case "CHILE" -> "cl";
            case "PARAGUAY" -> "py";
            case "PERÚ", "PERU" -> "pe";

            default -> "un";
        };
    }
    public String formatearFechaHora(String fechaUtc) {

        if (fechaUtc == null || fechaUtc.isBlank()) {
            return "";
        }

        OffsetDateTime fecha = OffsetDateTime.parse(fechaUtc);

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy • HH:mm 'UTC'",
                        new Locale("es", "EC"));

        return fecha.format(formato);
    }
    
}

