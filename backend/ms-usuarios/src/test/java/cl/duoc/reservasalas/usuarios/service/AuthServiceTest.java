package cl.duoc.reservasalas.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import cl.duoc.reservasalas.usuarios.dto.LoginRequest;
import cl.duoc.reservasalas.usuarios.dto.LoginResponse;
import cl.duoc.reservasalas.usuarios.exception.CredencialesInvalidasException;
import cl.duoc.reservasalas.usuarios.model.Rol;
import cl.duoc.reservasalas.usuarios.model.Usuario;
import cl.duoc.reservasalas.usuarios.repository.UsuarioRepository;

/**
 * Pruebas unitarias de AuthService. No levantan Spring ni MySQL: el repositorio,
 * el codificador de contraseñas y el JwtService se reemplazan por "mocks"
 * (objetos falsos a los que se les dice qué responder).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "relator1@duocuc.test";
    private static final String PASSWORD = "clave-de-prueba";
    private static final String HASH = "$2a$10$hashFalsoSoloParaLaPrueba";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario relator;

    @BeforeEach
    void prepararUsuario() {
        relator = new Usuario("Relator Ficticio Uno", EMAIL, Rol.RELATOR, HASH);
        relator.setId(3L);
    }

    @Test
    void loginConCredencialesCorrectasDevuelveTokenYUsuario() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(relator));
        when(passwordEncoder.matches(PASSWORD, HASH)).thenReturn(true);
        when(jwtService.generarToken(relator)).thenReturn("token-de-prueba");

        LoginResponse respuesta = authService.login(new LoginRequest(EMAIL, PASSWORD));

        assertEquals("token-de-prueba", respuesta.token());
        assertEquals(3L, respuesta.usuario().id());
        assertEquals("Relator Ficticio Uno", respuesta.usuario().nombre());
        assertEquals(EMAIL, respuesta.usuario().email());
        assertEquals(Rol.RELATOR, respuesta.usuario().rol());
    }

    @Test
    void loginConEmailInexistenteLanzaCredencialesInvalidas() {
        when(usuarioRepository.findByEmail("nadie@duocuc.test")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(new LoginRequest("nadie@duocuc.test", PASSWORD)));
        verify(jwtService, never()).generarToken(any());
    }

    @Test
    void loginConPasswordIncorrectaLanzaCredencialesInvalidas() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(relator));
        when(passwordEncoder.matches("incorrecta", HASH)).thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.login(new LoginRequest(EMAIL, "incorrecta")));
        verify(jwtService, never()).generarToken(any());
    }
}
