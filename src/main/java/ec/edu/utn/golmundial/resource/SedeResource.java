package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.ActualizarSedeRequest;
import ec.edu.utn.golmundial.dto.CrearSedeRequest;
import ec.edu.utn.golmundial.dto.SedeDTO;
import ec.edu.utn.golmundial.dto.UsuarioSesionDTO;
import ec.edu.utn.golmundial.exception.CuentaInactivaException;
import ec.edu.utn.golmundial.exception.ReglaNegocioException;
import ec.edu.utn.golmundial.exception.RolNoAutorizadoException;
import ec.edu.utn.golmundial.exception.SedeDuplicadaException;
import ec.edu.utn.golmundial.exception.SedeNoEncontradaException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import ec.edu.utn.golmundial.exception.TokenInvalidoException;
import ec.edu.utn.golmundial.service.AdministracionSedeService;
import ec.edu.utn.golmundial.service.ConsultaTorneoService;
import ec.edu.utn.golmundial.service.SeguridadService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
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
 * Consultas públicas y operaciones administrativas
 * sobre las sedes del torneo.
 */
@Path("/sedes")
@Produces(MediaType.APPLICATION_JSON)
public class SedeResource {

    @EJB
    private ConsultaTorneoService consultaService;

    @EJB
    private AdministracionSedeService
            administracionSedeService;

    @EJB
    private SeguridadService seguridadService;

    /**
     * Consulta pública de todas las sedes.
     */
    @GET
    public Response listar() {

        return Response
                .ok(
                        consultaService.listarSedes()
                )
                .build();
    }

    /**
     * Consulta pública de una sede.
     */
    @GET
    @Path("/{id}")
    public Response buscar(
            @PathParam("id") Long id
    ) {

        SedeDTO sede =
                consultaService.buscarSede(id);

        if (sede == null) {

            return error(
                    Response.Status.NOT_FOUND,
                    "Sede no encontrada"
            );
        }

        return Response.ok(sede).build();
    }

    /**
     * Crea una sede.
     *
     * Operación exclusiva del administrador.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(
            CrearSedeRequest solicitud,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            SedeDTO sede =
                    administracionSedeService
                            .crear(
                                    solicitud,
                                    administrador.getUsername()
                            );

            return Response
                    .status(Response.Status.CREATED)
                    .entity(sede)
                    .build();

        } catch (RuntimeException excepcion) {

            return manejarExcepcion(excepcion);
        }
    }

    /**
     * Actualiza una sede.
     *
     * Operación exclusiva del administrador.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(
            @PathParam("id") Long id,
            ActualizarSedeRequest solicitud,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            SedeDTO sede =
                    administracionSedeService
                            .actualizar(
                                    id,
                                    solicitud,
                                    administrador.getUsername()
                            );

            return Response.ok(sede).build();

        } catch (RuntimeException excepcion) {

            return manejarExcepcion(excepcion);
        }
    }

    /**
     * Elimina una sede que no tenga partidos.
     *
     * Operación exclusiva del administrador.
     */
    @DELETE
    @Path("/{id}")
    public Response eliminar(
            @PathParam("id") Long id,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            administracionSedeService
                    .eliminar(
                            id,
                            administrador.getUsername()
                    );

            return Response
                    .ok(
                            Map.of(
                                    "mensaje",
                                    "Sede eliminada correctamente",
                                    "sedeId",
                                    id
                            )
                    )
                    .build();

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
                instanceof SedeNoEncontradaException) {

            return error(
                    Response.Status.NOT_FOUND,
                    excepcion.getMessage()
            );
        }

        if (excepcion
                instanceof SedeDuplicadaException
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
