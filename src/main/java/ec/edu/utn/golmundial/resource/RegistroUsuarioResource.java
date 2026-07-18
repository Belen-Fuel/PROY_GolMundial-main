package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.RegistroUsuarioRequest;
import ec.edu.utn.golmundial.dto.UsuarioDTO;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import ec.edu.utn.golmundial.exception.UsuarioDuplicadoException;
import ec.edu.utn.golmundial.service.UsuarioService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Registro público de usuarios normales.
 */
@Path("/auth/registro")
@Produces(MediaType.APPLICATION_JSON)
public class RegistroUsuarioResource {

    @EJB
    private UsuarioService usuarioService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrar(
            RegistroUsuarioRequest solicitud
    ) {

        try {

            UsuarioDTO usuario =
                    usuarioService
                            .registrarPublicamente(
                                    solicitud
                            );

            return Response
                    .status(Response.Status.CREATED)
                    .entity(usuario)
                    .build();

        } catch (SolicitudInvalidaException excepcion) {

            return error(
                    Response.Status.BAD_REQUEST,
                    excepcion.getMessage()
            );

        } catch (UsuarioDuplicadoException excepcion) {

            return error(
                    Response.Status.CONFLICT,
                    excepcion.getMessage()
            );
        }
    }

    private Response error(
            Response.Status estado,
            String mensaje
    ) {

        return Response
                .status(estado)
                .entity(
                        Map.of(
                                "estadoHttp",
                                estado.getStatusCode(),
                                "error",
                                mensaje
                        )
                )
                .build();
    }
}

