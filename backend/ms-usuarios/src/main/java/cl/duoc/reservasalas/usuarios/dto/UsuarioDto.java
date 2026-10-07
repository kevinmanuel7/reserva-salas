package cl.duoc.reservasalas.usuarios.dto;

import cl.duoc.reservasalas.usuarios.model.Rol;
import cl.duoc.reservasalas.usuarios.model.Usuario;

/**
 * Datos públicos de un usuario. No tiene password: la contraseña nunca sale del backend.
 */
public record UsuarioDto(Long id, String nombre, String email, Rol rol) {

    public static UsuarioDto desde(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}
