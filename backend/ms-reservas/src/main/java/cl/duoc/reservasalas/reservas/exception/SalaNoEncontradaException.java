package cl.duoc.reservasalas.reservas.exception;

public class SalaNoEncontradaException extends RuntimeException {
    public SalaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
