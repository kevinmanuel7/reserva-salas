package cl.duoc.reservasalas.usuarios.exception;

/**
 * Se lanza cuando GET /usuarios/{id} pide un id que no existe.
 * El manejador global la convierte en 404 USUARIO_NO_ENCONTRADO.
 */
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException() {
        super("No existe el usuario pedido");
    }
}
