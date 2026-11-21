package cl.duoc.reservasalas.salas.dto;

import cl.duoc.reservasalas.salas.model.Equipo;
import cl.duoc.reservasalas.salas.model.EstadoEquipo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Equipo de una sala, tal como entra y sale por la API (Contrato 1, versión 5).
 */
public record EquipoDto(
        @NotBlank(message = "El nombre del equipo no puede estar vacío")
        String nombre,

        @NotNull(message = "El estado del equipo es obligatorio")
        EstadoEquipo estado,

        String comentario
) {
    public static EquipoDto desde(Equipo equipo) {
        return new EquipoDto(equipo.getNombre(), equipo.getEstado(), equipo.getComentario());
    }

    public Equipo aEquipo() {
        return new Equipo(nombre, estado, comentario);
    }
}
