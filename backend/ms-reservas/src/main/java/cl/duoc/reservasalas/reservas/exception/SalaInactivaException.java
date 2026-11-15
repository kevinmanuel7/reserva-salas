package cl.duoc.reservasalas.reservas.exception;

public class SalaInactivaException extends RuntimeException {
    public SalaInactivaException(String mensaje) {
        super(mensaje);
    }
}
