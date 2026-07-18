package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando ya existe una cuenta
 * con el mismo username.
 */
@ApplicationException(rollback = true)
public class UsuarioDuplicadoException
        extends RuntimeException {

    public UsuarioDuplicadoException(
            String mensaje
    ) {
        super(mensaje);
    }
}
