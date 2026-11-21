package cl.duoc.reservasalas.reservas.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad Reserva según la sección 4 de docs/plan-proyecto.md.
 */
@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long salaId;

    @Column(nullable = false)
    private Long usuarioId;

    @Column(nullable = false)
    private String nombreRelator;

    @Column(nullable = false)
    private String actividad;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Integer bloqueInicio;

    @Column(nullable = false)
    private Integer bloqueFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.ACTIVA;

    // Campos de la versión 6 del Contrato 1. Son opcionales: null hasta que alguien
    // los marque. La reserva sigue ACTIVA, para que la sala no se libere.
    private LocalTime horaCheckIn;

    private Boolean verificada;

    private Boolean noPresentado;

    public Reserva() {
    }

    public Reserva(Long salaId, Long usuarioId, String nombreRelator, String actividad,
                   LocalDate fecha, Integer bloqueInicio, Integer bloqueFin, EstadoReserva estado) {
        this.salaId = salaId;
        this.usuarioId = usuarioId;
        this.nombreRelator = nombreRelator;
        this.actividad = actividad;
        this.fecha = fecha;
        this.bloqueInicio = bloqueInicio;
        this.bloqueFin = bloqueFin;
        this.estado = estado != null ? estado : EstadoReserva.ACTIVA;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSalaId() {
        return salaId;
    }

    public void setSalaId(Long salaId) {
        this.salaId = salaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombreRelator() {
        return nombreRelator;
    }

    public void setNombreRelator(String nombreRelator) {
        this.nombreRelator = nombreRelator;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Integer getBloqueInicio() {
        return bloqueInicio;
    }

    public void setBloqueInicio(Integer bloqueInicio) {
        this.bloqueInicio = bloqueInicio;
    }

    public Integer getBloqueFin() {
        return bloqueFin;
    }

    public void setBloqueFin(Integer bloqueFin) {
        this.bloqueFin = bloqueFin;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public LocalTime getHoraCheckIn() {
        return horaCheckIn;
    }

    public void setHoraCheckIn(LocalTime horaCheckIn) {
        this.horaCheckIn = horaCheckIn;
    }

    public Boolean getVerificada() {
        return verificada;
    }

    public void setVerificada(Boolean verificada) {
        this.verificada = verificada;
    }

    public Boolean getNoPresentado() {
        return noPresentado;
    }

    public void setNoPresentado(Boolean noPresentado) {
        this.noPresentado = noPresentado;
    }
}
