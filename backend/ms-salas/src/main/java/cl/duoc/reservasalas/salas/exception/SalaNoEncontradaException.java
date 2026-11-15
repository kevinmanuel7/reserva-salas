package cl.duoc.reservasalas.salas.exception;

public class SalaNoEncontradaException extends RuntimeException {
    public SalaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
