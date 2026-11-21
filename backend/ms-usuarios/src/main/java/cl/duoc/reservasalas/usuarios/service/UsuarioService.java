package cl.duoc.reservasalas.usuarios.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import cl.duoc.reservasalas.usuarios.dto.ActualizarPerfilRequest;
import cl.duoc.reservasalas.usuarios.dto.UsuarioDto;
import cl.duoc.reservasalas.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.reservasalas.usuarios.exception.DatosInvalidosException;
import cl.duoc.reservasalas.usuarios.model.Usuario;
import cl.duoc.reservasalas.usuarios.repository.UsuarioRepository;

/**
 * Lógica del perfil del usuario que llama (GET y PUT /api/v1/usuarios/me).
 * El id siempre sale del token, así que nadie puede ver ni editar a otro usuario.
 */
@Service
public class UsuarioService {

    private static final ZoneId ZONA_CHILE = ZoneId.of("America/Santiago");
    private static final int EDAD_MINIMA = 18;

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioDto obtenerPerfil(String idUsuario) {
        return UsuarioDto.desde(buscar(idUsuario));
    }

    // Solo cambia fechaNacimiento: nombre, email y rol no se pueden modificar.
    public UsuarioDto actualizarPerfil(String idUsuario, ActualizarPerfilRequest request) {
        Usuario usuario = buscar(idUsuario);
        LocalDate fecha = request.fechaNacimiento();
        LocalDate hoy = LocalDate.now(ZONA_CHILE);

        if (fecha.isAfter(hoy)) {
            throw new DatosInvalidosException("La fecha de nacimiento no puede ser futura");
        }
        // Quien cumple 18 justo hoy nació en hoy.minusYears(18): ese día sí se acepta.
        if (fecha.isAfter(hoy.minusYears(EDAD_MINIMA))) {
            throw new DatosInvalidosException("Debes tener al menos 18 años");
        }

        usuario.setFechaNacimiento(fecha);
        return UsuarioDto.desde(usuarioRepository.save(usuario));
    }

    // Si el usuario del token ya no existe, se responde igual que en /auth/refresh (401).
    private Usuario buscar(String idUsuario) {
        return usuarioRepository.findById(Long.valueOf(idUsuario))
                .orElseThrow(CredencialesInvalidasException::new);
    }
}
