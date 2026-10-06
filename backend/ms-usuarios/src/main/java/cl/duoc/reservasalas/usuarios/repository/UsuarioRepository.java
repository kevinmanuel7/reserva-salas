package cl.duoc.reservasalas.usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.reservasalas.usuarios.model.Usuario;

/**
 * Acceso a la tabla usuario. Spring Data JPA genera la implementación.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring arma la consulta a partir del nombre: SELECT ... WHERE email = ?
    Optional<Usuario> findByEmail(String email);
}
