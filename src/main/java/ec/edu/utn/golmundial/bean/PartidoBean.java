package ec.edu.utn.golmundial.bean;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import ec.edu.utn.golmundial.dto.PartidoDTO;
import ec.edu.utn.golmundial.dto.ResultadoPartidoRequest;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("partioBean")
@ViewScoped
public class PartidoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_URL =
            "http://localhost:8080/golmundial-estadisticas/api/partidos";

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private LoginBean loginBean;

    private List<PartidoDTO> partidos =
            new ArrayList<>();

    private PartidoDTO partidoSeleccionado =
            new PartidoDTO();

    @PostConstruct
    public void init() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        cargarPartidos();
    }

    public void cargarPartidos() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        try (Client cliente = ClientBuilder.newClient()) {

            this.partidos = cliente
                    .target(API_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PartidoDTO>>() {
                    });

            if (this.partidos == null) {
                this.partidos = new ArrayList<>();
            }

        } catch (Exception excepcion) {

            this.partidos = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    "No se pudieron cargar los partidos."
            );

            excepcion.printStackTrace();
        }
    }

    public void actualizarPartidos() {

        cargarPartidos();

        mostrarMensaje(
                FacesMessage.SEVERITY_INFO,
                "Actualizado",
                "La información de los partidos se actualizó correctamente."
        );
    }

    public void prepararResultado(
            PartidoDTO partido
    ) {

        this.partidoSeleccionado =
                copiarPartido(partido);

        if (this.partidoSeleccionado.getGolesLocal() == null) {
            this.partidoSeleccionado.setGolesLocal(0);
        }

        if (this.partidoSeleccionado.getGolesVisitante() == null) {
            this.partidoSeleccionado.setGolesVisitante(0);
        }
    }

    public void guardarResultado() {

        agregarResultadoAjax(false);

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        if (partidoSeleccionado == null
                || partidoSeleccionado.getId() == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Advertencia",
                    "No se ha seleccionado un partido."
            );

            return;
        }

        if (partidoSeleccionado.getGolesLocal() == null
                || partidoSeleccionado.getGolesVisitante() == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Marcador incompleto",
                    "Debe ingresar los goles de ambas selecciones."
            );

            return;
        }

        if (partidoSeleccionado.getGolesLocal() < 0
                || partidoSeleccionado.getGolesVisitante() < 0) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Marcador inválido",
                    "Los goles no pueden ser negativos."
            );

            return;
        }

        ResultadoPartidoRequest solicitud =
                new ResultadoPartidoRequest();

        solicitud.setGolesLocal(
                partidoSeleccionado.getGolesLocal()
        );

        solicitud.setGolesVisitante(
                partidoSeleccionado.getGolesVisitante()
        );

        String url = API_URL
                + "/"
                + partidoSeleccionado.getId()
                + "/resultado";

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(url)
                     .request(MediaType.APPLICATION_JSON)
                     .header(
                             HttpHeaders.AUTHORIZATION,
                             loginBean.getAuthorizationHeader()
                     )
                     .put(
                             Entity.entity(
                                     solicitud,
                                     MediaType.APPLICATION_JSON
                             )
                     )) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                cargarPartidos();

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Resultado registrado",
                        "El marcador oficial se guardó correctamente."
                );

                agregarResultadoAjax(true);
                return;
            }

            manejarRespuestaError(
                    respuesta,
                    "No se pudo registrar el resultado."
            );

        } catch (Exception excepcion) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible registrar el resultado del partido."
            );

            excepcion.printStackTrace();
        }
    }

    public int getTotalPartidos() {
        return partidos == null
                ? 0
                : partidos.size();
    }

    public long getPartidosProgramados() {
        return contarPorEstado("PROGRAMADO");
    }

    public long getPartidosEnJuego() {
        return contarPorEstado("EN_JUEGO");
    }

    public long getPartidosFinalizados() {
        return contarPorEstado("FINALIZADO");
    }

    public String textoEstado(
            String estado
    ) {

        if (estado == null || estado.isBlank()) {
            return "Sin estado";
        }

        return switch (estado.toUpperCase()) {
            case "PROGRAMADO" -> "Programado";
            case "EN_JUEGO" -> "En juego";
            case "FINALIZADO" -> "Finalizado";
            case "SUSPENDIDO" -> "Suspendido";
            case "CANCELADO" -> "Cancelado";
            default -> estado.replace('_', ' ');
        };
    }

    public String claseEstado(
            String estado
    ) {

        if (estado == null || estado.isBlank()) {
            return "neutro";
        }

        return switch (estado.toUpperCase()) {
            case "PROGRAMADO" -> "programado";
            case "EN_JUEGO" -> "en-juego";
            case "FINALIZADO" -> "finalizado";
            case "SUSPENDIDO" -> "suspendido";
            case "CANCELADO" -> "cancelado";
            default -> "neutro";
        };
    }

    public String textoFase(
            String fase
    ) {

        if (fase == null || fase.isBlank()) {
            return "Sin fase";
        }

        return switch (fase.toUpperCase()) {
            case "GRUPOS" -> "Fase de grupos";
            case "DIECISEISAVOS" -> "Dieciseisavos de final";
            case "OCTAVOS" -> "Octavos de final";
            case "CUARTOS" -> "Cuartos de final";
            case "SEMIFINAL" -> "Semifinal";
            case "TERCER_PUESTO" -> "Tercer puesto";
            case "FINAL" -> "Final";
            default -> fase.replace('_', ' ');
        };
    }

    public String formatearFecha(
            String fecha
    ) {

        if (fecha == null || fecha.isBlank()) {
            return "Fecha no disponible";
        }

        try {
            return OffsetDateTime
                    .parse(fecha)
                    .format(FORMATO_FECHA);

        } catch (DateTimeParseException ignorada) {
        }

        try {
            return LocalDateTime
                    .parse(fecha)
                    .format(FORMATO_FECHA);

        } catch (DateTimeParseException ignorada) {
        }

        return fecha
                .replace('T', ' ')
                .replace("Z", "");
    }

    public boolean resultadoDisponible(
            String estado
    ) {

        if (estado == null) {
            return true;
        }

        return !"FINALIZADO".equalsIgnoreCase(estado)
                && !"CANCELADO".equalsIgnoreCase(estado)
                && !"SUSPENDIDO".equalsIgnoreCase(estado);
    }

    private long contarPorEstado(
            String estado
    ) {

        if (partidos == null) {
            return 0;
        }

        return partidos
                .stream()
                .filter(partido -> partido != null
                        && partido.getEstado() != null
                        && estado.equalsIgnoreCase(
                                partido.getEstado()
                        ))
                .count();
    }

    private PartidoDTO copiarPartido(
            PartidoDTO origen
    ) {

        PartidoDTO copia = new PartidoDTO();

        if (origen == null) {
            return copia;
        }

        copia.setId(origen.getId());
        copia.setNumeroPartidoFifa(
                origen.getNumeroPartidoFifa()
        );
        copia.setFase(origen.getFase());
        copia.setGrupo(origen.getGrupo());

        copia.setSeleccionLocalId(
                origen.getSeleccionLocalId()
        );
        copia.setCodigoFifaLocal(
                origen.getCodigoFifaLocal()
        );
        copia.setSeleccionLocal(
                origen.getSeleccionLocal()
        );

        copia.setSeleccionVisitanteId(
                origen.getSeleccionVisitanteId()
        );
        copia.setCodigoFifaVisitante(
                origen.getCodigoFifaVisitante()
        );
        copia.setSeleccionVisitante(
                origen.getSeleccionVisitante()
        );

        copia.setFechaHoraUtc(
                origen.getFechaHoraUtc()
        );
        copia.setFechaHoraEt(
                origen.getFechaHoraEt()
        );

        copia.setSedeId(origen.getSedeId());
        copia.setSede(origen.getSede());
        copia.setCiudad(origen.getCiudad());
        copia.setPais(origen.getPais());

        copia.setEstado(origen.getEstado());
        copia.setGolesLocal(origen.getGolesLocal());
        copia.setGolesVisitante(
                origen.getGolesVisitante()
        );

        copia.setCuotaLocal(origen.getCuotaLocal());
        copia.setCuotaEmpate(origen.getCuotaEmpate());
        copia.setCuotaVisitante(
                origen.getCuotaVisitante()
        );
        copia.setResultadoNotificado(
                origen.isResultadoNotificado()
        );

        return copia;
    }

    private boolean sesionValida() {

        return loginBean != null
                && loginBean.isAdministrador()
                && loginBean.getAuthorizationHeader() != null;
    }

    private void manejarRespuestaError(
            Response respuesta,
            String mensajePredeterminado
    ) {

        agregarResultadoAjax(false);

        if (respuesta.getStatus()
                == Response.Status.UNAUTHORIZED.getStatusCode()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Sesión expirada",
                    "Debe iniciar sesión nuevamente."
            );

            redirigirAlLogin();
            return;
        }

        String detalle = mensajePredeterminado;

        try {

            if (respuesta.hasEntity()) {
                detalle = extraerMensajeError(
                        respuesta.readEntity(String.class),
                        mensajePredeterminado
                );
            }

        } catch (Exception ignorada) {
        }

        mostrarMensaje(
                FacesMessage.SEVERITY_ERROR,
                "No se pudo guardar",
                detalle
        );
    }

    private String extraerMensajeError(
            String contenido,
            String mensajePredeterminado
    ) {

        if (contenido == null || contenido.isBlank()) {
            return mensajePredeterminado;
        }

        String marca = "\"error\":\"";
        int inicio = contenido.indexOf(marca);

        if (inicio < 0) {
            return contenido;
        }

        inicio += marca.length();
        int fin = contenido.indexOf('"', inicio);

        if (fin < 0) {
            return contenido;
        }

        return contenido.substring(inicio, fin)
                .replace("\\\"", "\"");
    }

    private void agregarResultadoAjax(
            boolean operacionExitosa
    ) {

        PrimeFaces.current()
                .ajax()
                .addCallbackParam(
                        "operacionExitosa",
                        operacionExitosa
                );
    }

    private void redirigirAlLogin() {

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (contexto == null
                || contexto.getResponseComplete()) {
            return;
        }

        String ruta =
                contexto.getExternalContext()
                        .getRequestContextPath()
                        + "/login.xhtml";

        try {

            contexto.getExternalContext()
                    .redirect(ruta);

            contexto.responseComplete();

        } catch (IOException excepcion) {
            excepcion.printStackTrace();
        }
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

    public PartidoDTO getPartidoSeleccionado() {
        return partidoSeleccionado;
    }

    public void setPartidoSeleccionado(
            PartidoDTO partidoSeleccionado
    ) {
        this.partidoSeleccionado =
                partidoSeleccionado;
    }
}