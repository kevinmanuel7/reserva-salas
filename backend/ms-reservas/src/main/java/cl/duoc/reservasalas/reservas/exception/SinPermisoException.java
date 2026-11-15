package cl.duoc.reservasalas.reservas.exception;

public class SinPermisoException extends RuntimeException {
    public SinPermisoException(String mensaje) {
        super(mensaje);
    }
}
