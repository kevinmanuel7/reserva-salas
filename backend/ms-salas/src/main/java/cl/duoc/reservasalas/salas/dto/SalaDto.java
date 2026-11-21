package cl.duoc.reservasalas.salas.dto;

import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import cl.duoc.reservasalas.salas.model.Sala;
import cl.duoc.reservasalas.salas.model.TipoSala;

/**
 * Objeto de transferencia de datos para Sala.
 * Cumple con el modelo de datos de la sección 4 y Contrato 1 de docs/plan-proyecto.md.
 * ubicacion, horaApertura, horaCierre y equipos son de la versión 5 del contrato.
 */
public record SalaDto(
        Long id,
        String codigo,
        String nombre,
        TipoSala tipo,
        Integer capacidad,
        Boolean tieneProyector,
        Boolean tieneComputadores,
        Boolean tieneClimatizacion,
        Boolean activa,
        String ubicacion,
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaApertura,
        @JsonFormat(pattern = "HH:mm")
        LocalTime horaCierre,
        List<EquipoDto> equipos
) {
    public static SalaDto desde(Sala sala) {
        return new SalaDto(
                sala.getId(),
                sala.getCodigo(),
                sala.getNombre(),
                sala.getTipo(),
                sala.getCapacidad(),
                sala.getTieneProyector(),
                sala.getTieneComputadores(),
                sala.getTieneClimatizacion(),
                sala.getActiva(),
                sala.getUbicacion(),
                sala.getHoraApertura(),
                sala.getHoraCierre(),
                sala.getEquipos().stream().map(EquipoDto::desde).toList()
        );
    }
}
