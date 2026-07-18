package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando la selección solicitada no existe.
 */
@ApplicationException(rollback = false)
public class SeleccionNoEncontradaException
        extends RuntimeException {

    public SeleccionNoEncontradaException(
            String mensaje
    ) {
        super(mensaje);
    }
}
