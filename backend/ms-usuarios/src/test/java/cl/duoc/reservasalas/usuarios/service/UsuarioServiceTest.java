package cl.duoc.reservasalas.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import cl.duoc.reservasalas.usuarios.dto.ActualizarPerfilRequest;
import cl.duoc.reservasalas.usuarios.dto.CambiarPasswordRequest;
import cl.duoc.reservasalas.usuarios.dto.UsuarioDto;
import cl.duoc.reservasalas.usuarios.exception.ContrasenaIncorrectaException;
import cl.duoc.reservasalas.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.reservasalas.usuarios.exception.DatosInvalidosException;
import cl.duoc.reservasalas.usuarios.exception.UsuarioNoEncontradoException;
import cl.duoc.reservasalas.usuarios.model.Rol;
import cl.duoc.reservasalas.usuarios.model.Usuario;
import cl.duoc.reservasalas.usuarios.repository.UsuarioRepository;

/**
 * Pruebas unitarias de UsuarioService (/usuarios/me, /me/password y /usuarios/{id}).
 * Igual que en AuthServiceTest, el repositorio y el PasswordEncoder son mocks y no se
 * levanta Spring ni MySQL.
 * Las fechas se calculan desde hoy para que las pruebas no venzan con el tiempo.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String EMAIL = "relator1@duocuc.test";
    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Santiago"));

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario relator;

    @BeforeEach
    void prepararUsuario() {
        relator = new Usuario("Relator Ficticio Uno", EMAIL, Rol.RELATOR, "$2a$10$hashFalso");
        relator.setId(3L);
    }

    // save() del mock devuelve el mismo usuario que recibe, como haría la base de datos.
    private void guardarDevuelveElMismoUsuario() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(llamada -> llamada.getArgument(0));
    }

    @Test
    void obtenerPerfilDevuelveLosDatosDelUsuarioDelToken() {
        relator.setFechaNacimiento(LocalDate.of(1990, 5, 20));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));

        UsuarioDto perfil = usuarioService.obtenerPerfil("3");

        assertEquals(3L, perfil.id());
        assertEquals("Relator Ficticio Uno", perfil.nombre());
        assertEquals(EMAIL, perfil.email());
        assertEquals(Rol.RELATOR, perfil.rol());
        assertEquals(LocalDate.of(1990, 5, 20), perfil.fechaNacimiento());
    }

    @Test
    void obtenerPerfilSinFechaDevuelveFechaNull() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));

        assertNull(usuarioService.obtenerPerfil("3").fechaNacimiento());
    }

    @Test
    void actualizarPerfilConFechaValidaLaGuardaYNoCambiaLoDemas() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        guardarDevuelveElMismoUsuario();

        UsuarioDto perfil = usuarioService.actualizarPerfil("3",
                new ActualizarPerfilRequest(LocalDate.of(1990, 5, 20), null, null));

        assertEquals(LocalDate.of(1990, 5, 20), perfil.fechaNacimiento());
        assertEquals("Relator Ficticio Uno", perfil.nombre());
        assertEquals(EMAIL, perfil.email());
        assertEquals(Rol.RELATOR, perfil.rol());
        verify(usuarioRepository).save(relator);
    }

    @Test
    void actualizarPerfilConFechaFuturaLanzaDatosInvalidos() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));

        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> usuarioService.actualizarPerfil("3", new ActualizarPerfilRequest(HOY.plusDays(1), null, null)));

        assertEquals("La fecha de nacimiento no puede ser futura", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarPerfilConMenorDe18LanzaDatosInvalidos() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        // Le falta un día para cumplir 18.
        LocalDate fecha = HOY.minusYears(18).plusDays(1);

        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> usuarioService.actualizarPerfil("3", new ActualizarPerfilRequest(fecha, null, null)));

        assertEquals("Debes tener al menos 18 años", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarPerfilQueCumple18HoyLaAcepta() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        guardarDevuelveElMismoUsuario();

        UsuarioDto perfil = usuarioService.actualizarPerfil("3",
                new ActualizarPerfilRequest(HOY.minusYears(18), null, null));

        assertEquals(HOY.minusYears(18), perfil.fechaNacimiento());
    }

    @Test
    void perfilDeUsuarioInexistenteLanzaCredencialesInvalidas() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> usuarioService.obtenerPerfil("99"));
    }

    // --- PUT /usuarios/me con telefono y area (Contrato 1 v10) ---

    @Test
    void actualizarPerfilParcialConservaLoGuardado() {
        relator.setFechaNacimiento(LocalDate.of(1990, 5, 20));
        relator.setTelefono("+56911111111");
        relator.setArea("Informática");
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        guardarDevuelveElMismoUsuario();

        // Solo llega el teléfono: la fecha y el área llegan null y no se tocan.
        UsuarioDto perfil = usuarioService.actualizarPerfil("3",
                new ActualizarPerfilRequest(null, "+56912345678", null));

        assertEquals("+56912345678", perfil.telefono());
        assertEquals(LocalDate.of(1990, 5, 20), perfil.fechaNacimiento());
        assertEquals("Informática", perfil.area());
    }

    @Test
    void actualizarPerfilConTelefonoInvalidoLanzaDatosInvalidos() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));

        // Le falta un dígito: "+569" + 7 dígitos.
        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> usuarioService.actualizarPerfil("3", new ActualizarPerfilRequest(null, "+5691234567", null)));

        assertEquals("El teléfono debe tener el formato +569 seguido de 8 dígitos", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarPerfilConAreaDemasiadoLargaLanzaDatosInvalidos() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        String area = "a".repeat(61);

        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> usuarioService.actualizarPerfil("3", new ActualizarPerfilRequest(null, null, area)));

        assertEquals("El área no puede tener más de 60 caracteres", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarPerfilConAreaEnBlancoLanzaDatosInvalidos() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));

        assertThrows(DatosInvalidosException.class,
                () -> usuarioService.actualizarPerfil("3", new ActualizarPerfilRequest(null, null, "   ")));
        verify(usuarioRepository, never()).save(any());
    }

    // --- PUT /usuarios/me/password (Contrato 1 v10) ---

    @Test
    void cambiarPasswordConActualIncorrectaLanzaContrasenaIncorrecta() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        when(passwordEncoder.matches("equivocada", "$2a$10$hashFalso")).thenReturn(false);

        assertThrows(ContrasenaIncorrectaException.class, () -> usuarioService.cambiarPassword("3",
                new CambiarPasswordRequest("equivocada", "nuevaSegura123")));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPasswordConNuevaCortaLanzaDatosInvalidos() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        when(passwordEncoder.matches("actual123", "$2a$10$hashFalso")).thenReturn(true);

        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> usuarioService.cambiarPassword("3", new CambiarPasswordRequest("actual123", "corta")));

        assertEquals("La contraseña nueva debe tener al menos 8 caracteres", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPasswordConNuevaIgualALaActualLanzaDatosInvalidos() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        when(passwordEncoder.matches("actual123", "$2a$10$hashFalso")).thenReturn(true);

        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> usuarioService.cambiarPassword("3", new CambiarPasswordRequest("actual123", "actual123")));

        assertEquals("La contraseña nueva debe ser distinta de la actual", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPasswordCorrectoGuardaElHashNuevo() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        when(passwordEncoder.matches("actual123", "$2a$10$hashFalso")).thenReturn(true);
        when(passwordEncoder.encode("nuevaSegura123")).thenReturn("$2a$10$hashNuevo");

        usuarioService.cambiarPassword("3", new CambiarPasswordRequest("actual123", "nuevaSegura123"));

        // Se guarda el hash, nunca la contraseña tal cual.
        assertEquals("$2a$10$hashNuevo", relator.getPassword());
        verify(usuarioRepository).save(relator);
    }

    // --- GET /usuarios/{id} (Contrato 1 v10) ---

    @Test
    void obtenerPorIdDevuelveElUsuario() {
        relator.setTelefono("+56912345678");
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));

        UsuarioDto usuario = usuarioService.obtenerPorId(3L);

        assertEquals(3L, usuario.id());
        assertEquals("+56912345678", usuario.telefono());
    }

    @Test
    void obtenerPorIdInexistenteLanzaUsuarioNoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEncontradoException.class, () -> usuarioService.obtenerPorId(99L));
    }
}
