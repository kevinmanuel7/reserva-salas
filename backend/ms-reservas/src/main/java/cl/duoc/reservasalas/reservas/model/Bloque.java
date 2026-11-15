package cl.duoc.reservasalas.reservas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad Bloque según la sección 4 de docs/plan-proyecto.md.
 * 13 bloques fijos de 1 hora.
 */
@Entity
@Table(name = "bloque")
public class Bloque {

    @Id
    private Integer numero;

    @Column(nullable = false, length = 5)
    private String horaInicio;

    @Column(nullable = false, length = 5)
    private String horaFin;

    public Bloque() {
    }

    public Bloque(Integer numero, String horaInicio, String horaFin) {
        this.numero = numero;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }
}
