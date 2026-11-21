package cl.duoc.reservasalas.reservas.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.reservasalas.reservas.dto.ErrorResponse;

/**
 * Convierte las excepciones de los controladores en el formato de error del
 * Contrato 1: {"codigo": ..., "mensaje": ...}.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Los datos enviados no son válidos"
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return responder(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "El cuerpo de la petición no es un JSON válido");
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(DatosInvalidosException ex) {
        return responder(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", ex.getMessage());
    }

    @ExceptionHandler(BloqueInvalidoException.class)
    public ResponseEntity<ErrorResponse> bloqueInvalido(BloqueInvalidoException ex) {
        return responder(HttpStatus.BAD_REQUEST, "BLOQUE_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(FechaPasadaException.class)
    public ResponseEntity<ErrorResponse> fechaPasada(FechaPasadaException ex) {
        return responder(HttpStatus.BAD_REQUEST, "FECHA_PASADA", ex.getMessage());
    }

    @ExceptionHandler(SalaInactivaException.class)
    public ResponseEntity<ErrorResponse> salaInactiva(SalaInactivaException ex) {
        return responder(HttpStatus.BAD_REQUEST, "SALA_INACTIVA", ex.getMessage());
    }

    @ExceptionHandler(SalaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> salaNoEncontrada(SalaNoEncontradaException ex) {
        return responder(HttpStatus.NOT_FOUND, "SALA_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(ReservaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> reservaNoEncontrada(ReservaNoEncontradaException ex) {
        return responder(HttpStatus.NOT_FOUND, "RESERVA_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> tokenInvalido(TokenInvalidoException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(SinPermisoException.class)
    public ResponseEntity<ErrorResponse> sinPermiso(SinPermisoException ex) {
        return responder(HttpStatus.FORBIDDEN, "SIN_PERMISO", ex.getMessage());
    }

    @ExceptionHandler(CruceHorarioException.class)
    public ResponseEntity<ErrorResponse> cruceHorario(CruceHorarioException ex) {
        return responder(HttpStatus.CONFLICT, "CRUCE_HORARIO", ex.getMessage());
    }

    @ExceptionHandler(UsuarioOcupadoException.class)
    public ResponseEntity<ErrorResponse> usuarioOcupado(UsuarioOcupadoException ex) {
        return responder(HttpStatus.CONFLICT, "USUARIO_OCUPADO", ex.getMessage());
    }

    @ExceptionHandler(ReservaNoActivaException.class)
    public ResponseEntity<ErrorResponse> reservaNoActiva(ReservaNoActivaException ex) {
        return responder(HttpStatus.CONFLICT, "RESERVA_NO_ACTIVA", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> otrosErrores(Exception ex) {
        if (ex instanceof org.springframework.web.ErrorResponse errorSpring) {
            return ResponseEntity.status(errorSpring.getStatusCode())
                    .body(new ErrorResponse("DATOS_INVALIDOS", "La petición no es válida para esta ruta"));
        }
        log.error("Error inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Intenta nuevamente más tarde");
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponse(codigo, mensaje));
    }
}
