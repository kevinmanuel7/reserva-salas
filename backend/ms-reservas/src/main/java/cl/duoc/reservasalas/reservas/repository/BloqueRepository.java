package cl.duoc.reservasalas.reservas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.reservasalas.reservas.model.Bloque;

public interface BloqueRepository extends JpaRepository<Bloque, Integer> {
    List<Bloque> findAllByOrderByNumeroAsc();
}
