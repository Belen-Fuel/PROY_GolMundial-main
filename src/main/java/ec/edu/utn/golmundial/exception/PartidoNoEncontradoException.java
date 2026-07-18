package ec.edu.utn.golmundial.exception;

import jakarta.ejb.ApplicationException;

/**
 * Se produce cuando el partido solicitado no existe.
 *
 * Se declara como excepción de aplicación para que
 * WildFly no la convierta en un error interno EJB.
 */
@ApplicationException(rollback = true)
public class PartidoNoEncontradoException
        extends RuntimeException {

    public PartidoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
