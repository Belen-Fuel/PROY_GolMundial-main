package ec.edu.utn.golmundial.resource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Muestra un resumen de los registros cargados
 * en la base de datos del Servicio de Estadísticas.
 */
@Path("/datos/resumen")
@Produces(MediaType.APPLICATION_JSON)
public class ResumenDatosResource {

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    @GET
    public Response obtenerResumen() {

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put(
                "torneo",
                "Copa Mundial de la FIFA 2026"
        );

        respuesta.put(
                "roles",
                contar("Rol")
        );

        respuesta.put(
                "usuarios",
                contar("Usuario")
        );

        respuesta.put(
                "fases",
                contar("Fase")
        );

        respuesta.put(
                "grupos",
                contar("Grupo")
        );

        respuesta.put(
                "sedes",
                contar("Sede")
        );

        respuesta.put(
                "selecciones",
                contar("Seleccion")
        );

        respuesta.put(
                "partidos",
                contar("Partido")
        );

        respuesta.put(
                "estado",
                "DATOS_CARGADOS"
        );

        return Response
                .ok(respuesta)
                .build();
    }

    private Long contar(String entidad) {

        return entityManager
                .createQuery(
                        "SELECT COUNT(e) FROM "
                                + entidad
                                + " e",
                        Long.class
                )
                .getSingleResult();
    }
}
