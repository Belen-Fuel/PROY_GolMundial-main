package ec.edu.utn.golmundial.dto;

/**
 * Contiene los datos generados al proteger
 * una contraseña con PBKDF2.
 */
public class PasswordHashDTO {

    private final String hash;
    private final String salt;
    private final int iteraciones;

    public PasswordHashDTO(
            String hash,
            String salt,
            int iteraciones
    ) {
        this.hash = hash;
        this.salt = salt;
        this.iteraciones = iteraciones;
    }

    public String getHash() {
        return hash;
    }

    public String getSalt() {
        return salt;
    }

    public int getIteraciones() {
        return iteraciones;
    }
}
