package cl.duoc.reservasalas.usuarios.exception;

/**
 * Se lanza cuando los datos tienen el formato correcto pero no cumplen una regla
 * de negocio (ejemplo: fecha de nacimiento futura).
 * El manejador global la convierte en 400 DATOS_INVALIDOS.
 */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
