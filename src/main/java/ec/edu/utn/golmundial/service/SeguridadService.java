package ec.edu.utn.golmundial.service;

import ec.edu.utn.golmundial.dto.UsuarioSesionDTO;
import ec.edu.utn.golmundial.entity.SesionUsuario;
import ec.edu.utn.golmundial.entity.Usuario;
import ec.edu.utn.golmundial.exception.CuentaInactivaException;
import ec.edu.utn.golmundial.exception.RolNoAutorizadoException;
import ec.edu.utn.golmundial.exception.TokenInvalidoException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;

/**
 * Valida sesiones y permisos antes de ejecutar
 * operaciones protegidas.
 */
@Stateless
public class SeguridadService {

    private static final String PREFIJO_BEARER =
            "Bearer ";

    private static final String ROL_ADMINISTRADOR =
            "ADMINISTRADOR";

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    /**
     * Valida que el encabezado contenga un token válido
     * y que el usuario tenga rol ADMINISTRADOR.
     *
     * @param autorizacion encabezado Authorization.
     * @return información del administrador autenticado.
     */
    public UsuarioSesionDTO validarAdministrador(
            String autorizacion
    ) {

        Usuario usuario =
                obtenerUsuarioAutenticado(autorizacion);

        String nombreRol =
                usuario.getRol().getNombre();

        if (!ROL_ADMINISTRADOR.equalsIgnoreCase(
                nombreRol
        )) {

            throw new RolNoAutorizadoException(
                    "La operación requiere el rol ADMINISTRADOR"
            );
        }

        return convertirUsuario(usuario);
    }

    /**
     * Verifica el encabezado, el token, la expiración,
     * la revocación y el estado de la cuenta.
     */
    private Usuario obtenerUsuarioAutenticado(
            String autorizacion
    ) {

        String token =
                extraerToken(autorizacion);

        String tokenHash =
                calcularHashToken(token);

        OffsetDateTime ahora =
                OffsetDateTime.now(ZoneOffset.UTC);

        List<SesionUsuario> sesiones =
                entityManager
                        .createQuery(
                                "SELECT s FROM SesionUsuario s "
                                        + "WHERE s.tokenHash = :tokenHash "
                                        + "AND s.revocada = false "
                                        + "AND s.fechaExpiracionUtc > :ahora",
                                SesionUsuario.class
                        )
                        .setParameter(
                                "tokenHash",
                                tokenHash
                        )
                        .setParameter(
                                "ahora",
                                ahora
                        )
                        .setMaxResults(1)
                        .getResultList();

        if (sesiones.isEmpty()) {
            throw new TokenInvalidoException(
                    "El token no es válido, expiró o fue revocado"
            );
        }

        SesionUsuario sesion =
                sesiones.get(0);

        Usuario usuario =
                sesion.getUsuario();

        if (usuario == null) {
            throw new TokenInvalidoException(
                    "La sesión no tiene un usuario asociado"
            );
        }

        if (!usuario.isActivo()) {
            throw new CuentaInactivaException(
                    "La cuenta se encuentra inactiva"
            );
        }

        sesion.setUltimoAccesoUtc(ahora);
        entityManager.flush();

        return usuario;
    }

    /**
     * Extrae el token del encabezado Bearer.
     */
    private String extraerToken(
            String autorizacion
    ) {

        if (autorizacion == null
                || autorizacion.isBlank()) {

            throw new TokenInvalidoException(
                    "Debe enviar el encabezado Authorization"
            );
        }

        if (!autorizacion.startsWith(
                PREFIJO_BEARER
        )) {

            throw new TokenInvalidoException(
                    "El encabezado Authorization debe usar Bearer"
            );
        }

        String token = autorizacion
                .substring(PREFIJO_BEARER.length())
                .trim();

        if (token.isBlank()) {
            throw new TokenInvalidoException(
                    "El token de autenticación está vacío"
            );
        }

        return token;
    }

    /**
     * Calcula el mismo hash SHA-256 utilizado
     * por AutenticacionService.
     */
    private String calcularHashToken(
            String token
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash = digest.digest(
                    token.getBytes(
                            StandardCharsets.UTF_8
                    )
            );

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (Exception excepcion) {

            throw new IllegalStateException(
                    "No se pudo verificar el token",
                    excepcion
            );
        }
    }

    private UsuarioSesionDTO convertirUsuario(
            Usuario usuario
    ) {

        return new UsuarioSesionDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol().getNombre(),
                usuario.isActivo(),
                usuario.isCambioPasswordObligatorio()
        );
    }
}
