package ec.edu.utn.golmundial.service;

import ec.edu.utn.golmundial.dto.PasswordHashDTO;
import ec.edu.utn.golmundial.exception.SolicitudInvalidaException;
import jakarta.ejb.Stateless;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Servicio encargado de proteger y verificar
 * las contraseñas de los usuarios.
 *
 * Utiliza:
 *
 * - PBKDF2WithHmacSHA256.
 * - Sal aleatoria individual.
 * - 210 000 iteraciones.
 * - Hash de 256 bits.
 *
 * Las contraseñas nunca se almacenan en texto plano.
 */
@Stateless
public class PasswordService {

    private static final String ALGORITMO =
            "PBKDF2WithHmacSHA256";

    private static final int ITERACIONES =
            210_000;

    private static final int TAMANO_SAL_BYTES =
            16;

    private static final int TAMANO_HASH_BITS =
            256;

    private final SecureRandom secureRandom =
            new SecureRandom();

    /**
     * Genera un hash seguro para una contraseña nueva.
     *
     * @param password contraseña recibida.
     * @return hash, sal e iteraciones utilizadas.
     */
    public PasswordHashDTO generarHash(
            String password
    ) {

        validarPassword(password);

        byte[] salt =
                new byte[TAMANO_SAL_BYTES];

        secureRandom.nextBytes(salt);

        byte[] hash = calcularHash(
                password.toCharArray(),
                salt,
                ITERACIONES
        );

        return new PasswordHashDTO(
                Base64.getEncoder()
                        .encodeToString(hash),

                Base64.getEncoder()
                        .encodeToString(salt),

                ITERACIONES
        );
    }

    /**
     * Comprueba si una contraseña coincide con
     * el hash almacenado en PostgreSQL.
     *
     * @param password contraseña recibida.
     * @param hashGuardado hash almacenado.
     * @param saltGuardado sal almacenada.
     * @param iteraciones número de iteraciones.
     * @return true cuando la contraseña es correcta.
     */
    public boolean verificar(
            String password,
            String hashGuardado,
            String saltGuardado,
            int iteraciones
    ) {

        if (password == null
                || hashGuardado == null
                || saltGuardado == null
                || iteraciones <= 0) {

            return false;
        }

        try {

            byte[] salt = Base64
                    .getDecoder()
                    .decode(saltGuardado);

            byte[] hashEsperado = Base64
                    .getDecoder()
                    .decode(hashGuardado);

            byte[] hashRecibido =
                    calcularHash(
                            password.toCharArray(),
                            salt,
                            iteraciones
                    );

            /*
             * Evita una comparación vulnerable
             * a ataques de tiempo.
             */
            return MessageDigest.isEqual(
                    hashEsperado,
                    hashRecibido
            );

        } catch (IllegalArgumentException excepcion) {

            return false;
        }
    }

    /**
     * Ejecuta el algoritmo PBKDF2.
     */
    private byte[] calcularHash(
            char[] password,
            byte[] salt,
            int iteraciones
    ) {

        PBEKeySpec especificacion =
                new PBEKeySpec(
                        password,
                        salt,
                        iteraciones,
                        TAMANO_HASH_BITS
                );

        try {

            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance(
                            ALGORITMO
                    );

            return fabrica
                    .generateSecret(especificacion)
                    .getEncoded();

        } catch (Exception excepcion) {

            throw new IllegalStateException(
                    "No se pudo proteger la contraseña",
                    excepcion
            );

        } finally {

            especificacion.clearPassword();
        }
    }

    /**
     * Valida las reglas mínimas de la contraseña.
     *
     * Se utiliza SolicitudInvalidaException porque
     * está declarada como excepción de aplicación EJB.
     * De esta forma WildFly no la transforma en un
     * error HTTP 500.
     */
    private void validarPassword(
            String password
    ) {

        if (password == null
                || password.isBlank()) {

            throw new SolicitudInvalidaException(
                    "La contraseña es obligatoria"
            );
        }

        if (password.length() < 8) {

            throw new SolicitudInvalidaException(
                    "La contraseña debe tener "
                            + "al menos 8 caracteres"
            );
        }

        if (password.length() > 128) {

            throw new SolicitudInvalidaException(
                    "La contraseña no puede superar "
                            + "los 128 caracteres"
            );
        }
    }
}
