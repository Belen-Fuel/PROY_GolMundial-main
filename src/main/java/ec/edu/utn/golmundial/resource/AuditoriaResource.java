package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.AuditoriaDTO;
import ec.edu.utn.golmundial.entity.Auditoria;
import ec.edu.utn.golmundial.exception.CuentaInactivaException;
import ec.edu.utn.golmundial.exception.RolNoAutorizadoException;
import ec.edu.utn.golmundial.exception.TokenInvalidoException;
import ec.edu.utn.golmundial.service.SeguridadService;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/auditoria")
@Produces(MediaType.APPLICATION_JSON)
public class AuditoriaResource {

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    @EJB
    private SeguridadService seguridadService;

    /**
     * Consulta exclusiva del administrador.
     */
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

            List<AuditoriaDTO> resultado =
                    entityManager
                            .createQuery(
                                    "SELECT a FROM Auditoria a "
                                            + "ORDER BY a.fechaHoraUtc DESC",
                                    Auditoria.class
                            )
                            .setMaxResults(100)
                            .getResultList()
                            .stream()
                            .map(this::convertir)
                            .toList();

            return Response.ok(resultado).build();

        } catch (TokenInvalidoException excepcion) {

            return error(
                    Response.Status.UNAUTHORIZED,
                    excepcion.getMessage()
            );

        } catch (CuentaInactivaException excepcion) {

            return error(
                    Response.Status.FORBIDDEN,
                    excepcion.getMessage()
            );

        } catch (RolNoAutorizadoException excepcion) {

            return error(
                    Response.Status.FORBIDDEN,
                    excepcion.getMessage()
            );
        }
    }

    private AuditoriaDTO convertir(
            Auditoria auditoria
    ) {

        return new AuditoriaDTO(
                auditoria.getId(),
                auditoria.getAccion(),
                auditoria.getEntidad(),
                auditoria.getEntidadId(),
                auditoria.getUsuarioReferencia(),
                auditoria.getDetalle(),
                auditoria.getFechaHoraUtc().toString()
        );
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
