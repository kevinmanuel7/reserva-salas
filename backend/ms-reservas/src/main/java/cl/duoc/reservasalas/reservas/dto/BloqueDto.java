package cl.duoc.reservasalas.reservas.dto;

import cl.duoc.reservasalas.reservas.model.Bloque;

public record BloqueDto(Integer numero, String horaInicio, String horaFin) {
    public static BloqueDto desde(Bloque b) {
        return new BloqueDto(b.getNumero(), b.getHoraInicio(), b.getHoraFin());
    }
}
