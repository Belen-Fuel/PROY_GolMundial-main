package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.EventoIntegracionDTO;
import ec.edu.utn.golmundial.entity.EstadoEventoIntegracion;
import ec.edu.utn.golmundial.entity.EventoIntegracion;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Path("/integracion/eventos")
@Produces(MediaType.APPLICATION_JSON)
public class EventoIntegracionResource {

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
            String autorizacion,

            @QueryParam("estado")
            String estado
    ) {

        try {

            seguridadService
                    .validarAdministrador(
                            autorizacion
                    );

            List<EventoIntegracion> eventos;

            if (estado == null || estado.isBlank()) {

                eventos = entityManager
                        .createQuery(
                                "SELECT e FROM EventoIntegracion e "
                                        + "ORDER BY e.fechaCreacionUtc DESC",
                                EventoIntegracion.class
                        )
                        .getResultList();

            } else {

                EstadoEventoIntegracion estadoEnum =
                        EstadoEventoIntegracion.valueOf(
                                estado.trim()
                                        .toUpperCase(
                                                Locale.ROOT
                                        )
                        );

                eventos = entityManager
                        .createQuery(
                                "SELECT e FROM EventoIntegracion e "
                                        + "WHERE e.estado = :estado "
                                        + "ORDER BY e.fechaCreacionUtc DESC",
                                EventoIntegracion.class
                        )
                        .setParameter(
                                "estado",
                                estadoEnum
                        )
                        .getResultList();
            }

            return Response
                    .ok(
                            eventos.stream()
                                    .map(this::convertir)
                                    .toList()
                    )
                    .build();

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

        } catch (IllegalArgumentException excepcion) {

            return error(
                    Response.Status.BAD_REQUEST,
                    "Estado de evento no válido"
            );
        }
    }

    private EventoIntegracionDTO convertir(
            EventoIntegracion evento
    ) {

        return new EventoIntegracionDTO(
                evento.getId(),
                evento.getTipoEvento(),
                evento.getEntidad(),
                evento.getEntidadId(),
                evento.getPayloadJson(),
                evento.getEstado().name(),
                evento.getIntentos(),
                evento.getUltimoError(),
                evento.getFechaCreacionUtc().toString(),
                evento.getFechaActualizacionUtc().toString()
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
