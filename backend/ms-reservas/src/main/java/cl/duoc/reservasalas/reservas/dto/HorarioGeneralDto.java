package cl.duoc.reservasalas.reservas.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import cl.duoc.reservasalas.reservas.model.HorarioGeneral;

public record HorarioGeneralDto(
        @JsonFormat(pattern = "HH:mm")
        LocalTime apertura,
        @JsonFormat(pattern = "HH:mm")
        LocalTime cierre,
        Integer pausaMinutos,
        List<DayOfWeek> dias
) {
    public static HorarioGeneralDto desde(HorarioGeneral h) {
        return new HorarioGeneralDto(
                h.getApertura(),
                h.getCierre(),
                h.getPausaMinutos(),
                List.copyOf(h.getDias())
        );
    }
}
