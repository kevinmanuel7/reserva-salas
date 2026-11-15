package cl.duoc.reservasalas.salas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.reservasalas.salas.model.Sala;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    Optional<Sala> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    @Query("SELECT s FROM Sala s WHERE "
            + "(:incluirInactivas = true OR s.activa = true) AND "
            + "(:capacidadMin IS NULL OR s.capacidad >= :capacidadMin) AND "
            + "(:proyector IS NULL OR s.tieneProyector = :proyector) AND "
            + "(:computadores IS NULL OR s.tieneComputadores = :computadores) AND "
            + "(:climatizacion IS NULL OR s.tieneClimatizacion = :climatizacion)")
    List<Sala> buscarSalasConFiltros(
            @Param("capacidadMin") Integer capacidadMin,
            @Param("proyector") Boolean proyector,
            @Param("computadores") Boolean computadores,
            @Param("climatizacion") Boolean climatizacion,
            @Param("incluirInactivas") boolean incluirInactivas
    );

    @Modifying
    @Query(value = "INSERT INTO sala (id, codigo, nombre, tipo, capacidad, tieneProyector, tieneComputadores, tieneClimatizacion, activa) "
            + "VALUES (:id, :codigo, :nombre, :tipo, :capacidad, :tieneProyector, :tieneComputadores, :tieneClimatizacion, :activa)", nativeQuery = true)
    void insertarConId(@Param("id") Long id,
                       @Param("codigo") String codigo,
                       @Param("nombre") String nombre,
                       @Param("tipo") String tipo,
                       @Param("capacidad") Integer capacidad,
                       @Param("tieneProyector") Boolean tieneProyector,
                       @Param("tieneComputadores") Boolean tieneComputadores,
                       @Param("tieneClimatizacion") Boolean tieneClimatizacion,
                       @Param("activa") Boolean activa);
}
