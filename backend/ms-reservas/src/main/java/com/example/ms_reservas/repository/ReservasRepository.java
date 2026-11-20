package com.example.ms_reservas.repository;

import com.example.ms_reservas.model.Reservas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservasRepository extends JpaRepository<Reservas, Long> {

    // 1. Buscar reservas activas por fecha
    List<Reservas> findByFechaAndEstado(String fecha, String estado);

    // 2. Buscar reservas activas por fecha y sala específica
    List<Reservas> findByFechaAndSalaIdAndEstado(String fecha, Long salaId, String estado);

    // 3. Buscar reservas activas de un usuario específico
    List<Reservas> findByUsuarioIdAndEstado(Long usuarioId, String estado);
    List<Reservas> findByUsuarioIdAndFechaAndEstado(Long usuarioId, String fecha, String estado);

    // 4. Consulta para detectar cruces de horario
    @Query("SELECT r FROM Reservas r WHERE r.salaId = :salaId AND r.fecha = :fecha AND r.estado = 'ACTIVA' " +
            "AND :bloqueInicio <= r.bloqueFin AND :bloqueFin >= r.bloqueInicio")
    List<Reservas> encontrarCruces(
            @Param("salaId") Long salaId,
            @Param("fecha") String fecha,
            @Param("bloqueInicio") Integer bloqueInicio,
            @Param("bloqueFin") Integer bloqueFin
    );
}