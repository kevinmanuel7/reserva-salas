package cl.duoc.reservasalas.usuarios.exception;

/**
 * Se lanza al cambiar la contraseña si passwordActual no coincide con la guardada.
 * El manejador global la convierte en 400 CONTRASENA_INCORRECTA (no 401: el token
 * sí es válido, lo que falla es un dato del formulario).
 */
public class ContrasenaIncorrectaException extends RuntimeException {

    public ContrasenaIncorrectaException() {
        super("La contraseña actual no es correcta");
    }
}
