package ec.edu.utn.golmundial.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.PrimeFaces;

import ec.edu.utn.golmundial.dto.CambiarEstadoUsuarioRequest;
import ec.edu.utn.golmundial.dto.CambiarPasswordUsuarioRequest;
import ec.edu.utn.golmundial.dto.CambiarRolUsuarioRequest;
import ec.edu.utn.golmundial.dto.CrearUsuarioRequest;
import ec.edu.utn.golmundial.dto.UsuarioDTO;
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

@Named("usuarioBean")
@ViewScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String API_URL =
            "http://localhost:8080/golmundial-estadisticas/api/usuarios";

    @Inject
    private LoginBean loginBean;

    private List<UsuarioDTO> usuarios =
            new ArrayList<>();

    private CrearUsuarioRequest nuevoUsuario =
            new CrearUsuarioRequest();

    private UsuarioDTO usuarioSeleccionado;

    private String nuevoRol;

    private CambiarPasswordUsuarioRequest cambioPassword =
            new CambiarPasswordUsuarioRequest();

    @PostConstruct
    public void init() {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        prepararNuevoUsuario();
        prepararCambioPassword(null);
        cargarUsuarios();
    }

    public void cargarUsuarios() {

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

                usuarios = respuesta.readEntity(
                        new GenericType<List<UsuarioDTO>>() {
                        }
                );

                if (usuarios == null) {
                    usuarios = new ArrayList<>();
                }

                return;
            }

            manejarRespuestaError(
                    respuesta,
                    "No se pudieron cargar los usuarios."
            );

        } catch (Exception e) {

            usuarios = new ArrayList<>();

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible conectarse con la API de usuarios."
            );

            e.printStackTrace();
        }
    }

    public void prepararNuevoUsuario() {

        nuevoUsuario = new CrearUsuarioRequest();
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setRol("USUARIO");
    }

    public void crearUsuario() {

        agregarResultadoAjax(false);

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
                     .post(
                             Entity.entity(
                                     nuevoUsuario,
                                     MediaType.APPLICATION_JSON
                             )
                     )) {

            if (respuesta.getStatus()
                    == Response.Status.CREATED.getStatusCode()) {

                cargarUsuarios();
                prepararNuevoUsuario();

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Usuario creado",
                        "La cuenta fue registrada correctamente."
                );

                agregarResultadoAjax(true);
                return;
            }

            manejarRespuestaError(
                    respuesta,
                    "No se pudo crear el usuario."
            );

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible registrar el usuario."
            );

            e.printStackTrace();
        }
    }

    public void prepararCambioRol(
            UsuarioDTO usuario
    ) {

        usuarioSeleccionado = usuario;
        nuevoRol = usuario == null
                ? null
                : usuario.getRol();
    }

    public void cambiarRol() {

        agregarResultadoAjax(false);

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        if (usuarioSeleccionado == null
                || usuarioSeleccionado.getId() == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Advertencia",
                    "No se ha seleccionado un usuario."
            );

            return;
        }

        if (nuevoRol == null || nuevoRol.isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Advertencia",
                    "Seleccione un rol."
            );

            return;
        }

        CambiarRolUsuarioRequest solicitud =
                new CambiarRolUsuarioRequest();

        solicitud.setRol(nuevoRol);

        String url =
                API_URL
                        + "/"
                        + usuarioSeleccionado.getId()
                        + "/rol";

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

                cargarUsuarios();

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Rol actualizado",
                        "El nuevo rol fue guardado correctamente."
                );

                agregarResultadoAjax(true);
                return;
            }

            manejarRespuestaError(
                    respuesta,
                    "No se pudo cambiar el rol."
            );

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible cambiar el rol."
            );

            e.printStackTrace();
        }
    }

    public void cambiarEstado(
            UsuarioDTO usuario
    ) {

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        if (usuario == null || usuario.getId() == null) {
            return;
        }

        CambiarEstadoUsuarioRequest solicitud =
                new CambiarEstadoUsuarioRequest();

        solicitud.setActivo(!usuario.isActivo());

        String url =
                API_URL
                        + "/"
                        + usuario.getId()
                        + "/estado";

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

                cargarUsuarios();

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        solicitud.getActivo()
                                ? "Usuario activado"
                                : "Usuario desactivado",
                        solicitud.getActivo()
                                ? "La cuenta ya puede iniciar sesión."
                                : "La cuenta quedó bloqueada y sus sesiones fueron revocadas."
                );

                return;
            }

            manejarRespuestaError(
                    respuesta,
                    "No se pudo cambiar el estado del usuario."
            );

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible cambiar el estado."
            );

            e.printStackTrace();
        }
    }

    public void prepararCambioPassword(
            UsuarioDTO usuario
    ) {

        usuarioSeleccionado = usuario;
        cambioPassword = new CambiarPasswordUsuarioRequest();
        cambioPassword.setObligarCambio(true);
    }

    public void cambiarPassword() {

        agregarResultadoAjax(false);

        if (!sesionValida()) {
            redirigirAlLogin();
            return;
        }

        if (usuarioSeleccionado == null
                || usuarioSeleccionado.getId() == null) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Advertencia",
                    "No se ha seleccionado un usuario."
            );

            return;
        }

        if (cambioPassword == null
                || cambioPassword.getNuevaPassword() == null
                || cambioPassword.getNuevaPassword().isBlank()) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_WARN,
                    "Contraseña requerida",
                    "Ingrese una nueva contraseña."
            );

            return;
        }

        String url =
                API_URL
                        + "/"
                        + usuarioSeleccionado.getId()
                        + "/password";

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
                                     cambioPassword,
                                     MediaType.APPLICATION_JSON
                             )
                     )) {

            if (respuesta.getStatus()
                    == Response.Status.OK.getStatusCode()) {

                cargarUsuarios();
                cambioPassword = new CambiarPasswordUsuarioRequest();
                cambioPassword.setObligarCambio(true);

                mostrarMensaje(
                        FacesMessage.SEVERITY_INFO,
                        "Contraseña restablecida",
                        "La contraseña se actualizó y las sesiones anteriores fueron cerradas."
                );

                agregarResultadoAjax(true);
                return;
            }

            manejarRespuestaError(
                    respuesta,
                    "No se pudo restablecer la contraseña."
            );

        } catch (Exception e) {

            mostrarMensaje(
                    FacesMessage.SEVERITY_ERROR,
                    "Error de comunicación",
                    "No fue posible restablecer la contraseña."
            );

            e.printStackTrace();
        }
    }

    public int getCantidadUsuarios() {
        return usuarios == null ? 0 : usuarios.size();
    }

    public long getCantidadActivos() {

        if (usuarios == null) {
            return 0L;
        }

        return usuarios.stream()
                .filter(usuario -> usuario != null
                        && usuario.isActivo())
                .count();
    }

    public long getCantidadInactivos() {
        return getCantidadUsuarios() - getCantidadActivos();
    }

    public long getCantidadCambioPendiente() {

        if (usuarios == null) {
            return 0L;
        }

        return usuarios.stream()
                .filter(usuario -> usuario != null
                        && usuario.isCambioPasswordObligatorio())
                .count();
    }

    public long getCantidadUsuariosRegistrados() {

        if (usuarios == null) {
            return 0L;
        }

        return usuarios.stream()
                .filter(usuario -> usuario != null
                        && "USUARIO".equalsIgnoreCase(
                        usuario.getRol()
                ))
                .count();
    }

    public long getCantidadInvitados() {

        if (usuarios == null) {
            return 0L;
        }

        return usuarios.stream()
                .filter(usuario -> usuario != null
                        && "INVITADO".equalsIgnoreCase(
                        usuario.getRol()
                ))
                .count();
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
                detalle = respuesta.readEntity(String.class);
            }

        } catch (Exception ignored) {
        }

        mostrarMensaje(
                FacesMessage.SEVERITY_ERROR,
                "No se pudo completar la operación",
                detalle
        );
    }

    private void agregarResultadoAjax(
            boolean operacionExitosa
    ) {

        FacesContext contexto =
                FacesContext.getCurrentInstance();

        if (contexto != null
                && contexto.isPostback()) {

            PrimeFaces.current()
                    .ajax()
                    .addCallbackParam(
                            "operacionExitosa",
                            operacionExitosa
                    );
        }
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

        } catch (IOException e) {
            e.printStackTrace();
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

    public List<UsuarioDTO> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(
            List<UsuarioDTO> usuarios
    ) {
        this.usuarios = usuarios;
    }

    public CrearUsuarioRequest getNuevoUsuario() {
        return nuevoUsuario;
    }

    public void setNuevoUsuario(
            CrearUsuarioRequest nuevoUsuario
    ) {
        this.nuevoUsuario = nuevoUsuario;
    }

    public UsuarioDTO getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }

    public void setUsuarioSeleccionado(
            UsuarioDTO usuarioSeleccionado
    ) {
        this.usuarioSeleccionado = usuarioSeleccionado;
    }

    public String getNuevoRol() {
        return nuevoRol;
    }

    public void setNuevoRol(
            String nuevoRol
    ) {
        this.nuevoRol = nuevoRol;
    }

    public CambiarPasswordUsuarioRequest getCambioPassword() {
        return cambioPassword;
    }

    public void setCambioPassword(
            CambiarPasswordUsuarioRequest cambioPassword
    ) {
        this.cambioPassword = cambioPassword;
    }
}