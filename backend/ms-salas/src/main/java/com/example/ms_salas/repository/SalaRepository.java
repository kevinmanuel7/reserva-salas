package com.example.ms_salas.repository;

import com.example.ms_salas.model.Sala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long> {

    Optional<Sala> findByCodigo(String codigo);

    // Consulta con filtros opcionales para listar salas
    @Query("SELECT s FROM Sala s WHERE " +
            "(:incluirInactivas = true OR s.activa = true) AND " +
            "(:capacidadMin IS NULL OR s.capacidad >= :capacidadMin) AND " +
            "(:proyector IS NULL OR s.tieneProyector = :proyector) AND " +
            "(:computadores IS NULL OR s.tieneComputadores = :computadores) AND " +
            "(:climatizacion IS NULL OR s.tieneClimatizacion = :climatizacion)")
    List<Sala> buscarSalasConFiltros(
            @Param("capacidadMin") Integer capacidadMin,
            @Param("proyector") Boolean proyector,
            @Param("computadores") Boolean computadores,
            @Param("climatizacion") Boolean climatizacion,
            @Param("incluirInactivas") boolean incluirInactivas
    );
}