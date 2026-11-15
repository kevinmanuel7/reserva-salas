package cl.duoc.reservasalas.reservas.dto;

import java.time.LocalDate;

import cl.duoc.reservasalas.reservas.model.EstadoReserva;
import cl.duoc.reservasalas.reservas.model.Reserva;

public record ReservaDto(
        Long id,
        Long salaId,
        Long usuarioId,
        String nombreRelator,
        String actividad,
        LocalDate fecha,
        Integer bloqueInicio,
        Integer bloqueFin,
        EstadoReserva estado
) {
    public static ReservaDto desde(Reserva r) {
        return new ReservaDto(
                r.getId(),
                r.getSalaId(),
                r.getUsuarioId(),
                r.getNombreRelator(),
                r.getActividad(),
                r.getFecha(),
                r.getBloqueInicio(),
                r.getBloqueFin(),
                r.getEstado()
        );
    }
}
