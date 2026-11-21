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

import cl.duoc.reservasalas.usuarios.dto.ActualizarPerfilRequest;
import cl.duoc.reservasalas.usuarios.dto.UsuarioDto;
import cl.duoc.reservasalas.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.reservasalas.usuarios.exception.DatosInvalidosException;
import cl.duoc.reservasalas.usuarios.model.Rol;
import cl.duoc.reservasalas.usuarios.model.Usuario;
import cl.duoc.reservasalas.usuarios.repository.UsuarioRepository;

/**
 * Pruebas unitarias de UsuarioService (GET y PUT /usuarios/me). Igual que en
 * AuthServiceTest, el repositorio es un mock y no se levanta Spring ni MySQL.
 * Las fechas se calculan desde hoy para que las pruebas no venzan con el tiempo.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String EMAIL = "relator1@duocuc.test";
    private static final LocalDate HOY = LocalDate.now(ZoneId.of("America/Santiago"));

    @Mock
    private UsuarioRepository usuarioRepository;

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
                new ActualizarPerfilRequest(LocalDate.of(1990, 5, 20)));

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
                () -> usuarioService.actualizarPerfil("3", new ActualizarPerfilRequest(HOY.plusDays(1))));

        assertEquals("La fecha de nacimiento no puede ser futura", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarPerfilConMenorDe18LanzaDatosInvalidos() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        // Le falta un día para cumplir 18.
        LocalDate fecha = HOY.minusYears(18).plusDays(1);

        DatosInvalidosException error = assertThrows(DatosInvalidosException.class,
                () -> usuarioService.actualizarPerfil("3", new ActualizarPerfilRequest(fecha)));

        assertEquals("Debes tener al menos 18 años", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizarPerfilQueCumple18HoyLaAcepta() {
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(relator));
        guardarDevuelveElMismoUsuario();

        UsuarioDto perfil = usuarioService.actualizarPerfil("3",
                new ActualizarPerfilRequest(HOY.minusYears(18)));

        assertEquals(HOY.minusYears(18), perfil.fechaNacimiento());
    }

    @Test
    void perfilDeUsuarioInexistenteLanzaCredencialesInvalidas() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> usuarioService.obtenerPerfil("99"));
    }
}
