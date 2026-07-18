package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.CambiarEstadoUsuarioRequest;
import ec.edu.utn.golmundial.dto.CambiarPasswordUsuarioRequest;
import ec.edu.utn.golmundial.dto.CambiarRolUsuarioRequest;
import ec.edu.utn.golmundial.dto.CrearUsuarioRequest;
import ec.edu.utn.golmundial.dto.UsuarioDTO;
import ec.edu.utn.golmundial.dto.UsuarioSesionDTO;
import ec.edu.utn.golmundial.exception.CuentaInactivaException;
import ec.edu.utn.golmundial.exception.ReglaNegocioException;
import ec.edu.utn.golmundial.exception.RolNoAutorizadoException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import ec.edu.utn.golmundial.exception.TokenInvalidoException;
import ec.edu.utn.golmundial.exception.UsuarioDuplicadoException;
import ec.edu.utn.golmundial.exception.UsuarioNoEncontradoException;
import ec.edu.utn.golmundial.service.SeguridadService;
import ec.edu.utn.golmundial.service.UsuarioService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Operaciones administrativas sobre usuarios.
 */
@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioResource {

    @EJB
    private SeguridadService seguridadService;

    @EJB
    private UsuarioService usuarioService;

    @GET
    public Response listar(
            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            seguridadService
                    .validarAdministrador(
                            autorizacion
                    );

            return Response
                    .ok(
                            usuarioService
                                    .listarUsuarios()
                    )
                    .build();

        } catch (RuntimeException excepcion) {
            return manejarExcepcion(excepcion);
        }
    }

    @GET
    @Path("/{id}")
    public Response buscar(
            @PathParam("id") Long id,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            seguridadService
                    .validarAdministrador(
                            autorizacion
                    );

            return Response
                    .ok(
                            usuarioService
                                    .buscarUsuario(id)
                    )
                    .build();

        } catch (RuntimeException excepcion) {
            return manejarExcepcion(excepcion);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(
            CrearUsuarioRequest solicitud,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            UsuarioDTO usuario =
                    usuarioService.crearUsuario(
                            solicitud,
                            administrador.getUsername()
                    );

            return Response
                    .status(Response.Status.CREATED)
                    .entity(usuario)
                    .build();

        } catch (RuntimeException excepcion) {
            return manejarExcepcion(excepcion);
        }
    }

    @PUT
    @Path("/{id}/estado")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cambiarEstado(
            @PathParam("id") Long id,
            CambiarEstadoUsuarioRequest solicitud,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            UsuarioDTO usuario =
                    usuarioService.cambiarEstado(
                            id,
                            solicitud,
                            administrador.getId(),
                            administrador.getUsername()
                    );

            return Response.ok(usuario).build();

        } catch (RuntimeException excepcion) {
            return manejarExcepcion(excepcion);
        }
    }

    @PUT
    @Path("/{id}/rol")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cambiarRol(
            @PathParam("id") Long id,
            CambiarRolUsuarioRequest solicitud,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            UsuarioDTO usuario =
                    usuarioService.cambiarRol(
                            id,
                            solicitud,
                            administrador.getId(),
                            administrador.getUsername()
                    );

            return Response.ok(usuario).build();

        } catch (RuntimeException excepcion) {
            return manejarExcepcion(excepcion);
        }
    }

    @PUT
    @Path("/{id}/password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cambiarPassword(
            @PathParam("id") Long id,
            CambiarPasswordUsuarioRequest solicitud,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            UsuarioDTO usuario =
                    usuarioService.cambiarPassword(
                            id,
                            solicitud,
                            administrador.getUsername()
                    );

            return Response.ok(usuario).build();

        } catch (RuntimeException excepcion) {
            return manejarExcepcion(excepcion);
        }
    }

    private Response manejarExcepcion(
            RuntimeException excepcion
    ) {

        if (excepcion
                instanceof TokenInvalidoException) {

            return error(
                    Response.Status.UNAUTHORIZED,
                    excepcion.getMessage()
            );
        }

        if (excepcion
                instanceof CuentaInactivaException
                || excepcion
                instanceof RolNoAutorizadoException) {

            return error(
                    Response.Status.FORBIDDEN,
                    excepcion.getMessage()
            );
        }

        if (excepcion
                instanceof UsuarioNoEncontradoException) {

            return error(
                    Response.Status.NOT_FOUND,
                    excepcion.getMessage()
            );
        }

        if (excepcion
                instanceof UsuarioDuplicadoException
                || excepcion
                instanceof ReglaNegocioException) {

            return error(
                    Response.Status.CONFLICT,
                    excepcion.getMessage()
            );
        }

        if (excepcion
                instanceof SolicitudInvalidaException) {

            return error(
                    Response.Status.BAD_REQUEST,
                    excepcion.getMessage()
            );
        }

        throw excepcion;
    }

    private Response error(
            Response.Status estado,
            String mensaje
    ) {

        return Response
                .status(estado)
                .entity(
                        Map.of(
                                "estadoHttp",
                                estado.getStatusCode(),
                                "error",
                                mensaje
                        )
                )
                .build();
    }
}
