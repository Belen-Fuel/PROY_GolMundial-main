package ec.edu.utn.golmundial.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint utilizado para comprobar que el Servicio
 * de Estadísticas está iniciado correctamente.
 */
@Path("/salud")
@Produces(MediaType.APPLICATION_JSON)
public class SaludResource {

    /**
     * Comprueba el estado general de la API.
     *
     * URL:
     * GET /api/salud
     *
     * @return información básica sobre el servicio.
     */
    @GET
    public Response verificarEstado() {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put(
                "servicio",
                "UTN GolMundial 2026 - Servicio de Estadísticas"
        );

        respuesta.put("estado", "ACTIVO");
        respuesta.put("tecnologia", "Jakarta EE 10");
        respuesta.put("servidor", "WildFly");
        respuesta.put("java", System.getProperty("java.version"));
        respuesta.put("fechaHoraUtc", Instant.now().toString());

        return Response.ok(respuesta).build();
    }
}
