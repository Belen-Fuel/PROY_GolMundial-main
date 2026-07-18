package ec.edu.utn.golmundial.seed;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Ejecuta la carga del seed cuando WildFly despliega
 * el Servicio de Estadísticas.
 */
@Singleton
@Startup
public class SeedStartup {

    private static final Logger LOGGER =
            Logger.getLogger(SeedStartup.class.getName());

    @EJB
    private SeedService seedService;

    @PostConstruct
    public void inicializar() {

        try {
            LOGGER.info(
                    "Iniciando verificación del seed "
                            + "UTN GolMundial 2026..."
            );

            seedService.cargarDatosIniciales();

        } catch (Exception excepcion) {

            LOGGER.log(
                    Level.SEVERE,
                    "No se pudo cargar el seed del Mundial 2026",
                    excepcion
            );

            throw new IllegalStateException(
                    "Error durante la carga del seed",
                    excepcion
            );
        }
    }
}
