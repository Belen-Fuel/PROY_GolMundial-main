package ec.edu.utn.golmundial.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import ec.edu.utn.golmundial.dto.EstadisticaSeleccionDTO;
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
    private List<EstadisticaSeleccionDTO> estadisticas = new ArrayList<>();

    private String grupoSeleccionado;
    private String busquedaEstadisticas;

    @PostConstruct
    public void init() {
        cargarDatosPublicos();
    }

    public void cargarDatosPublicos() {
        cargarPartidos();
        cargarGrupos();
        cargarSelecciones();
        cargarPosiciones();
        cargarEstadisticas();
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

    public void cargarEstadisticas() {

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_BASE + "/estadisticas/selecciones")
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

                return;
            }

            estadisticas = new ArrayList<>();

            mostrarError(
                    respuesta,
                    "No se pudieron cargar las estadísticas."
            );

        } catch (Exception excepcion) {

            estadisticas = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible consultar las estadísticas."
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
                .filter(this::esPartidoProximo)
                .sorted(
                        Comparator.comparing(
                                this::obtenerFechaOrdenamiento
                        )
                )
                .limit(6)
                .toList();
    }

    public PartidoDTO getPartidoDestacado() {

        List<PartidoDTO> proximos = getProximosPartidos();

        if (proximos.isEmpty()) {
            return null;
        }

        return proximos.getFirst();
    }

    public List<SeleccionDTO> getSeleccionesDestacadas() {

        if (selecciones == null || selecciones.isEmpty()) {
            return new ArrayList<>();
        }

        return selecciones.stream()
                .sorted(
                        Comparator
                                .comparing(
                                        SeleccionDTO::isAnfitrion
                                )
                                .reversed()
                                .thenComparing(
                                        seleccion -> seleccion.getNombre() == null
                                                ? ""
                                                : seleccion.getNombre(),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )
                .limit(8)
                .toList();
    }

    public int getTotalPartidos() {
        return partidos == null ? 0 : partidos.size();
    }

    public int getTotalSelecciones() {
        return selecciones == null ? 0 : selecciones.size();
    }

    public int getTotalGrupos() {
        return grupos == null ? 0 : grupos.size();
    }

    public long getPartidosFinalizados() {

        if (partidos == null) {
            return 0;
        }

        return partidos.stream()
                .filter(partido -> estadoEs(
                        partido,
                        "FINALIZADO"
                ))
                .count();
    }

    public long getPartidosPendientes() {

        if (partidos == null) {
            return 0;
        }

        return partidos.stream()
                .filter(this::esPartidoProximo)
                .count();
    }

    private boolean esPartidoProximo(PartidoDTO partido) {

        if (partido == null) {
            return false;
        }

        String estado = partido.getEstado();

        return estado == null
                || (!"FINALIZADO".equalsIgnoreCase(estado)
                && !"CANCELADO".equalsIgnoreCase(estado)
                && !"SUSPENDIDO".equalsIgnoreCase(estado));
    }

    private boolean estadoEs(
            PartidoDTO partido,
            String estadoEsperado
    ) {

        return partido != null
                && partido.getEstado() != null
                && estadoEsperado.equalsIgnoreCase(
                        partido.getEstado()
                );
    }

    private OffsetDateTime obtenerFechaOrdenamiento(
            PartidoDTO partido
    ) {

        if (partido == null
                || partido.getFechaHoraUtc() == null
                || partido.getFechaHoraUtc().isBlank()) {

            return OffsetDateTime.MAX;
        }

        try {
            return OffsetDateTime.parse(
                    partido.getFechaHoraUtc()
            );
        } catch (DateTimeParseException excepcion) {
            return OffsetDateTime.MAX;
        }
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
                        Comparator.comparingInt(
                                PosicionDTO::getPosicion
                        )
                )
                .toList();
    }

    public String obtenerCodigoBandera(
            String codigoFifa
    ) {

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

    public String obtenerBanderaPorNombre(
            String seleccion
    ) {

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

    public String formatearFechaHora(
            String fechaUtc
    ) {

        if (fechaUtc == null || fechaUtc.isBlank()) {
            return "Fecha por confirmar";
        }

        try {
            OffsetDateTime fecha = OffsetDateTime.parse(
                    fechaUtc
            );

            DateTimeFormatter formato =
                    DateTimeFormatter.ofPattern(
                            "dd MMM yyyy · HH:mm 'UTC'",
                            new Locale("es", "EC")
                    );

            return fecha.format(formato);

        } catch (DateTimeParseException excepcion) {
            return fechaUtc;
        }
    }

    public String claseEstado(
            String estado
    ) {

        if (estado == null || estado.isBlank()) {
            return "estado-programado";
        }

        return switch (estado.trim().toUpperCase()) {
            case "EN_JUEGO", "EN JUEGO" -> "estado-en-juego";
            case "FINALIZADO" -> "estado-finalizado";
            case "SUSPENDIDO" -> "estado-suspendido";
            case "CANCELADO" -> "estado-cancelado";
            default -> "estado-programado";
        };
    }

    public void filtrarEstadisticas() {
        // El filtrado se realiza en memoria mediante los getters de la vista.
    }

    public void limpiarFiltrosEstadisticas() {
        grupoSeleccionado = null;
        busquedaEstadisticas = null;
    }

    public List<EstadisticaSeleccionDTO> estadisticasPorGrupo(
            String codigoGrupo
    ) {

        if (codigoGrupo == null
                || codigoGrupo.isBlank()
                || estadisticas == null
                || estadisticas.isEmpty()) {

            return new ArrayList<>();
        }

        String textoBusqueda = normalizarBusqueda(
                busquedaEstadisticas
        );

        return estadisticas.stream()
                .filter(item -> item != null)
                .filter(item -> item.getGrupo() != null
                        && item.getGrupo().equalsIgnoreCase(codigoGrupo))
                .filter(item -> coincideBusquedaEstadistica(
                        item,
                        textoBusqueda
                ))
                .sorted(comparadorEstadisticas())
                .toList();
    }

    public List<GrupoDTO> getGruposConEstadisticas() {

        if (grupos == null || grupos.isEmpty()) {
            return new ArrayList<>();
        }

        return grupos.stream()
                .filter(grupo -> grupo != null
                        && grupo.getCodigo() != null)
                .filter(grupo -> grupoSeleccionado == null
                        || grupoSeleccionado.isBlank()
                        || grupo.getCodigo().equalsIgnoreCase(
                                grupoSeleccionado
                        ))
                .filter(grupo -> !estadisticasPorGrupo(
                        grupo.getCodigo()
                ).isEmpty())
                .sorted(Comparator.comparing(
                        grupo -> grupo.getCodigo().toUpperCase()
                ))
                .toList();
    }

    public int getCantidadEstadisticasFiltradas() {

        return getGruposConEstadisticas().stream()
                .mapToInt(grupo -> estadisticasPorGrupo(
                        grupo.getCodigo()
                ).size())
                .sum();
    }

    public int getPartidosFinalizadosEstadisticas() {

        if (estadisticas == null || estadisticas.isEmpty()) {
            return 0;
        }

        int participaciones = estadisticas.stream()
                .filter(item -> item != null)
                .mapToInt(EstadisticaSeleccionDTO::getJugados)
                .sum();

        return participaciones / 2;
    }

    public int getTotalGolesEstadisticas() {

        if (estadisticas == null || estadisticas.isEmpty()) {
            return 0;
        }

        return estadisticas.stream()
                .filter(item -> item != null)
                .mapToInt(EstadisticaSeleccionDTO::getGolesFavor)
                .sum();
    }

    public BigDecimal getPromedioGolesEstadisticas() {

        int partidosFinalizados =
                getPartidosFinalizadosEstadisticas();

        if (partidosFinalizados == 0) {
            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return BigDecimal.valueOf(
                        getTotalGolesEstadisticas()
                )
                .divide(
                        BigDecimal.valueOf(partidosFinalizados),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    public EstadisticaSeleccionDTO getLiderEstadisticas() {

        return estadisticasValidas().stream()
                .max(Comparator
                        .comparingInt(
                                EstadisticaSeleccionDTO::getPuntos
                        )
                        .thenComparingInt(
                                EstadisticaSeleccionDTO::getDiferenciaGoles
                        )
                        .thenComparingInt(
                                EstadisticaSeleccionDTO::getGolesFavor
                        ))
                .orElse(null);
    }

    public EstadisticaSeleccionDTO getMejorAtaqueEstadisticas() {

        return estadisticasValidas().stream()
                .max(Comparator
                        .comparingInt(
                                EstadisticaSeleccionDTO::getGolesFavor
                        )
                        .thenComparingInt(
                                EstadisticaSeleccionDTO::getDiferenciaGoles
                        ))
                .orElse(null);
    }

    public EstadisticaSeleccionDTO getMejorDefensaEstadisticas() {

        return estadisticasValidas().stream()
                .filter(item -> item.getJugados() > 0)
                .min(Comparator
                        .comparingInt(
                                EstadisticaSeleccionDTO::getGolesContra
                        )
                        .thenComparing(
                                Comparator.comparingInt(
                                        EstadisticaSeleccionDTO::getPuntos
                                ).reversed()
                        ))
                .orElse(null);
    }

    public EstadisticaSeleccionDTO getMayorDiferenciaEstadisticas() {

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

    public String getNombreLiderEstadisticas() {
        return nombreEstadistica(getLiderEstadisticas());
    }

    public String getNombreMejorAtaqueEstadisticas() {
        return nombreEstadistica(getMejorAtaqueEstadisticas());
    }

    public String getNombreMejorDefensaEstadisticas() {
        return nombreEstadistica(getMejorDefensaEstadisticas());
    }

    public String getNombreMayorDiferenciaEstadisticas() {
        return nombreEstadistica(getMayorDiferenciaEstadisticas());
    }

    public String getCodigoLiderEstadisticas() {
        return codigoEstadistica(getLiderEstadisticas());
    }

    public String getCodigoMejorAtaqueEstadisticas() {
        return codigoEstadistica(getMejorAtaqueEstadisticas());
    }

    public String getCodigoMejorDefensaEstadisticas() {
        return codigoEstadistica(getMejorDefensaEstadisticas());
    }

    public String getCodigoMayorDiferenciaEstadisticas() {
        return codigoEstadistica(getMayorDiferenciaEstadisticas());
    }

    public int getPuntosLiderEstadisticas() {
        EstadisticaSeleccionDTO item = getLiderEstadisticas();
        return item == null ? 0 : item.getPuntos();
    }

    public int getGolesMejorAtaqueEstadisticas() {
        EstadisticaSeleccionDTO item =
                getMejorAtaqueEstadisticas();

        return item == null ? 0 : item.getGolesFavor();
    }

    public int getGolesMejorDefensaEstadisticas() {
        EstadisticaSeleccionDTO item =
                getMejorDefensaEstadisticas();

        return item == null ? 0 : item.getGolesContra();
    }

    public int getDiferenciaMayorEstadisticas() {
        EstadisticaSeleccionDTO item =
                getMayorDiferenciaEstadisticas();

        return item == null ? 0 : item.getDiferenciaGoles();
    }

    private List<EstadisticaSeleccionDTO> estadisticasValidas() {

        if (estadisticas == null || estadisticas.isEmpty()) {
            return List.of();
        }

        return estadisticas.stream()
                .filter(item -> item != null)
                .toList();
    }

    private Comparator<EstadisticaSeleccionDTO> comparadorEstadisticas() {

        return Comparator
                .comparingInt(
                        EstadisticaSeleccionDTO::getPuntos
                )
                .reversed()
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
                .thenComparing(item -> item.getSeleccion() == null
                        ? ""
                        : item.getSeleccion(),
                        String.CASE_INSENSITIVE_ORDER);
    }

    private boolean coincideBusquedaEstadistica(
            EstadisticaSeleccionDTO item,
            String textoBusqueda
    ) {

        if (textoBusqueda.isBlank()) {
            return true;
        }

        return contieneTexto(item.getSeleccion(), textoBusqueda)
                || contieneTexto(item.getCodigoFifa(), textoBusqueda)
                || contieneTexto(item.getGrupo(), textoBusqueda)
                || contieneTexto(item.getConfederacion(), textoBusqueda);
    }

    private boolean contieneTexto(
            String valor,
            String textoBusqueda
    ) {

        return valor != null
                && valor.toLowerCase(Locale.ROOT)
                .contains(textoBusqueda);
    }

    private String normalizarBusqueda(
            String texto
    ) {

        return texto == null
                ? ""
                : texto.trim().toLowerCase(Locale.ROOT);
    }

    private String nombreEstadistica(
            EstadisticaSeleccionDTO item
    ) {

        if (item == null
                || item.getSeleccion() == null
                || item.getSeleccion().isBlank()) {
            return "Sin datos";
        }

        return item.getSeleccion();
    }

    private String codigoEstadistica(
            EstadisticaSeleccionDTO item
    ) {

        if (item == null
                || item.getCodigoFifa() == null
                || item.getCodigoFifa().isBlank()) {
            return "UN";
        }

        return item.getCodigoFifa();
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
        this.grupoSeleccionado = grupoSeleccionado;
    }

    public List<EstadisticaSeleccionDTO> getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(
            List<EstadisticaSeleccionDTO> estadisticas
    ) {
        this.estadisticas = estadisticas;
    }

    public String getBusquedaEstadisticas() {
        return busquedaEstadisticas;
    }

    public void setBusquedaEstadisticas(
            String busquedaEstadisticas
    ) {
        this.busquedaEstadisticas = busquedaEstadisticas;
    }
}