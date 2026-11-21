package cl.duoc.reservasalas.reservas.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de PUT /api/v1/horario. Las horas llegan como "HH:mm" y los días como
 * texto ("MONDAY"). Las reglas entre campos (horas exactas, rango 08:00–21:00 y
 * apertura antes del cierre) las revisa HorarioGeneralService.
 */
public record ActualizarHorarioRequest(
        @NotNull(message = "La hora de apertura es obligatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime apertura,

        @NotNull(message = "La hora de cierre es obligatoria")
        @JsonFormat(pattern = "HH:mm")
        LocalTime cierre,

        @NotNull(message = "La pausa entre bloques es obligatoria")
        @Min(value = 0, message = "La pausa entre bloques debe estar entre 0 y 30 minutos")
        @Max(value = 30, message = "La pausa entre bloques debe estar entre 0 y 30 minutos")
        Integer pausaMinutos,

        @NotEmpty(message = "Debes indicar al menos un día de atención")
        List<@NotNull(message = "Los días no pueden venir vacíos") DayOfWeek> dias
) {
}
