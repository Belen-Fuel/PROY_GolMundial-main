package ec.edu.utn.golmundial.resource;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint técnico para comprobar la conexión entre:
 *
 * Jakarta EE -> JPA -> WildFly Datasource -> PostgreSQL.
 */
@Path("/salud/base-datos")
@Produces(MediaType.APPLICATION_JSON)
public class BaseDatosResource {

    /**
     * EntityManager administrado por WildFly.
     *
     * La unidad GolMundialPU se encuentra configurada
     * en META-INF/persistence.xml.
     */
    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    /**
     * Ejecuta una consulta real en PostgreSQL mediante JPA.
     *
     * URL:
     * GET /api/salud/base-datos
     */
    @GET
    public Response verificarConexion() {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        try {
            Object resultado = entityManager
                    .createNativeQuery(
                            "SELECT current_database(), current_user"
                    )
                    .getSingleResult();

            Object[] datos = (Object[]) resultado;

            respuesta.put("servicio", "Servicio de Estadísticas");
            respuesta.put("estado", "CONECTADO");
            respuesta.put("jpa", "GolMundialPU");
            respuesta.put("datasource", "java:/jdbc/GolMundialDS");
            respuesta.put("baseDatos", datos[0]);
            respuesta.put("usuario", datos[1]);
            respuesta.put("fechaHoraUtc", Instant.now().toString());

            return Response.ok(respuesta).build();

        } catch (Exception excepcion) {

            respuesta.put("servicio", "Servicio de Estadísticas");
            respuesta.put("estado", "ERROR");
            respuesta.put(
                    "mensaje",
                    "No se pudo consultar PostgreSQL mediante JPA"
            );
            respuesta.put(
                    "detalle",
                    excepcion.getMessage()
            );

            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(respuesta)
                    .build();
        }
    }
}
