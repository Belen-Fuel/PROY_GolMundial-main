package ec.edu.utn.golmundial.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.edu.utn.golmundial.dto.PartidoDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("partidoBean") // Mantiene la coincidencia exacta con tu archivo xhtml
@ViewScoped
public class PartidoBean implements Serializable {
    @Inject
    private LoginBean loginBean;

    private List<PartidoDTO> partidos = new ArrayList<>();
    private PartidoDTO partidoSeleccionado = new PartidoDTO(); // Partido que se edita en el modal

    // URL de la API del Backend de Estadísticas
    private static final String API_URL = "http://localhost:8080/golmundial-estadisticas/api/partidos";

    @PostConstruct
    public void init() {
        cargarPartidos();
    }

    // Método para traer todos los partidos desde el backend mediante API REST (JSON)
    public void cargarPartidos() {
        Client cliente = null;
        try {
            cliente = ClientBuilder.newClient();
            this.partidos = cliente.target(API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PartidoDTO>>() {});
        } catch (Exception e) {
            System.err.println("Error al obtener los partidos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cliente != null) {
                cliente.close();
            }
        }
    }

    private boolean validarResultado() {

        FacesContext context = FacesContext.getCurrentInstance();

        if (partidoSeleccionado == null
                || partidoSeleccionado.getId() == null) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Partido inválido",
                            "Debe seleccionar un partido."
                    ));

            context.validationFailed();
            return false;
        }

        Integer golesLocal =
                partidoSeleccionado.getGolesLocal();

        Integer golesVisitante =
                partidoSeleccionado.getGolesVisitante();

        if (golesLocal == null || golesVisitante == null) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Marcador incompleto",
                            "Debe ingresar los goles de ambos equipos."
                    ));

            context.validationFailed();
            return false;
        }

        if (golesLocal < 0 || golesVisitante < 0) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Marcador inválido",
                            "Los goles no pueden ser negativos."
                    ));

            context.validationFailed();
            return false;
        }

        String codigoLocal =
                partidoSeleccionado.getCodigoFifaLocal();

        String codigoVisitante =
                partidoSeleccionado.getCodigoFifaVisitante();

        if (codigoLocal != null
                && codigoVisitante != null
                && codigoLocal.equalsIgnoreCase(codigoVisitante)) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Selecciones inválidas",
                            "Una selección no puede jugar contra sí misma."
                    ));

            context.validationFailed();
            return false;
        }

        boolean faseEliminatoria =
                partidoSeleccionado.getGrupo() == null
                || partidoSeleccionado.getGrupo().isBlank();

        if (faseEliminatoria
                && golesLocal.equals(golesVisitante)) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_ERROR,
                            "Empate no permitido",
                            "En una fase eliminatoria debe existir un ganador."
                    ));

            context.validationFailed();
            return false;
        }

        return true;
    }

    // Método corregido para registrar el marcador oficial siguiendo el contrato de datos del Backend
    public void guardarResultado() {

        FacesContext context = FacesContext.getCurrentInstance();

        if (!validarResultado()) {
            return;
        }

        Client cliente = null;
        Response respuesta = null;

        try {

            cliente = ClientBuilder.newClient();

            ec.edu.utn.golmundial.dto.ResultadoPartidoRequest solicitudGoles =
                    new ec.edu.utn.golmundial.dto.ResultadoPartidoRequest();

            solicitudGoles.setGolesLocal(
                    partidoSeleccionado.getGolesLocal()
            );

            solicitudGoles.setGolesVisitante(
                    partidoSeleccionado.getGolesVisitante()
            );

            String urlConId =
                    API_URL
                    + "/"
                    + partidoSeleccionado.getId()
                    + "/resultado";

            respuesta = cliente
                    .target(urlConId)
                    .request(MediaType.APPLICATION_JSON)
                    .header(
                            jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION,
                            loginBean.getAuthorizationHeader()
                    )
                    .put(
                            Entity.entity(
                                    solicitudGoles,
                                    MediaType.APPLICATION_JSON
                            )
                    );

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                cargarPartidos();

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_INFO,
                                "Resultado registrado",
                                "El marcador se guardó correctamente."
                        ));

            } else {

                String detalle = "El backend rechazó el resultado.";

                if (respuesta.hasEntity()) {
                    detalle = respuesta.readEntity(String.class);
                }

                context.addMessage(null,
                        new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                "No se pudo guardar",
                                detalle
                        ));

                /*
                * Evita que el diálogo se cierre cuando
                * el backend rechaza el resultado.
                */
                context.validationFailed();
            }

        } catch (Exception e) {

            context.addMessage(null,
                    new FacesMessage(
                            FacesMessage.SEVERITY_FATAL,
                            "Error de comunicación",
                            "No fue posible comunicarse con el backend."
                    ));

            context.validationFailed();
            e.printStackTrace();

        } finally {

            if (respuesta != null) {
                respuesta.close();
            }

            if (cliente != null) {
                cliente.close();
            }
        }
    }
    public void verificarAcceso() {

        if (!loginBean.isLogueado()
                || !loginBean.isAdministrador()) {

            try {
                FacesContext facesContext =
                        FacesContext.getCurrentInstance();

                String contexto =
                        facesContext
                                .getExternalContext()
                                .getRequestContextPath();

                facesContext
                        .getExternalContext()
                        .redirect(contexto + "/login.xhtml");

                facesContext.responseComplete();

            } catch (IOException e) {
                throw new RuntimeException(
                        "No se pudo redirigir al login",
                        e
                );
            }
        }
    }

    public String obtenerCodigoBandera(String codigoFifa) {

    if (codigoFifa == null || codigoFifa.isBlank()) {
        return "desconocido";
    }

    return switch (codigoFifa.trim().toUpperCase()) {

        case "USA" -> "us";
        case "CAN" -> "ca";
        case "MEX" -> "mx";

        case "ARG" -> "ar";
        case "BRA" -> "br";
        case "ECU" -> "ec";
        case "COL" -> "co";
        case "URU" -> "uy";
        case "PAR" -> "py";
        case "CHI" -> "cl";
        case "PER" -> "pe";
        case "BOL" -> "bo";
        case "VEN" -> "ve";

        case "ESP" -> "es";
        case "FRA" -> "fr";
        case "GER" -> "de";
        case "ENG" -> "gb-eng";
        case "POR" -> "pt";
        case "ITA" -> "it";
        case "NED" -> "nl";
        case "BEL" -> "be";
        case "CRO" -> "hr";
        case "SUI" -> "ch";
        case "DEN" -> "dk";
        case "NOR" -> "no";
        case "SWE" -> "se";
        case "POL" -> "pl";
        case "AUT" -> "at";
        case "SRB" -> "rs";
        case "UKR" -> "ua";
        case "SCO" -> "gb-sct";
        case "WAL" -> "gb-wls";

        case "JPN" -> "jp";
        case "KOR" -> "kr";
        case "IRN" -> "ir";
        case "KSA" -> "sa";
        case "AUS" -> "au";
        case "QAT" -> "qa";

        case "MAR" -> "ma";
        case "SEN" -> "sn";
        case "EGY" -> "eg";
        case "ALG" -> "dz";
        case "TUN" -> "tn";
        case "GHA" -> "gh";
        case "NGA" -> "ng";
        case "CMR" -> "cm";
        case "CIV" -> "ci";
        case "RSA" -> "za";

        case "NZL" -> "nz";

        default -> codigoFifa.trim().toLowerCase();
    };
}
    

    // --- GETTERS Y SETTERS ---

    public List<PartidoDTO> getPartidos() {
        return partidos;
    }

    public void setPartidos(List<PartidoDTO> partidos) {
        this.partidos = partidos;
    }

    public PartidoDTO getPartidoSeleccionado() {
        return partidoSeleccionado;
    }

    public void setPartidoSeleccionado(PartidoDTO partidoSeleccionado) {
        this.partidoSeleccionado = partidoSeleccionado;
    }
}