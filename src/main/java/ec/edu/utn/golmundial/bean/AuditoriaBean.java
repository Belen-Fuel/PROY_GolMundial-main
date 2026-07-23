package ec.edu.utn.golmundial.bean;

import java.io.IOException;
import java.io.Serializable;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import ec.edu.utn.golmundial.dto.AuditoriaDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Named("auditoriaBean")
@ViewScoped
public class AuditoriaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_URL =
            "http://localhost:8080/golmundial-estadisticas/api/auditoria";

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final DateTimeFormatter FORMATO_ACTUALIZACION =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private LoginBean loginBean;

    private List<AuditoriaDTO> auditorias = new ArrayList<>();
    private List<AuditoriaDTO> auditoriasFiltradas;
    private OffsetDateTime ultimaActualizacion;

    @PostConstruct
    public void init() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        cargarAuditorias();
    }

    public void cargarAuditorias() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        try (Client cliente = ClientBuilder.newClient();
             Response respuesta = cliente
                     .target(API_URL)
                     .request(MediaType.APPLICATION_JSON)
                     .header(
                             HttpHeaders.AUTHORIZATION,
                             loginBean.getAuthorizationHeader()
                     )
                     .get()) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                auditorias = respuesta.readEntity(
                        new GenericType<List<AuditoriaDTO>>() {
                        }
                );

                if (auditorias == null) {
                    auditorias = new ArrayList<>();
                }

                auditoriasFiltradas = null;
                ultimaActualizacion = OffsetDateTime.now();
                return;
            }

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

            String detalle =
                    "No se pudieron cargar los registros de auditoría.";

            if (respuesta.hasEntity()) {
                detalle = respuesta.readEntity(String.class);
            }

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error",
                    detalle
            );

        } catch (Exception e) {

            auditorias = new ArrayList<>();
            auditoriasFiltradas = null;

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible conectarse con la API de auditoría."
            );

            e.printStackTrace();
        }
    }

    public boolean filtroGlobal(
            Object valor,
            Object filtro,
            Locale locale
    ) {

        String textoFiltro = normalizar(
                filtro == null ? "" : filtro.toString()
        );

        if (textoFiltro.isBlank()) {
            return true;
        }

        if (!(valor instanceof AuditoriaDTO auditoria)) {
            return false;
        }

        String contenido = String.join(
                " ",
                textoSeguro(auditoria.getId()),
                textoSeguro(auditoria.getAccion()),
                accionVisible(auditoria.getAccion()),
                textoSeguro(auditoria.getEntidad()),
                entidadVisible(auditoria.getEntidad()),
                textoSeguro(auditoria.getEntidadId()),
                textoSeguro(auditoria.getUsuarioReferencia()),
                textoSeguro(auditoria.getDetalle()),
                textoSeguro(auditoria.getFechaHoraUtc()),
                fechaVisible(auditoria.getFechaHoraUtc())
        );

        return normalizar(contenido).contains(textoFiltro);
    }

    public int getTotalRegistros() {
        return auditorias == null ? 0 : auditorias.size();
    }

    public int getCantidadMostrada() {

        if (auditoriasFiltradas != null) {
            return auditoriasFiltradas.size();
        }

        return getTotalRegistros();
    }

    public long getRegistrosHoy() {

        if (auditorias == null || auditorias.isEmpty()) {
            return 0;
        }

        return auditorias.stream()
                .filter(a -> esDeHoy(a.getFechaHoraUtc()))
                .count();
    }

    public int getUsuariosDistintos() {

        Set<String> usuarios = new HashSet<>();

        if (auditorias != null) {
            auditorias.stream()
                    .map(AuditoriaDTO::getUsuarioReferencia)
                    .filter(valor -> valor != null && !valor.isBlank())
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .forEach(usuarios::add);
        }

        return usuarios.size();
    }

    public int getEntidadesDistintas() {

        Set<String> entidades = new HashSet<>();

        if (auditorias != null) {
            auditorias.stream()
                    .map(AuditoriaDTO::getEntidad)
                    .filter(valor -> valor != null && !valor.isBlank())
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .forEach(entidades::add);
        }

        return entidades.size();
    }

    public String getFechaActualizacion() {

        if (ultimaActualizacion == null) {
            return "Sin actualizar";
        }

        return ultimaActualizacion
                .atZoneSameInstant(ZoneId.systemDefault())
                .format(FORMATO_ACTUALIZACION);
    }

    public String accionVisible(String accion) {

        if (accion == null || accion.isBlank()) {
            return "Acción no especificada";
        }

        return switch (accion.trim().toUpperCase()) {
            case "CREAR_USUARIO" -> "Crear usuario";
            case "CAMBIAR_ROL_USUARIO" -> "Cambiar rol";
            case "ACTIVAR_USUARIO" -> "Activar usuario";
            case "DESACTIVAR_USUARIO" -> "Desactivar usuario";
            case "RESTABLECER_PASSWORD" -> "Restablecer contraseña";
            case "REGISTRO_USUARIO" -> "Registro de usuario";
            case "CREAR_SELECCION" -> "Crear selección";
            case "ACTUALIZAR_SELECCION" -> "Actualizar selección";
            case "ELIMINAR_SELECCION" -> "Eliminar selección";
            case "CREAR_SEDE" -> "Crear sede";
            case "ACTUALIZAR_SEDE" -> "Actualizar sede";
            case "ELIMINAR_SEDE" -> "Eliminar sede";
            case "REGISTRAR_RESULTADO", "RESULTADO_OFICIAL_REGISTRADO" ->
                    "Registrar resultado";
            case "INICIAR_SESION", "LOGIN" -> "Inicio de sesión";
            case "CERRAR_SESION", "LOGOUT" -> "Cierre de sesión";
            default -> convertirCodigo(accion);
        };
    }

    public String accionIcono(String accion) {

        String valor = accion == null ? "" : accion.toUpperCase();

        if (valor.contains("ELIMINAR") || valor.contains("DESACTIVAR")) {
            return "pi pi-trash";
        }

        if (valor.contains("CREAR") || valor.contains("REGISTRO")) {
            return "pi pi-plus-circle";
        }

        if (valor.contains("ACTUALIZAR") || valor.contains("CAMBIAR")
                || valor.contains("RESTABLECER")) {
            return "pi pi-pencil";
        }

        if (valor.contains("RESULTADO")) {
            return "pi pi-check-circle";
        }

        if (valor.contains("LOGIN") || valor.contains("INICIAR")) {
            return "pi pi-sign-in";
        }

        if (valor.contains("LOGOUT") || valor.contains("CERRAR")) {
            return "pi pi-sign-out";
        }

        if (valor.contains("ACTIVAR")) {
            return "pi pi-check";
        }

        return "pi pi-history";
    }

    public String accionClase(String accion) {

        String valor = accion == null ? "" : accion.toUpperCase();

        if (valor.contains("ELIMINAR") || valor.contains("DESACTIVAR")) {
            return "accion-roja";
        }

        if (valor.contains("CREAR") || valor.contains("REGISTRO")) {
            return "accion-verde";
        }

        if (valor.contains("ACTUALIZAR") || valor.contains("CAMBIAR")
                || valor.contains("RESTABLECER")) {
            return "accion-azul";
        }

        if (valor.contains("RESULTADO")) {
            return "accion-morada";
        }

        if (valor.contains("LOGIN") || valor.contains("INICIAR")
                || valor.contains("LOGOUT") || valor.contains("CERRAR")) {
            return "accion-gris";
        }

        if (valor.contains("ACTIVAR")) {
            return "accion-verde";
        }

        return "accion-neutra";
    }

    public String entidadVisible(String entidad) {

        if (entidad == null || entidad.isBlank()) {
            return "Sin entidad";
        }

        return switch (entidad.trim().toUpperCase()) {
            case "USUARIO" -> "Usuario";
            case "SELECCION" -> "Selección";
            case "SEDE" -> "Sede";
            case "PARTIDO" -> "Partido";
            default -> convertirCodigo(entidad);
        };
    }

    public String usuarioVisible(String usuario) {
        return usuario == null || usuario.isBlank()
                ? "Sistema"
                : usuario;
    }

    public String detalleVisible(String detalle) {
        return detalle == null || detalle.isBlank()
                ? "Sin detalle adicional"
                : detalle;
    }

    public String fechaVisible(String fechaHoraUtc) {

        if (fechaHoraUtc == null || fechaHoraUtc.isBlank()) {
            return "Sin fecha";
        }

        try {
            return OffsetDateTime
                    .parse(fechaHoraUtc)
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .format(FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            return fechaHoraUtc;
        }
    }

    private boolean esDeHoy(String fechaHoraUtc) {

        if (fechaHoraUtc == null || fechaHoraUtc.isBlank()) {
            return false;
        }

        try {
            return OffsetDateTime
                    .parse(fechaHoraUtc)
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDate()
                    .equals(
                            OffsetDateTime.now()
                                    .atZoneSameInstant(ZoneId.systemDefault())
                                    .toLocalDate()
                    );
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private String convertirCodigo(String codigo) {

        String texto = codigo
                .trim()
                .toLowerCase()
                .replace('_', ' ');

        if (texto.isBlank()) {
            return "Sin información";
        }

        return Character.toUpperCase(texto.charAt(0))
                + texto.substring(1);
    }

    private String textoSeguro(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private String normalizar(String valor) {

        if (valor == null) {
            return "";
        }

        String sinAcentos = Normalizer.normalize(
                valor,
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return sinAcentos.toLowerCase().trim();
    }

    private boolean sesionValida() {
        return loginBean != null
                && loginBean.isAdministrador()
                && loginBean.getAuthorizationHeader() != null;
    }

    private void redirigirAlLogin() {

        FacesContext contexto = FacesContext.getCurrentInstance();

        if (contexto == null || contexto.getResponseComplete()) {
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
                    new FacesMessage(severidad, titulo, detalle)
            );
        }
    }

    public List<AuditoriaDTO> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<AuditoriaDTO> auditorias) {
        this.auditorias = auditorias;
    }

    public List<AuditoriaDTO> getAuditoriasFiltradas() {
        return auditoriasFiltradas;
    }

    public void setAuditoriasFiltradas(
            List<AuditoriaDTO> auditoriasFiltradas
    ) {
        this.auditoriasFiltradas = auditoriasFiltradas;
    }
}