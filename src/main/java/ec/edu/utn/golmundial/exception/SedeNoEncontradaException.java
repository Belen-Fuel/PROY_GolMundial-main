package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando la sede solicitada no existe.
 */
@ApplicationException(rollback = false)
public class SedeNoEncontradaException
        extends RuntimeException {

    public SedeNoEncontradaException(
            String mensaje
    ) {
        super(mensaje);
    }
}
