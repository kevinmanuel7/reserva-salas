package cl.duoc.reservasalas.reservas.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * Horario general de la institución (Contrato 1 versión 7, sección 4 de docs/plan-proyecto.md).
 * Es una configuración de una sola fila que edita el coordinador.
 * Los días se guardan en la tabla horario_general_dia, unida por horarioId.
 */
@Entity
@Table(name = "horario_general")
public class HorarioGeneral {

    // Valores por defecto: se usan al arrancar si la tabla está vacía.
    public static final LocalTime APERTURA_POR_DEFECTO = LocalTime.of(8, 0);
    public static final LocalTime CIERRE_POR_DEFECTO = LocalTime.of(21, 0);
    public static final int PAUSA_MINUTOS_POR_DEFECTO = 5;
    public static final List<DayOfWeek> DIAS_POR_DEFECTO = List.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalTime apertura;

    @Column(nullable = false)
    private LocalTime cierre;

    @Column(nullable = false)
    private Integer pausaMinutos;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "horario_general_dia", joinColumns = @JoinColumn(name = "horarioId"))
    @Column(name = "dia", nullable = false)
    private List<DayOfWeek> dias = new ArrayList<>();

    public HorarioGeneral() {
    }

    public HorarioGeneral(LocalTime apertura, LocalTime cierre, Integer pausaMinutos, List<DayOfWeek> dias) {
        this.apertura = apertura;
        this.cierre = cierre;
        this.pausaMinutos = pausaMinutos;
        this.dias = new ArrayList<>(dias);
    }

    public static HorarioGeneral porDefecto() {
        return new HorarioGeneral(APERTURA_POR_DEFECTO, CIERRE_POR_DEFECTO,
                PAUSA_MINUTOS_POR_DEFECTO, DIAS_POR_DEFECTO);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalTime getApertura() {
        return apertura;
    }

    public void setApertura(LocalTime apertura) {
        this.apertura = apertura;
    }

    public LocalTime getCierre() {
        return cierre;
    }

    public void setCierre(LocalTime cierre) {
        this.cierre = cierre;
    }

    public Integer getPausaMinutos() {
        return pausaMinutos;
    }

    public void setPausaMinutos(Integer pausaMinutos) {
        this.pausaMinutos = pausaMinutos;
    }

    public List<DayOfWeek> getDias() {
        return dias;
    }

    public void setDias(List<DayOfWeek> dias) {
        this.dias = new ArrayList<>(dias);
    }
}
