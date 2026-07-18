package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando los datos recibidos por la API
 * son incompletos o no cumplen las validaciones.
 */
@ApplicationException(rollback = true)
public class SolicitudInvalidaException
        extends RuntimeException {

    public SolicitudInvalidaException(String mensaje) {
        super(mensaje);
    }
}
