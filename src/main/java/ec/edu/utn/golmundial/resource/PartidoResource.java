package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.PartidoDTO;
import ec.edu.utn.golmundial.dto.ResultadoPartidoDTO;
import ec.edu.utn.golmundial.dto.ResultadoPartidoRequest;
import ec.edu.utn.golmundial.dto.UsuarioSesionDTO;
import ec.edu.utn.golmundial.exception.CuentaInactivaException;
import ec.edu.utn.golmundial.exception.PartidoNoEncontradoException;
import ec.edu.utn.golmundial.exception.ReglaNegocioException;
import ec.edu.utn.golmundial.exception.RolNoAutorizadoException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import ec.edu.utn.golmundial.exception.TokenInvalidoException;
import ec.edu.utn.golmundial.service.AdministracionPartidoService;
import ec.edu.utn.golmundial.service.ConsultaTorneoService;
import ec.edu.utn.golmundial.service.SeguridadService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/partidos")
@Produces(MediaType.APPLICATION_JSON)
public class PartidoResource {

    @EJB
    private ConsultaTorneoService consultaService;

    @EJB
    private AdministracionPartidoService
            administracionPartidoService;

    @EJB
    private SeguridadService seguridadService;

    /**
     * Consulta pública de partidos.
     */
    @GET
    public Response listar(
            @QueryParam("grupo") String grupo,
            @QueryParam("fase") String fase,
            @QueryParam("estado") String estado
    ) {

        try {

            return Response
                    .ok(
                            consultaService.listarPartidos(
                                    grupo,
                                    fase,
                                    estado
                            )
                    )
                    .build();

        } catch (IllegalArgumentException excepcion) {

            return respuestaError(
                    Response.Status.BAD_REQUEST,
                    excepcion.getMessage()
            );
        }
    }

    /**
     * Consulta pública del detalle de un partido.
     */
    @GET
    @Path("/{id}")
    public Response buscar(
            @PathParam("id") Long id
    ) {

        PartidoDTO partido =
                consultaService.buscarPartido(id);

        if (partido == null) {

            return respuestaError(
                    Response.Status.NOT_FOUND,
                    "Partido no encontrado"
            );
        }

        return Response.ok(partido).build();
    }

    /**
     * Operación exclusiva del administrador.
     */
    @PUT
    @Path("/{id}/resultado")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarResultado(
            @PathParam("id") Long id,

            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion,

            ResultadoPartidoRequest solicitud
    ) {

        try {

            UsuarioSesionDTO administrador =
                    seguridadService
                            .validarAdministrador(
                                    autorizacion
                            );

            ResultadoPartidoDTO resultado =
                    administracionPartidoService
                            .registrarResultado(
                                    id,
                                    solicitud,
                                    administrador.getUsername()
                            );

            return Response.ok(resultado).build();

        } catch (TokenInvalidoException excepcion) {

            return respuestaError(
                    Response.Status.UNAUTHORIZED,
                    excepcion.getMessage()
            );

        } catch (CuentaInactivaException excepcion) {

            return respuestaError(
                    Response.Status.FORBIDDEN,
                    excepcion.getMessage()
            );

        } catch (RolNoAutorizadoException excepcion) {

            return respuestaError(
                    Response.Status.FORBIDDEN,
                    excepcion.getMessage()
            );

        } catch (PartidoNoEncontradoException excepcion) {

            return respuestaError(
                    Response.Status.NOT_FOUND,
                    excepcion.getMessage()
            );

        } catch (ReglaNegocioException excepcion) {

            return respuestaError(
                    Response.Status.CONFLICT,
                    excepcion.getMessage()
            );

        } catch (SolicitudInvalidaException excepcion) {

            return respuestaError(
                    Response.Status.BAD_REQUEST,
                    excepcion.getMessage()
            );
        }
    }

    private Response respuestaError(
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
