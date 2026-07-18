package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Representa una operación que no puede ejecutarse
 * por el estado actual del sistema.
 */
@ApplicationException(rollback = true)
public class ReglaNegocioException
        extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
