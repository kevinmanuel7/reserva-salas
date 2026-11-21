package cl.duoc.reservasalas.salas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Equipo de una sala (ejemplo: "Proyector"). No es una entidad propia: se guarda
 * como parte de la Sala, en la tabla sala_equipo (ver @ElementCollection en Sala).
 */
@Embeddable
public class Equipo {

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEquipo estado;

    private String comentario;

    public Equipo() {
    }

    public Equipo(String nombre, EstadoEquipo estado, String comentario) {
        this.nombre = nombre;
        this.estado = estado;
        this.comentario = comentario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EstadoEquipo getEstado() {
        return estado;
    }

    public void setEstado(EstadoEquipo estado) {
        this.estado = estado;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
