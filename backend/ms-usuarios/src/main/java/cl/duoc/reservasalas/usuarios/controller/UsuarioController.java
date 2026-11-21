package cl.duoc.reservasalas.usuarios.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.reservasalas.usuarios.dto.ActualizarPerfilRequest;
import cl.duoc.reservasalas.usuarios.dto.CambiarPasswordRequest;
import cl.duoc.reservasalas.usuarios.dto.UsuarioDto;
import cl.duoc.reservasalas.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

/**
 * Recibe las peticiones de /api/v1/usuarios. Todas exigen token (SecurityConfig).
 * Las rutas "me" (= yo mismo) trabajan sobre el usuario del token; GET /{id} es solo
 * para el coordinador.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // jwt.getSubject() es el claim "sub": el id del usuario como texto.
    @GetMapping("/me")
    public UsuarioDto obtenerPerfil(@AuthenticationPrincipal Jwt jwt) {
        return usuarioService.obtenerPerfil(jwt.getSubject());
    }

    @PutMapping("/me")
    public UsuarioDto actualizarPerfil(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody ActualizarPerfilRequest request) {
        return usuarioService.actualizarPerfil(jwt.getSubject(), request);
    }

    // 204 No Content: el cambio salió bien y la respuesta no lleva cuerpo.
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarPassword(@AuthenticationPrincipal Jwt jwt,
                                @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(jwt.getSubject(), request);
    }

    // Solo coordinador (lo revisa SecurityConfig antes de llegar aquí; si no → 403).
    @GetMapping("/{id}")
    public UsuarioDto obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }
}
