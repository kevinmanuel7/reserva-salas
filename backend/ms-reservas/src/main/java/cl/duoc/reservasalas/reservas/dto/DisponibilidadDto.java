package cl.duoc.reservasalas.reservas.dto;

import java.time.LocalDate;

import cl.duoc.reservasalas.reservas.model.Reserva;

/**
 * Vista pública de disponibilidad de reservas (sin usuarioId, nombreRelator ni actividad).
 */
public record DisponibilidadDto(
        Long salaId,
        LocalDate fecha,
        Integer bloqueInicio,
        Integer bloqueFin
) {
    public static DisponibilidadDto desde(Reserva r) {
        return new DisponibilidadDto(r.getSalaId(), r.getFecha(), r.getBloqueInicio(), r.getBloqueFin());
    }
}
