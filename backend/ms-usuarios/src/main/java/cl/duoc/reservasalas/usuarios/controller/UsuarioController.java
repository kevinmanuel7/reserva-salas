package cl.duoc.reservasalas.usuarios.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.reservasalas.usuarios.dto.ActualizarPerfilRequest;
import cl.duoc.reservasalas.usuarios.dto.UsuarioDto;
import cl.duoc.reservasalas.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

/**
 * Recibe las peticiones de /api/v1/usuarios. Ambas rutas exigen token (SecurityConfig)
 * y trabajan sobre el usuario del token: "me" = yo mismo.
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
}
