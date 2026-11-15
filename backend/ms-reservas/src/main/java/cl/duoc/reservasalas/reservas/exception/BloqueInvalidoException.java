package cl.duoc.reservasalas.reservas.exception;

public class BloqueInvalidoException extends RuntimeException {
    public BloqueInvalidoException(String mensaje) {
        super(mensaje);
    }
}
