package cl.duoc.reservasalas.usuarios.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import cl.duoc.reservasalas.usuarios.dto.ActualizarPerfilRequest;
import cl.duoc.reservasalas.usuarios.dto.CambiarPasswordRequest;
import cl.duoc.reservasalas.usuarios.dto.UsuarioDto;
import cl.duoc.reservasalas.usuarios.exception.ContrasenaIncorrectaException;
import cl.duoc.reservasalas.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.reservasalas.usuarios.exception.DatosInvalidosException;
import cl.duoc.reservasalas.usuarios.exception.UsuarioNoEncontradoException;
import cl.duoc.reservasalas.usuarios.model.Usuario;
import cl.duoc.reservasalas.usuarios.repository.UsuarioRepository;

/**
 * Lógica del perfil del usuario que llama (/api/v1/usuarios/me y /me/password) y de
 * la consulta de un usuario por id (solo coordinador, lo controla SecurityConfig).
 * En las rutas "me" el id sale del token, así que nadie puede editar a otro usuario.
 */
@Service
public class UsuarioService {

    private static final ZoneId ZONA_CHILE = ZoneId.of("America/Santiago");
    private static final int EDAD_MINIMA = 18;
    // Celular chileno: "+569" seguido de exactamente 8 dígitos (ejemplo: +56912345678).
    private static final Pattern TELEFONO_CHILE = Pattern.compile("^\\+569\\d{8}$");
    private static final int LARGO_MAXIMO_AREA = 60;
    private static final int LARGO_MINIMO_PASSWORD = 8;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioDto obtenerPerfil(String idUsuario) {
        return UsuarioDto.desde(buscar(idUsuario));
    }

    public UsuarioDto obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioDto::desde)
                .orElseThrow(UsuarioNoEncontradoException::new);
    }

    // Cambia fechaNacimiento, telefono y area. El campo que llega null conserva lo guardado.
    // Primero se validan todos y después se asignan: si uno falla, no se cambia ninguno.
    // Nombre, email y rol no se pueden modificar.
    public UsuarioDto actualizarPerfil(String idUsuario, ActualizarPerfilRequest request) {
        Usuario usuario = buscar(idUsuario);

        if (request.fechaNacimiento() != null) {
            validarFechaNacimiento(request.fechaNacimiento());
        }
        if (request.telefono() != null && !TELEFONO_CHILE.matcher(request.telefono()).matches()) {
            throw new DatosInvalidosException("El teléfono debe tener el formato +569 seguido de 8 dígitos");
        }
        if (request.area() != null) {
            validarArea(request.area());
        }

        if (request.fechaNacimiento() != null) {
            usuario.setFechaNacimiento(request.fechaNacimiento());
        }
        if (request.telefono() != null) {
            usuario.setTelefono(request.telefono());
        }
        if (request.area() != null) {
            usuario.setArea(request.area());
        }
        return UsuarioDto.desde(usuarioRepository.save(usuario));
    }

    public void cambiarPassword(String idUsuario, CambiarPasswordRequest request) {
        Usuario usuario = buscar(idUsuario);

        // matches() aplica BCrypt a lo escrito y lo compara con el hash guardado.
        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new ContrasenaIncorrectaException();
        }
        if (request.passwordNueva().length() < LARGO_MINIMO_PASSWORD) {
            throw new DatosInvalidosException("La contraseña nueva debe tener al menos 8 caracteres");
        }
        // La actual ya se comprobó arriba, así que basta compararla con la nueva como texto.
        if (request.passwordNueva().equals(request.passwordActual())) {
            throw new DatosInvalidosException("La contraseña nueva debe ser distinta de la actual");
        }

        usuario.setPassword(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }

    private void validarFechaNacimiento(LocalDate fecha) {
        LocalDate hoy = LocalDate.now(ZONA_CHILE);
        if (fecha.isAfter(hoy)) {
            throw new DatosInvalidosException("La fecha de nacimiento no puede ser futura");
        }
        // Quien cumple 18 justo hoy nació en hoy.minusYears(18): ese día sí se acepta.
        if (fecha.isAfter(hoy.minusYears(EDAD_MINIMA))) {
            throw new DatosInvalidosException("Debes tener al menos 18 años");
        }
    }

    // isBlank() también rechaza un área hecha solo de espacios.
    private void validarArea(String area) {
        if (area.isBlank()) {
            throw new DatosInvalidosException("El área no puede estar vacía");
        }
        if (area.length() > LARGO_MAXIMO_AREA) {
            throw new DatosInvalidosException("El área no puede tener más de 60 caracteres");
        }
    }

    // Si el usuario del token ya no existe, se responde igual que en /auth/refresh (401).
    private Usuario buscar(String idUsuario) {
        return usuarioRepository.findById(Long.valueOf(idUsuario))
                .orElseThrow(CredencialesInvalidasException::new);
    }
}
