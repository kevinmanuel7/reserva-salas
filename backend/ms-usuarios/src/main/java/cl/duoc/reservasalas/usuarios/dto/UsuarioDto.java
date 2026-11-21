package cl.duoc.reservasalas.usuarios.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import cl.duoc.reservasalas.usuarios.model.Rol;
import cl.duoc.reservasalas.usuarios.model.Usuario;

/**
 * Datos públicos de un usuario. No tiene password: la contraseña nunca sale del backend.
 * fechaNacimiento (Contrato 1 v8), telefono y area (v10) van al final y pueden ser null.
 */
public record UsuarioDto(
        Long id,
        String nombre,
        String email,
        Rol rol,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaNacimiento,
        String telefono,
        String area) {

    public static UsuarioDto desde(Usuario usuario) {
        return new UsuarioDto(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol(),
                usuario.getFechaNacimiento(), usuario.getTelefono(), usuario.getArea());
    }
}
