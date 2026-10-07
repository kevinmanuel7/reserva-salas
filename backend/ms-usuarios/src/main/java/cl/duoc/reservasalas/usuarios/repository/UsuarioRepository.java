package cl.duoc.reservasalas.usuarios.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.reservasalas.usuarios.model.Usuario;

/**
 * Acceso a la tabla usuario. Spring Data JPA genera la implementación.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring arma la consulta a partir del nombre: SELECT ... WHERE email = ?
    Optional<Usuario> findByEmail(String email);

    // Inserta un usuario con un id elegido (solo para cargar el dataset, cuyos id
    // usan las reservas). save() no sirve aquí: con IDENTITY el id lo pone MySQL.
    @Modifying
    @Query(value = "INSERT INTO usuario (id, nombre, email, rol, password) "
            + "VALUES (:id, :nombre, :email, :rol, :password)", nativeQuery = true)
    void insertarConId(@Param("id") Long id,
                       @Param("nombre") String nombre,
                       @Param("email") String email,
                       @Param("rol") String rol,
                       @Param("password") String password);
}
