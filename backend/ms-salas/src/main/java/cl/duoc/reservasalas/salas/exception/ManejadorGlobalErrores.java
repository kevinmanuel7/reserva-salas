package cl.duoc.reservasalas.salas.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.reservasalas.salas.dto.ErrorResponse;

/**
 * Convierte las excepciones de los controladores en el formato de error del
 * Contrato 1: {"codigo": ..., "mensaje": ...}.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    // Falla una anotación del DTO (@NotBlank, @Min...) → 400 DATOS_INVALIDOS.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().isEmpty()
                ? "Los datos enviados no son válidos"
                : ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return responder(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", mensaje);
    }

    // El cuerpo no es un JSON válido (o falta) → 400 DATOS_INVALIDOS.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonInvalido(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", "El cuerpo de la petición no es un JSON válido");
    }

    // No existe la sala pedida → 404 SALA_NO_ENCONTRADA.
    @ExceptionHandler(SalaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> salaNoEncontrada(SalaNoEncontradaException ex) {
        return responder(HttpStatus.NOT_FOUND, "SALA_NO_ENCONTRADA", ex.getMessage());
    }

    // Código de sala ya en uso → 400 DATOS_INVALIDOS.
    @ExceptionHandler(CodigoSalaDuplicadoException.class)
    public ResponseEntity<ErrorResponse> codigoDuplicado(CodigoSalaDuplicadoException ex) {
        return responder(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS", ex.getMessage());
    }

    // Token inválido o ausente en ruta que lo requiere → 401 TOKEN_INVALIDO.
    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErrorResponse> tokenInvalido(TokenInvalidoException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "TOKEN_INVALIDO", ex.getMessage());
    }

    // Rol no permitido para la acción → 403 SIN_PERMISO.
    @ExceptionHandler(SinPermisoException.class)
    public ResponseEntity<ErrorResponse> sinPermiso(SinPermisoException ex) {
        return responder(HttpStatus.FORBIDDEN, "SIN_PERMISO", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> otrosErrores(Exception ex) {
        // Errores de Spring MVC sobre la forma de la petición (ruta inexistente, método
        // no permitido, Content-Type distinto de JSON...). Se conserva su código HTTP
        // (404, 405, 415...) y se informan como DATOS_INVALIDOS.
        if (ex instanceof org.springframework.web.ErrorResponse errorSpring) {
            return ResponseEntity.status(errorSpring.getStatusCode())
                    .body(new ErrorResponse("DATOS_INVALIDOS", "La petición no es válida para esta ruta"));
        }
        // Cualquier otro error inesperado → 500 ERROR_INTERNO. El detalle va al log, no a la app.
        log.error("Error inesperado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Intenta nuevamente más tarde");
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponse(codigo, mensaje));
    }
}
