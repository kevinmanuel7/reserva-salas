package cl.duoc.reservasalas.salas.exception;

public class SinPermisoException extends RuntimeException {
    public SinPermisoException(String mensaje) {
        super(mensaje);
    }
}
