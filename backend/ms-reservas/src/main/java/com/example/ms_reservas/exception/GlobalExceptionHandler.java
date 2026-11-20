package cl.duoc.reservasalas.reservas.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        String codigo = ex.getReason() != null ? ex.getReason() : "ERROR_INTERNO";
        String mensaje = obtenerMensajeAmigable(codigo);

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(new ErrorResponse(codigo, mensaje));
    }

    private String obtenerMensajeAmigable(String codigo) {
        switch (codigo) {
            case "DATOS_INVALIDOS": return "Faltan campos obligatorios o tienen un formato incorrecto.";
            case "BLOQUE_INVALIDO": return "Los bloques deben estar entre 1 y 13, y el bloque de fin debe ser mayor o igual al de inicio.";
            case "FECHA_PASADA": return "No se puede realizar una reserva en una fecha anterior a hoy.";
            case "SALA_INACTIVA": return "La sala seleccionada se encuentra fuera de servicio.";
            case "SALA_NO_ENCONTRADA": return "No existe la sala solicitada.";
            case "CRUCE_HORARIO": return "La sala ya está ocupada en alguno de esos bloques para la fecha seleccionada.";
            case "SIN_PERMISO": return "No tienes el permiso necesario para realizar esta acción.";
            case "RESERVA_NO_ENCONTRADA": return "No existe la reserva solicitada.";
            default: return "Ocurrió un error en el servidor.";
        }
    }
}