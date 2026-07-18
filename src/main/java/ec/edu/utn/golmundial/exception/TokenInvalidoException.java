package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando un token no existe,
 * expiró o fue revocado.
 */
@ApplicationException(rollback = false)
public class TokenInvalidoException
        extends RuntimeException {

    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
