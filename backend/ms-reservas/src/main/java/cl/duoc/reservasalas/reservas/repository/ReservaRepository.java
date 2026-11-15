package cl.duoc.reservasalas.reservas.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.reservasalas.reservas.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("SELECT r FROM Reserva r WHERE r.fecha = :fecha AND (:salaId IS NULL OR r.salaId = :salaId) AND r.estado = 'ACTIVA' ORDER BY r.bloqueInicio ASC")
    List<Reserva> buscarActivasPorFechaYSala(
            @Param("fecha") LocalDate fecha,
            @Param("salaId") Long salaId
    );

    @Query("SELECT r FROM Reserva r WHERE r.usuarioId = :usuarioId AND (:fecha IS NULL OR r.fecha = :fecha) AND r.estado = 'ACTIVA' ORDER BY r.fecha ASC, r.bloqueInicio ASC")
    List<Reserva> buscarMiasActivas(
            @Param("usuarioId") Long usuarioId,
            @Param("fecha") LocalDate fecha
    );

    @Query("SELECT r FROM Reserva r WHERE r.salaId = :salaId AND r.fecha = :fecha AND r.estado = 'ACTIVA' AND (:bloqueInicio <= r.bloqueFin AND :bloqueFin >= r.bloqueInicio)")
    List<Reserva> buscarCrucesSala(
            @Param("salaId") Long salaId,
            @Param("fecha") LocalDate fecha,
            @Param("bloqueInicio") Integer bloqueInicio,
            @Param("bloqueFin") Integer bloqueFin
    );

    @Query("SELECT r FROM Reserva r WHERE r.usuarioId = :usuarioId AND r.fecha = :fecha AND r.estado = 'ACTIVA' AND (:bloqueInicio <= r.bloqueFin AND :bloqueFin >= r.bloqueInicio)")
    List<Reserva> buscarCrucesUsuario(
            @Param("usuarioId") Long usuarioId,
            @Param("fecha") LocalDate fecha,
            @Param("bloqueInicio") Integer bloqueInicio,
            @Param("bloqueFin") Integer bloqueFin
    );

    @Modifying
    @Query(value = "INSERT INTO reserva (id, fecha, salaId, bloqueInicio, bloqueFin, usuarioId, nombreRelator, actividad, estado) "
            + "VALUES (:id, :fecha, :salaId, :bloqueInicio, :bloqueFin, :usuarioId, :nombreRelator, :actividad, :estado)", nativeQuery = true)
    void insertarConId(
            @Param("id") Long id,
            @Param("fecha") LocalDate fecha,
            @Param("salaId") Long salaId,
            @Param("bloqueInicio") Integer bloqueInicio,
            @Param("bloqueFin") Integer bloqueFin,
            @Param("usuarioId") Long usuarioId,
            @Param("nombreRelator") String nombreRelator,
            @Param("actividad") String actividad,
            @Param("estado") String estado
    );
}
