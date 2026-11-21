package cl.duoc.reservasalas.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.duoc.reservasalas.usuarios.config.SecurityConfig;
import cl.duoc.reservasalas.usuarios.dto.CambiarPasswordRequest;
import cl.duoc.reservasalas.usuarios.dto.UsuarioDto;
import cl.duoc.reservasalas.usuarios.exception.UsuarioNoEncontradoException;
import cl.duoc.reservasalas.usuarios.model.Rol;
import cl.duoc.reservasalas.usuarios.service.UsuarioService;

/**
 * Pruebas de las rutas de /api/v1/usuarios pasando por la seguridad real (SecurityConfig).
 * @WebMvcTest levanta solo la capa web: sin MySQL y con UsuarioService como mock.
 * El JwtDecoder también es un mock: según el texto del token devuelve un Jwt con rol
 * COORDINADOR o RELATOR, y así se prueba la conversión real del claim "rol".
 */
@WebMvcTest(controllers = UsuarioController.class, properties = "cors.origins=http://localhost:5173")
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    private static final String TOKEN_COORDINADOR = "token-coordinador";
    private static final String TOKEN_RELATOR = "token-relator";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void prepararTokens() {
        when(jwtDecoder.decode(TOKEN_COORDINADOR)).thenReturn(jwt(TOKEN_COORDINADOR, "1", Rol.COORDINADOR));
        when(jwtDecoder.decode(TOKEN_RELATOR)).thenReturn(jwt(TOKEN_RELATOR, "3", Rol.RELATOR));
    }

    private static Jwt jwt(String token, String id, Rol rol) {
        return Jwt.withTokenValue(token)
                .header("alg", "HS256")
                .subject(id)
                .claim("rol", rol.name())
                .build();
    }

    private static UsuarioDto relatorUno() {
        return new UsuarioDto(3L, "Relator Ficticio Uno", "relator1@duocuc.test", Rol.RELATOR,
                null, "+56912345678", "Informática");
    }

    @Test
    void obtenerPorIdComoCoordinadorResponde200() throws Exception {
        when(usuarioService.obtenerPorId(3L)).thenReturn(relatorUno());

        mockMvc.perform(get("/api/v1/usuarios/3").header("Authorization", "Bearer " + TOKEN_COORDINADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.telefono").value("+56912345678"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void obtenerPorIdComoRelatorResponde403() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/3").header("Authorization", "Bearer " + TOKEN_RELATOR))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
    }

    @Test
    void obtenerPorIdInexistenteResponde404() throws Exception {
        when(usuarioService.obtenerPorId(99L)).thenThrow(new UsuarioNoEncontradoException());

        mockMvc.perform(get("/api/v1/usuarios/99").header("Authorization", "Bearer " + TOKEN_COORDINADOR))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("USUARIO_NO_ENCONTRADO"));
    }

    // La regla de GET /usuarios/{id} no debe bloquear GET /usuarios/me a un relator.
    @Test
    void obtenerPerfilComoRelatorSigueRespondiendo200() throws Exception {
        when(usuarioService.obtenerPerfil("3")).thenReturn(relatorUno());

        mockMvc.perform(get("/api/v1/usuarios/me").header("Authorization", "Bearer " + TOKEN_RELATOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("relator1@duocuc.test"));
    }

    @Test
    void cambiarPasswordCorrectoResponde204SinCuerpo() throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/me/password")
                        .header("Authorization", "Bearer " + TOKEN_RELATOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"passwordActual\":\"actual123\",\"passwordNueva\":\"nuevaSegura123\"}"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(usuarioService).cambiarPassword(eq("3"), any(CambiarPasswordRequest.class));
    }
}
