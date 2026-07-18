package ec.edu.utn.golmundial.service;

import ec.edu.utn.golmundial.dto.LoginRequest;
import ec.edu.utn.golmundial.dto.LoginResponse;
import ec.edu.utn.golmundial.dto.UsuarioSesionDTO;
import ec.edu.utn.golmundial.entity.SesionUsuario;
import ec.edu.utn.golmundial.entity.Usuario;
import ec.edu.utn.golmundial.exception.CredencialesInvalidasException;
import ec.edu.utn.golmundial.exception.CuentaInactivaException;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import ec.edu.utn.golmundial.exception.TokenInvalidoException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Gestiona el inicio de sesión, la validación
 * de tokens, la consulta del perfil y el cierre
 * de sesión de los usuarios.
 */
@Stateless
public class AutenticacionService {

    /**
     * Duración máxima de una sesión iniciada.
     */
    private static final int HORAS_DURACION_TOKEN = 8;

    /**
     * Tamaño del token aleatorio antes de convertirlo
     * a Base64 URL.
     */
    private static final int TAMANO_TOKEN_BYTES = 32;

    @PersistenceContext(unitName = "GolMundialPU")
    private EntityManager entityManager;

    @EJB
    private PasswordService passwordService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    /**
     * Verifica las credenciales y crea una sesión.
     *
     * @param solicitud credenciales recibidas.
     * @return token e información del usuario.
     */
    public LoginResponse iniciarSesion(
            LoginRequest solicitud
    ) {

        validarSolicitudLogin(solicitud);

        String usernameNormalizado =
                solicitud.getUsername()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        /*
         * En esta consulta sí es válido utilizar JOIN FETCH,
         * porque no se asigna ningún alias a la relación rol.
         */
        List<Usuario> usuarios = entityManager
                .createQuery(
                        "SELECT u FROM Usuario u "
                                + "JOIN FETCH u.rol "
                                + "WHERE LOWER(u.username) = :username",
                        Usuario.class
                )
                .setParameter(
                        "username",
                        usernameNormalizado
                )
                .setMaxResults(1)
                .getResultList();

        /*
         * Se devuelve el mismo mensaje tanto para usuario
         * inexistente como para contraseña incorrecta.
         */
        if (usuarios.isEmpty()) {
            throw credencialesIncorrectas();
        }

        Usuario usuario = usuarios.get(0);

        if (!usuario.isActivo()) {
            throw new CuentaInactivaException(
                    "La cuenta se encuentra inactiva"
            );
        }

        boolean passwordCorrecto =
                passwordService.verificar(
                        solicitud.getPassword(),
                        usuario.getPasswordHash(),
                        usuario.getPasswordSalt(),
                        usuario.getPasswordIteraciones()
                );

        if (!passwordCorrecto) {
            throw credencialesIncorrectas();
        }

        String tokenReal = generarToken();
        String tokenHash =
                calcularHashToken(tokenReal);

        OffsetDateTime fechaExpiracion =
                OffsetDateTime
                        .now(ZoneOffset.UTC)
                        .plusHours(
                                HORAS_DURACION_TOKEN
                        );

        SesionUsuario sesion =
                new SesionUsuario(
                        usuario,
                        tokenHash,
                        fechaExpiracion
                );

        /*
         * PostgreSQL solamente almacena el hash
         * del token, nunca el token real.
         */
        entityManager.persist(sesion);
        entityManager.flush();

        return new LoginResponse(
                tokenReal,
                "Bearer",
                fechaExpiracion.toString(),
                convertirUsuario(usuario),
                "Inicio de sesión correcto"
        );
    }

    /**
     * Obtiene el perfil asociado a un token válido.
     *
     * @param token token real recibido en Authorization.
     * @return información pública del usuario.
     */
    public UsuarioSesionDTO obtenerPerfil(
            String token
    ) {

        SesionUsuario sesion =
                buscarSesionValida(token);

        actualizarUltimoAcceso(sesion);

        return convertirUsuario(
                sesion.getUsuario()
        );
    }

    /**
     * Revoca la sesión asociada al token.
     *
     * @param token token que se desea invalidar.
     */
    public void cerrarSesion(String token) {

        SesionUsuario sesion =
                buscarSesionValida(token);

        sesion.setRevocada(true);

        sesion.setUltimoAccesoUtc(
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        entityManager.flush();
    }

    /**
     * Busca una sesión activa, no expirada
     * y no revocada.
     *
     * Usuario y Rol están configurados con FetchType.EAGER,
     * por lo que no es necesario utilizar JOIN FETCH.
     */
    private SesionUsuario buscarSesionValida(
            String token
    ) {

        if (token == null || token.isBlank()) {
            throw new TokenInvalidoException(
                    "Debe enviar un token de autenticación"
            );
        }

        String tokenHash =
                calcularHashToken(token.trim());

        OffsetDateTime ahora =
                OffsetDateTime.now(ZoneOffset.UTC);

        /*
         * Consulta corregida:
         *
         * Se eliminaron:
         * JOIN FETCH s.usuario u
         * JOIN FETCH u.rol
         *
         * Esas instrucciones provocaban el error:
         * "The JPA specification does not permit
         * specifying an alias for fetch joins".
         */
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

        return sesion;
    }

    /**
     * Actualiza la fecha del último uso del token.
     */
    private void actualizarUltimoAcceso(
            SesionUsuario sesion
    ) {

        sesion.setUltimoAccesoUtc(
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        entityManager.flush();
    }

    /**
     * Valida que el cuerpo del login contenga
     * usuario y contraseña.
     */
    private void validarSolicitudLogin(
            LoginRequest solicitud
    ) {

        if (solicitud == null) {
            throw new SolicitudInvalidaException(
                    "Debe enviar las credenciales"
            );
        }

        if (solicitud.getUsername() == null
                || solicitud.getUsername().isBlank()) {

            throw new SolicitudInvalidaException(
                    "El nombre de usuario es obligatorio"
            );
        }

        if (solicitud.getPassword() == null
                || solicitud.getPassword().isBlank()) {

            throw new SolicitudInvalidaException(
                    "La contraseña es obligatoria"
            );
        }
    }

    /**
     * Construye un mensaje genérico para no revelar
     * si el usuario existe en la base de datos.
     */
    private CredencialesInvalidasException
    credencialesIncorrectas() {

        return new CredencialesInvalidasException(
                "Usuario o contraseña incorrectos"
        );
    }

    /**
     * Genera un token criptográficamente aleatorio.
     */
    private String generarToken() {

        byte[] bytes =
                new byte[TAMANO_TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    /**
     * Convierte el token real en un hash SHA-256.
     *
     * La base de datos almacena únicamente este hash.
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
                    "No se pudo proteger el token",
                    excepcion
            );
        }
    }

    /**
     * Convierte la entidad Usuario en una respuesta
     * que no expone hash, sal ni contraseña.
     */
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
