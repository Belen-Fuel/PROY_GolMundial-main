package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.ActualizarSeleccionRequest;
import ec.edu.utn.golmundial.dto.CrearSeleccionRequest;
import ec.edu.utn.golmundial.dto.EstadisticaSeleccionDTO;
import ec.edu.utn.golmundial.dto.SeleccionDTO;
import ec.edu.utn.golmundial.dto.UsuarioSesionDTO;
import ec.edu.utn.golmundial.exception.CuentaInactivaException;
import ec.edu.utn.golmundial.exception.ReglaNegocioException;
import ec.edu.utn.golmundial.exception.RolNoAutorizadoException;
import ec.edu.utn.golmundial.exception.SeleccionDuplicadaException;
import ec.edu.utn.golmundial.exception.SeleccionNoEncontradaException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import ec.edu.utn.golmundial.exception.TokenInvalidoException;
import ec.edu.utn.golmundial.service.AdministracionSeleccionService;
import ec.edu.utn.golmundial.service.ConsultaTorneoService;
import ec.edu.utn.golmundial.service.EstadisticaTorneoService;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Consultas públicas y operaciones administrativas
 * sobre las selecciones.
 */
@Path("/selecciones")
@Produces(MediaType.APPLICATION_JSON)
public class SeleccionResource {

    @EJB
    private ConsultaTorneoService consultaService;

    @EJB
    private EstadisticaTorneoService estadisticaService;

    @EJB
    private AdministracionSeleccionService
            administracionSeleccionService;

    @EJB
    private SeguridadService seguridadService;

    /**
     * Consulta pública.
     */
    @GET
    public Response listar(
            @QueryParam("grupo") String grupo
    ) {

        return Response
                .ok(
                        consultaService
                                .listarSelecciones(grupo)
                )
                .build();
    }

    /**
     * Consulta pública.
     */
    @GET
    @Path("/{id}")
    public Response buscar(
            @PathParam("id") Long id
    ) {

        SeleccionDTO seleccion =
                consultaService.buscarSeleccion(id);

        if (seleccion == null) {
            return error(
                    Response.Status.NOT_FOUND,
                    "Selección no encontrada"
            );
        }

        return Response.ok(seleccion).build();
    }

    /**
     * Consulta pública.
     */
    @GET
    @Path("/{id}/estadisticas")
    public Response obtenerEstadisticas(
            @PathParam("id") Long id
    ) {

        EstadisticaSeleccionDTO estadisticas =
                estadisticaService
                        .obtenerEstadisticasSeleccion(id);

        if (estadisticas == null) {
            return error(
                    Response.Status.NOT_FOUND,
                    "Selección no encontrada"
            );
        }

        return Response
                .ok(estadisticas)
                .build();
    }

    /**
     * Operación exclusiva del administrador.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(
            CrearSeleccionRequest solicitud,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            SeleccionDTO seleccion =
                    administracionSeleccionService
                            .crear(
                                    solicitud,
                                    administrador.getUsername()
                            );

            return Response
                    .status(Response.Status.CREATED)
                    .entity(seleccion)
                    .build();

        } catch (RuntimeException excepcion) {
            return manejarExcepcion(excepcion);
        }
    }

    /**
     * Operación exclusiva del administrador.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(
            @PathParam("id") Long id,
            ActualizarSeleccionRequest solicitud,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            SeleccionDTO seleccion =
                    administracionSeleccionService
                            .actualizar(
                                    id,
                                    solicitud,
                                    administrador.getUsername()
                            );

            return Response
                    .ok(seleccion)
                    .build();

        } catch (RuntimeException excepcion) {
            return manejarExcepcion(excepcion);
        }
    }

    /**
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

            administracionSeleccionService
                    .eliminar(
                            id,
                            administrador.getUsername()
                    );

            return Response.ok(
                    Map.of(
                            "mensaje",
                            "Selección eliminada correctamente",
                            "seleccionId",
                            id
                    )
            ).build();

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
                instanceof SeleccionNoEncontradaException) {

            return error(
                    Response.Status.NOT_FOUND,
                    excepcion.getMessage()
            );
        }

        if (excepcion
                instanceof SeleccionDuplicadaException
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
