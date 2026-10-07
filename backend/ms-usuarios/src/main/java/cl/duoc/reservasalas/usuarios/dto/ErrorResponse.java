package cl.duoc.reservasalas.usuarios.dto;

/**
 * Formato estándar de error del Contrato 1: {"codigo": ..., "mensaje": ...}.
 */
public record ErrorResponse(String codigo, String mensaje) {
}
