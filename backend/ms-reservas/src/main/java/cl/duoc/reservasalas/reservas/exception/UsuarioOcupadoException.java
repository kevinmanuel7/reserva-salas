package cl.duoc.reservasalas.reservas.exception;

public class UsuarioOcupadoException extends RuntimeException {
    public UsuarioOcupadoException(String mensaje) {
        super(mensaje);
    }
}
