package ec.edu.utn.golmundial.resource;

import ec.edu.utn.golmundial.dto.LoginRequest;
import ec.edu.utn.golmundial.dto.LoginResponse;
import ec.edu.utn.golmundial.dto.UsuarioSesionDTO;
import ec.edu.utn.golmundial.exception.CredencialesInvalidasException;
import ec.edu.utn.golmundial.exception.CuentaInactivaException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import ec.edu.utn.golmundial.exception.TokenInvalidoException;
import ec.edu.utn.golmundial.service.AutenticacionService;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

/**
 * Endpoints de autenticación.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @EJB
    private AutenticacionService
            autenticacionService;

    /**
     * POST /api/auth/login
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response iniciarSesion(
            LoginRequest solicitud
    ) {

        try {

            LoginResponse resultado =
                    autenticacionService
                            .iniciarSesion(solicitud);

            return Response.ok(resultado).build();

        } catch (
                SolicitudInvalidaException excepcion
        ) {

            return error(
                    Response.Status.BAD_REQUEST,
                    excepcion.getMessage()
            );

        } catch (
                CredencialesInvalidasException excepcion
        ) {

            return error(
                    Response.Status.UNAUTHORIZED,
                    excepcion.getMessage()
            );

        } catch (
                CuentaInactivaException excepcion
        ) {

            return error(
                    Response.Status.FORBIDDEN,
                    excepcion.getMessage()
            );
        }
    }

    /**
     * GET /api/auth/perfil
     */
    @GET
    @Path("/perfil")
    public Response obtenerPerfil(
            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            String token =
                    extraerToken(autorizacion);

            UsuarioSesionDTO perfil =
                    autenticacionService
                            .obtenerPerfil(token);

            return Response.ok(perfil).build();

        } catch (
                TokenInvalidoException excepcion
        ) {

            return error(
                    Response.Status.UNAUTHORIZED,
                    excepcion.getMessage()
            );

        } catch (
                CuentaInactivaException excepcion
        ) {

            return error(
                    Response.Status.FORBIDDEN,
                    excepcion.getMessage()
            );
        }
    }

    /**
     * POST /api/auth/logout
     */
    @POST
    @Path("/logout")
    public Response cerrarSesion(
            @HeaderParam(HttpHeaders.AUTHORIZATION)
            String autorizacion
    ) {

        try {

            String token =
                    extraerToken(autorizacion);

            autenticacionService
                    .cerrarSesion(token);

            return Response.ok(
                    Map.of(
                            "mensaje",
                            "Sesión cerrada correctamente"
                    )
            ).build();

        } catch (
                TokenInvalidoException excepcion
        ) {

            return error(
                    Response.Status.UNAUTHORIZED,
                    excepcion.getMessage()
            );
        }
    }

    private String extraerToken(
            String autorizacion
    ) {

        if (autorizacion == null
                || autorizacion.isBlank()) {

            throw new TokenInvalidoException(
                    "Debe enviar el encabezado Authorization"
            );
        }

        String prefijo = "Bearer ";

        if (!autorizacion.startsWith(prefijo)) {
            throw new TokenInvalidoException(
                    "El encabezado Authorization debe usar Bearer"
            );
        }

        String token = autorizacion
                .substring(prefijo.length())
                .trim();

        if (token.isBlank()) {
            throw new TokenInvalidoException(
                    "El token de autenticación está vacío"
            );
        }

        return token;
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
