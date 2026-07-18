package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando la cuenta solicitada no existe.
 */
@ApplicationException(rollback = false)
public class UsuarioNoEncontradoException
        extends RuntimeException {

    public UsuarioNoEncontradoException(
            String mensaje
    ) {
        super(mensaje);
    }
}
