package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando una cuenta está bloqueada.
 */
@ApplicationException(rollback = false)
public class CuentaInactivaException
        extends RuntimeException {

    public CuentaInactivaException(String mensaje) {
        super(mensaje);
    }
}
