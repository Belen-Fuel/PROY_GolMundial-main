package ec.edu.utn.golmundial.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configuración principal de Jakarta REST.
 *
 * Todos los endpoints de la aplicación comenzarán con /api.
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
}
