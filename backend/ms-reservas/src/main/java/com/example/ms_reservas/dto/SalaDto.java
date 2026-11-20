package cl.duoc.reservasalas.reservas.dto;

import lombok.Data;

@Data
public class SalaDto {
    private Long id;
    private String codigo;
    private String nombre;
    private String tipo;
    private Integer capacidad;
    private Boolean tieneProyector;
    private Boolean tieneComputadores;
    private Boolean tieneClimatizacion;
    private Boolean activa;
}