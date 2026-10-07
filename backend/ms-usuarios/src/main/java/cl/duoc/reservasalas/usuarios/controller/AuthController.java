package cl.duoc.reservasalas.usuarios.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.reservasalas.usuarios.dto.LoginRequest;
import cl.duoc.reservasalas.usuarios.dto.LoginResponse;
import cl.duoc.reservasalas.usuarios.service.AuthService;
import jakarta.validation.Valid;

/**
 * Recibe las peticiones de /auth. Solo traduce HTTP ↔ servicio; la lógica está en AuthService.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // @Valid revisa las anotaciones de LoginRequest; si fallan, no se llega a login().
    // Si todo sale bien responde 200 (el código por defecto).
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
