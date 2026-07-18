package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando el usuario o la contraseña
 * son incorrectos.
 */
@ApplicationException(rollback = false)
public class CredencialesInvalidasException
        extends RuntimeException {

    public CredencialesInvalidasException(
            String mensaje
    ) {
        super(mensaje);
    }
}
