package ec.edu.utn.golmundial.bean;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ec.edu.utn.golmundial.dto.CambiarEstadoUsuarioRequest;
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

        private List<UsuarioDTO> usuarios = new ArrayList<>();

        private CrearUsuarioRequest nuevoUsuario =
                new CrearUsuarioRequest();

        private UsuarioDTO usuarioSeleccionado;

        private String nuevoRol;

        @PostConstruct
        public void init() {

                if (loginBean == null || !loginBean.isAdministrador()) {
                redirigirAlLogin();
                return;
                }

                nuevoUsuario.setActivo(true);
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

        public void crearUsuario() {

                if (!sesionValida()) {
                        redirigirAlLogin();
                        return;
                }
                if (!validarNuevoUsuario()) {
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

                        nuevoUsuario = new CrearUsuarioRequest();
                        nuevoUsuario.setActivo(true);

                        cargarUsuarios();

                        mostrarMensaje(
                                FacesMessage.SEVERITY_INFO,
                                "Éxito",
                                "Usuario creado correctamente."
                        );

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

                this.usuarioSeleccionado = usuario;
                this.nuevoRol = usuario.getRol();
        }

        public void cambiarRol() {

                if (usuarioSeleccionado == null
                        || usuarioSeleccionado.getId() == null) {

                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Advertencia",
                                "No se ha seleccionado un usuario."
                        );

                        FacesContext.getCurrentInstance().validationFailed();
                        return;
                }

                String usuarioActual = loginBean.getUsername();

                if (usuarioActual != null
                        && usuarioSeleccionado.getUsername() != null
                        && usuarioActual.equalsIgnoreCase(
                                usuarioSeleccionado.getUsername()
                        )) {

                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Operación no permitida",
                                "No puede cambiar el rol de su propia cuenta."
                        );

                        FacesContext.getCurrentInstance().validationFailed();
                        return;
                }

                if (nuevoRol == null || nuevoRol.isBlank()) {

                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Advertencia",
                                "Seleccione un rol."
                        );

                        FacesContext.getCurrentInstance().validationFailed();
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
                                "Éxito",
                                "Rol actualizado correctamente."
                        );

                        return;
                        }

                        manejarRespuestaError(
                                respuesta,
                                "No se pudo cambiar el rol."
                        );

                        FacesContext.getCurrentInstance().validationFailed();

                } catch (Exception e) {

                        mostrarMensaje(
                                FacesMessage.SEVERITY_ERROR,
                                "Error de comunicación",
                                "No fue posible cambiar el rol."
                        );

                        FacesContext.getCurrentInstance().validationFailed();
                        e.printStackTrace();
                }
                }

        public void cambiarEstado(
                UsuarioDTO usuario
        ) {

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
                                "Éxito",
                                solicitud.getActivo()
                                        ? "Usuario activado correctamente."
                                        : "Usuario desactivado correctamente."
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
                        "Error",
                        detalle
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

       private boolean validarNuevoUsuario() {

                FacesContext context = FacesContext.getCurrentInstance();

                if (nuevoUsuario == null) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_ERROR,
                                "Datos inválidos",
                                "No se recibieron los datos del usuario."
                        );

                        context.validationFailed();
                        return false;
                }

                String username = nuevoUsuario.getUsername();
                String nombre = nuevoUsuario.getNombre();
                String password = nuevoUsuario.getPassword();
                String rol = nuevoUsuario.getRol();

                if (username == null || username.isBlank()) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Usuario obligatorio",
                                "Ingrese un nombre de usuario."
                        );

                        context.validationFailed();
                        return false;
                }

                final String usernameNormalizado = username.trim();

                if (usernameNormalizado.length() < 4) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Usuario inválido",
                                "El nombre de usuario debe tener al menos 4 caracteres."
                        );

                        context.validationFailed();
                        return false;
                }

                if (!usernameNormalizado.matches("^[a-zA-Z0-9._-]+$")) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Usuario inválido",
                                "El usuario solo puede contener letras, números, punto, guion y guion bajo."
                        );

                        context.validationFailed();
                        return false;
                }

                if (nombre == null || nombre.isBlank()) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Nombre obligatorio",
                                "Ingrese el nombre completo."
                        );

                        context.validationFailed();
                        return false;
                }

                final String nombreNormalizado = nombre.trim();

                if (nombreNormalizado.length() < 3) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Nombre inválido",
                                "El nombre debe tener al menos 3 caracteres."
                        );

                        context.validationFailed();
                        return false;
                }

                if (password == null || password.isBlank()) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Contraseña obligatoria",
                                "Ingrese una contraseña."
                        );

                        context.validationFailed();
                        return false;
                }

                if (password.length() < 8) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Contraseña inválida",
                                "La contraseña debe tener al menos 8 caracteres."
                        );

                        context.validationFailed();
                        return false;
                }

                if (rol == null || rol.isBlank()) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Rol obligatorio",
                                "Seleccione un rol."
                        );

                        context.validationFailed();
                        return false;
                }

                if (!rol.equals("USUARIO")
                        && !rol.equals("INVITADO")) {

                        mostrarMensaje(
                                FacesMessage.SEVERITY_WARN,
                                "Rol inválido",
                                "El rol seleccionado no es válido."
                        );

                        context.validationFailed();
                        return false;
                }

                boolean usuarioRepetido = usuarios.stream()
                        .anyMatch(u ->
                                u.getUsername() != null
                                        && u.getUsername()
                                        .equalsIgnoreCase(usernameNormalizado)
                        );

                if (usuarioRepetido) {
                        mostrarMensaje(
                                FacesMessage.SEVERITY_ERROR,
                                "Usuario duplicado",
                                "Ya existe una cuenta con ese nombre de usuario."
                        );

                        context.validationFailed();
                        return false;
                }

                nuevoUsuario.setUsername(usernameNormalizado);
                nuevoUsuario.setNombre(nombreNormalizado);

                return true;
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
}