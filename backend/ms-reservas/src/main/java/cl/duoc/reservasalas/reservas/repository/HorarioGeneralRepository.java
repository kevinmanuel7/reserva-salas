package cl.duoc.reservasalas.reservas.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.reservasalas.reservas.model.HorarioGeneral;

public interface HorarioGeneralRepository extends JpaRepository<HorarioGeneral, Long> {

    // La tabla tiene una sola fila; se toma la primera por si acaso.
    Optional<HorarioGeneral> findFirstByOrderByIdAsc();
}
