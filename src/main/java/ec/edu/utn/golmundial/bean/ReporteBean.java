package ec.edu.utn.golmundial.bean;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import ec.edu.utn.golmundial.dto.EstadisticaSeleccionDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("reporteBean")
@ViewScoped
public class ReporteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String URL_RESUMEN =
            "http://localhost:8080/golmundial-estadisticas/api/datos/resumen";

    private static final String URL_ESTADISTICAS =
            "http://localhost:8080/golmundial-estadisticas/api/estadisticas/selecciones";

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Inject
    private LoginBean loginBean;

    private String torneo = "Copa Mundial de la FIFA 2026";
    private Long roles = 0L;
    private Long usuarios = 0L;
    private Long fases = 0L;
    private Long grupos = 0L;
    private Long sedes = 0L;
    private Long selecciones = 0L;
    private Long partidos = 0L;
    private String estado = "SIN_DATOS";

    private String grupoSeleccionado;
    private String fechaActualizacion = "Sin actualizar";

    private List<EstadisticaSeleccionDTO> estadisticas =
            new ArrayList<>();

    @PostConstruct
    public void init() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        cargarReportes();
    }

    public void cargarReportes() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        cargarResumen();
        cargarEstadisticas();
        fechaActualizacion = LocalDateTime.now().format(FORMATO_FECHA);
    }

    public void cargarResumen() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(URL_RESUMEN)
                     .request(MediaType.APPLICATION_JSON)
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                Map<String, Object> datos = respuesta.readEntity(
                        new GenericType<Map<String, Object>>() {
                        }
                );

                torneo = obtenerTexto(datos, "torneo");
                roles = obtenerNumero(datos, "roles");
                usuarios = obtenerNumero(datos, "usuarios");
                fases = obtenerNumero(datos, "fases");
                grupos = obtenerNumero(datos, "grupos");
                sedes = obtenerNumero(datos, "sedes");
                selecciones = obtenerNumero(datos, "selecciones");
                partidos = obtenerNumero(datos, "partidos");
                estado = obtenerTexto(datos, "estado");
                return;
            }

            mostrarErrorRespuesta(
                    respuesta,
                    "No se pudo cargar el resumen general."
            );

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible cargar el resumen de datos."
            );

            e.printStackTrace();
        }
    }

    public void cargarEstadisticas() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        try (Client cliente = ClientBuilder.newClient()) {

            var destino = cliente.target(URL_ESTADISTICAS);

            if (grupoSeleccionado != null
                    && !grupoSeleccionado.isBlank()) {

                destino = destino.queryParam(
                        "grupo",
                        grupoSeleccionado.trim()
                );
            }

            try (Response respuesta = destino
                    .request(MediaType.APPLICATION_JSON)
                    .get()) {

                if (respuesta.getStatus()
                        == Response.Status.OK.getStatusCode()) {

                    estadisticas = respuesta.readEntity(
                            new GenericType<List<EstadisticaSeleccionDTO>>() {
                            }
                    );

                    if (estadisticas == null) {
                        estadisticas = new ArrayList<>();
                    }

                    ordenarEstadisticasPorGrupo();

                    fechaActualizacion = LocalDateTime.now()
                            .format(FORMATO_FECHA);
                    return;
                }

                estadisticas = new ArrayList<>();

                mostrarErrorRespuesta(
                        respuesta,
                        "No se pudieron cargar las estadísticas."
                );
            }

        } catch (Exception e) {

            estadisticas = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible cargar las estadísticas."
            );

            e.printStackTrace();
        }
    }

    private void ordenarEstadisticasPorGrupo() {

        if (estadisticas == null || estadisticas.isEmpty()) {
            return;
        }

        estadisticas.sort(
                Comparator
                        .comparing(
                                (EstadisticaSeleccionDTO item) ->
                                        item.getGrupo() == null
                                                ? ""
                                                : item.getGrupo()
                        )
                        .thenComparing(
                                Comparator.comparingInt(
                                        EstadisticaSeleccionDTO::getPuntos
                                ).reversed()
                        )
                        .thenComparing(
                                Comparator.comparingInt(
                                        EstadisticaSeleccionDTO::getDiferenciaGoles
                                ).reversed()
                        )
                        .thenComparing(
                                Comparator.comparingInt(
                                        EstadisticaSeleccionDTO::getGolesFavor
                                ).reversed()
                        )
                        .thenComparing(
                                item -> item.getSeleccion() == null
                                        ? ""
                                        : item.getSeleccion()
                        )
        );
    }

    public void limpiarFiltro() {
        grupoSeleccionado = null;
        cargarEstadisticas();
    }

    public int getCantidadEstadisticas() {
        return estadisticas == null ? 0 : estadisticas.size();
    }

    public int getPartidosFinalizadosCalculados() {

        if (estadisticas == null || estadisticas.isEmpty()) {
            return 0;
        }

        int participaciones = estadisticas.stream()
                .filter(item -> item != null)
                .mapToInt(EstadisticaSeleccionDTO::getJugados)
                .sum();

        return participaciones / 2;
    }

    public int getTotalGoles() {

        if (estadisticas == null || estadisticas.isEmpty()) {
            return 0;
        }

        return estadisticas.stream()
                .filter(item -> item != null)
                .mapToInt(EstadisticaSeleccionDTO::getGolesFavor)
                .sum();
    }

    public BigDecimal getPromedioGolesPorPartido() {

        int partidosFinalizados = getPartidosFinalizadosCalculados();

        if (partidosFinalizados == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(getTotalGoles())
                .divide(
                        BigDecimal.valueOf(partidosFinalizados),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    public EstadisticaSeleccionDTO getLiderPuntos() {

        return estadisticasValidas().stream()
                .max(Comparator
                        .comparingInt(EstadisticaSeleccionDTO::getPuntos)
                        .thenComparingInt(
                                EstadisticaSeleccionDTO::getDiferenciaGoles
                        )
                        .thenComparingInt(
                                EstadisticaSeleccionDTO::getGolesFavor
                        ))
                .orElse(null);
    }

    public EstadisticaSeleccionDTO getMejorAtaque() {

        return estadisticasValidas().stream()
                .max(Comparator
                        .comparingInt(EstadisticaSeleccionDTO::getGolesFavor)
                        .thenComparingInt(
                                EstadisticaSeleccionDTO::getDiferenciaGoles
                        ))
                .orElse(null);
    }

    public EstadisticaSeleccionDTO getMejorDefensa() {

        return estadisticasValidas().stream()
                .filter(item -> item.getJugados() > 0)
                .min(Comparator
                        .comparingInt(EstadisticaSeleccionDTO::getGolesContra)
                        .thenComparing(
                                Comparator.comparingInt(
                                        EstadisticaSeleccionDTO::getPuntos
                                ).reversed()
                        ))
                .orElse(null);
    }

    public EstadisticaSeleccionDTO getMayorDiferencia() {

        return estadisticasValidas().stream()
                .max(Comparator
                        .comparingInt(
                                EstadisticaSeleccionDTO::getDiferenciaGoles
                        )
                        .thenComparingInt(
                                EstadisticaSeleccionDTO::getGolesFavor
                        ))
                .orElse(null);
    }

    public String getNombreLiderPuntos() {
        return nombreSeleccion(getLiderPuntos());
    }

    public String getNombreMejorAtaque() {
        return nombreSeleccion(getMejorAtaque());
    }

    public String getNombreMejorDefensa() {
        return nombreSeleccion(getMejorDefensa());
    }

    public String getNombreMayorDiferencia() {
        return nombreSeleccion(getMayorDiferencia());
    }

    public int getPuntosLider() {
        EstadisticaSeleccionDTO item = getLiderPuntos();
        return item == null ? 0 : item.getPuntos();
    }

    public int getGolesMejorAtaque() {
        EstadisticaSeleccionDTO item = getMejorAtaque();
        return item == null ? 0 : item.getGolesFavor();
    }

    public int getGolesMejorDefensa() {
        EstadisticaSeleccionDTO item = getMejorDefensa();
        return item == null ? 0 : item.getGolesContra();
    }

    public int getDiferenciaMayor() {
        EstadisticaSeleccionDTO item = getMayorDiferencia();
        return item == null ? 0 : item.getDiferenciaGoles();
    }

    public boolean isDatosCargados() {
        return "DATOS_CARGADOS".equalsIgnoreCase(estado);
    }

    public String getGrupoMostrado() {

        if (grupoSeleccionado == null
                || grupoSeleccionado.isBlank()) {
            return "Todos los grupos";
        }

        return "Grupo " + grupoSeleccionado.toUpperCase();
    }

    private List<EstadisticaSeleccionDTO> estadisticasValidas() {

        if (estadisticas == null || estadisticas.isEmpty()) {
            return List.of();
        }

        return estadisticas.stream()
                .filter(item -> item != null)
                .toList();
    }

    private String nombreSeleccion(
            EstadisticaSeleccionDTO item
    ) {

        if (item == null
                || item.getSeleccion() == null
                || item.getSeleccion().isBlank()) {
            return "Sin datos";
        }

        return item.getSeleccion();
    }

    private Long obtenerNumero(
            Map<String, Object> datos,
            String clave
    ) {

        if (datos == null) {
            return 0L;
        }

        Object valor = datos.get(clave);

        if (valor instanceof Number numero) {
            return numero.longValue();
        }

        if (valor != null) {

            try {
                return Long.parseLong(valor.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        return 0L;
    }

    private String obtenerTexto(
            Map<String, Object> datos,
            String clave
    ) {

        if (datos == null || datos.get(clave) == null) {
            return "";
        }

        return datos.get(clave).toString();
    }

    private void mostrarErrorRespuesta(
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

    private boolean sesionValida() {

        return loginBean != null
                && loginBean.isAdministrador()
                && loginBean.getAuthorizationHeader() != null;
    }

    private void redirigirAlLogin() {

        FacesContext contexto = FacesContext.getCurrentInstance();

        if (contexto == null
                || contexto.getResponseComplete()) {
            return;
        }

        String ruta = contexto.getExternalContext()
                .getRequestContextPath()
                + "/login.xhtml";

        try {

            contexto.getExternalContext().redirect(ruta);
            contexto.responseComplete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarMensaje(
            FacesMessage.Severity severidad,
            String titulo,
            String detalle
    ) {

        FacesContext contexto = FacesContext.getCurrentInstance();

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

    public String getTorneo() {
        return torneo;
    }

    public Long getRoles() {
        return roles;
    }

    public Long getUsuarios() {
        return usuarios;
    }

    public Long getFases() {
        return fases;
    }

    public Long getGrupos() {
        return grupos;
    }

    public Long getSedes() {
        return sedes;
    }

    public Long getSelecciones() {
        return selecciones;
    }

    public Long getPartidos() {
        return partidos;
    }

    public String getEstado() {
        return estado;
    }

    public String getGrupoSeleccionado() {
        return grupoSeleccionado;
    }

    public void setGrupoSeleccionado(
            String grupoSeleccionado
    ) {
        this.grupoSeleccionado = grupoSeleccionado;
    }

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public List<EstadisticaSeleccionDTO> getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(
            List<EstadisticaSeleccionDTO> estadisticas
    ) {
        this.estadisticas = estadisticas;
    }
}