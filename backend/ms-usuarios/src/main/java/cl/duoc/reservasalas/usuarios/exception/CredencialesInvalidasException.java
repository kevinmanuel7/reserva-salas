package cl.duoc.reservasalas.usuarios.exception;

/**
 * Se lanza cuando el email no existe o la contraseña no coincide.
 * El manejador global la convierte en 401 CREDENCIALES_INVALIDAS.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        // Mismo mensaje en ambos casos, para no revelar qué emails existen.
        super("Email o contraseña incorrectos");
    }
}
