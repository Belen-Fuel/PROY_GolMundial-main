package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando un usuario autenticado intenta
 * ejecutar una operación que no corresponde a su rol.
 */
@ApplicationException(rollback = false)
public class RolNoAutorizadoException
        extends RuntimeException {

    public RolNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
