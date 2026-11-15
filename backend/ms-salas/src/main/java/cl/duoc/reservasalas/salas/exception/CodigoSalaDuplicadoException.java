package cl.duoc.reservasalas.salas.exception;

public class CodigoSalaDuplicadoException extends RuntimeException {
    public CodigoSalaDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
