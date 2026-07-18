package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.GrupoDTO;
import ec.edu.utn.golmundial.service.ConsultaTorneoService;
import ec.edu.utn.golmundial.service.EstadisticaTorneoService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/grupos")
@Produces(MediaType.APPLICATION_JSON)
public class GrupoResource {

    @EJB
    private ConsultaTorneoService consultaService;

    @EJB
    private EstadisticaTorneoService estadisticaService;

    @GET
    public Response listar() {

        return Response
                .ok(consultaService.listarGrupos())
                .build();
    }

    @GET
    @Path("/{codigo}")
    public Response buscar(
            @PathParam("codigo") String codigo
    ) {

        GrupoDTO grupo =
                consultaService.buscarGrupo(codigo);

        if (grupo == null) {
            return grupoNoEncontrado();
        }

        return Response.ok(grupo).build();
    }

    @GET
    @Path("/{codigo}/selecciones")
    public Response listarSelecciones(
            @PathParam("codigo") String codigo
    ) {

        GrupoDTO grupo =
                consultaService.buscarGrupo(codigo);

        if (grupo == null) {
            return grupoNoEncontrado();
        }

        return Response
                .ok(
                        consultaService
                                .listarSelecciones(codigo)
                )
                .build();
    }

    /**
     * Tabla de posiciones del grupo.
     */
    @GET
    @Path("/{codigo}/posiciones")
    public Response obtenerPosiciones(
            @PathParam("codigo") String codigo
    ) {

        GrupoDTO grupo =
                consultaService.buscarGrupo(codigo);

        if (grupo == null) {
            return grupoNoEncontrado();
        }

        return Response
                .ok(
                        estadisticaService
                                .obtenerPosicionesGrupo(codigo)
                )
                .build();
    }

    private Response grupoNoEncontrado() {

        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(
                        Map.of(
                                "error",
                                "Grupo no encontrado"
                        )
                )
                .build();
    }
}
