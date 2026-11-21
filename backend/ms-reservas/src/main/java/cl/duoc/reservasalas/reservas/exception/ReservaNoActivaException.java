package cl.duoc.reservasalas.reservas.exception;

public class ReservaNoActivaException extends RuntimeException {
    public ReservaNoActivaException(String mensaje) {
        super(mensaje);
    }
}
