package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.GrupoDTO;
import ec.edu.utn.golmundial.service.ConsultaTorneoService;
import ec.edu.utn.golmundial.service.EstadisticaTorneoService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/posiciones")
@Produces(MediaType.APPLICATION_JSON)
public class PosicionResource {

    @EJB
    private ConsultaTorneoService consultaService;

    @EJB
    private EstadisticaTorneoService estadisticaService;

    /**
     * GET /api/posiciones
     * GET /api/posiciones?grupo=A
     */
    @GET
    public Response listar(
            @QueryParam("grupo") String grupo
    ) {

        if (grupo != null && !grupo.isBlank()) {

            GrupoDTO grupoEncontrado =
                    consultaService.buscarGrupo(grupo);

            if (grupoEncontrado == null) {

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

            return Response
                    .ok(
                            estadisticaService
                                    .obtenerPosicionesGrupo(grupo)
                    )
                    .build();
        }

        return Response
                .ok(
                        estadisticaService
                                .obtenerTodasLasPosiciones()
                )
                .build();
    }
}
