package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando ya existe una selección
 * con el mismo código FIFA o nombre.
 */
@ApplicationException(rollback = true)
public class SeleccionDuplicadaException
        extends RuntimeException {

    public SeleccionDuplicadaException(
            String mensaje
    ) {
        super(mensaje);
    }
}
