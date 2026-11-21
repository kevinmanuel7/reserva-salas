package cl.duoc.reservasalas.salas.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * Entidad Sala según la sección 4 de docs/plan-proyecto.md.
 */
@Entity
@Table(name = "sala")
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSala tipo;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    private Boolean tieneProyector;

    @Column(nullable = false)
    private Boolean tieneComputadores;

    @Column(nullable = false)
    private Boolean tieneClimatizacion;

    @Column(nullable = false)
    private Boolean activa = true;

    // Campos de la versión 5 del Contrato 1. Son opcionales.
    private String ubicacion;

    // null = la sala usa el horario general de bloques.
    private LocalTime horaApertura;

    private LocalTime horaCierre;

    // Los equipos se guardan en la tabla sala_equipo, unidos a la sala por salaId.
    @ElementCollection
    @CollectionTable(name = "sala_equipo", joinColumns = @JoinColumn(name = "salaId"))
    private List<Equipo> equipos = new ArrayList<>();

    public Sala() {
    }

    public Sala(String codigo, String nombre, TipoSala tipo, Integer capacidad,
                Boolean tieneProyector, Boolean tieneComputadores, Boolean tieneClimatizacion, Boolean activa) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tieneProyector = tieneProyector;
        this.tieneComputadores = tieneComputadores;
        this.tieneClimatizacion = tieneClimatizacion;
        this.activa = activa != null ? activa : true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoSala getTipo() {
        return tipo;
    }

    public void setTipo(TipoSala tipo) {
        this.tipo = tipo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Boolean getTieneProyector() {
        return tieneProyector;
    }

    public void setTieneProyector(Boolean tieneProyector) {
        this.tieneProyector = tieneProyector;
    }

    public Boolean getTieneComputadores() {
        return tieneComputadores;
    }

    public void setTieneComputadores(Boolean tieneComputadores) {
        this.tieneComputadores = tieneComputadores;
    }

    public Boolean getTieneClimatizacion() {
        return tieneClimatizacion;
    }

    public void setTieneClimatizacion(Boolean tieneClimatizacion) {
        this.tieneClimatizacion = tieneClimatizacion;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public List<Equipo> getEquipos() {
        return equipos;
    }

    public void setEquipos(List<Equipo> equipos) {
        this.equipos = equipos;
    }
}
