package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando ya existe una sede
 * con el mismo nombre.
 */
@ApplicationException(rollback = true)
public class SedeDuplicadaException
        extends RuntimeException {

    public SedeDuplicadaException(
            String mensaje
    ) {
        super(mensaje);
    }
}
