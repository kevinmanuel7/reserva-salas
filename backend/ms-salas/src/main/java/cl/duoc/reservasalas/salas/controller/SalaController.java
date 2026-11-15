package cl.duoc.reservasalas.salas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.reservasalas.salas.dto.ActualizarSalaRequest;
import cl.duoc.reservasalas.salas.dto.CrearSalaRequest;
import cl.duoc.reservasalas.salas.dto.SalaDto;
import cl.duoc.reservasalas.salas.exception.SinPermisoException;
import cl.duoc.reservasalas.salas.exception.TokenInvalidoException;
import cl.duoc.reservasalas.salas.service.SalaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

/**
 * Endpoints del catálogo y administración de salas (Contrato 1, sección 5 de docs/plan-proyecto.md).
 */
@RestController
@RequestMapping("/api/v1/salas")
public class SalaController {

    private final SalaService salaService;

    public SalaController(SalaService salaService) {
        this.salaService = salaService;
    }

    @GetMapping
    public ResponseEntity<List<SalaDto>> listarSalas(
            @RequestParam(required = false) Integer capacidadMin,
            @RequestParam(required = false) Boolean proyector,
            @RequestParam(required = false) Boolean computadores,
            @RequestParam(required = false) Boolean climatizacion,
            @RequestParam(required = false, defaultValue = "false") boolean incluirInactivas,
            Authentication authentication) {

        if (incluirInactivas) {
            if (!estaAutenticado(authentication)) {
                throw new TokenInvalidoException("Debes iniciar sesión nuevamente");
            }
            boolean esCoordinador = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_COORDINADOR".equals(a.getAuthority()));
            if (!esCoordinador) {
                throw new SinPermisoException("No tienes permiso para realizar esta acción");
            }
        }

        List<SalaDto> salas = salaService.listarSalas(capacidadMin, proyector, computadores, climatizacion, incluirInactivas);
        return ResponseEntity.ok(salas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaDto> obtenerSalaPorId(@PathVariable Long id) {
        SalaDto sala = salaService.obtenerPorId(id);
        return ResponseEntity.ok(sala);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<SalaDto> crearSala(@Valid @RequestBody CrearSalaRequest request) {
        SalaDto nuevaSala = salaService.crearSala(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaSala);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<SalaDto> actualizarSala(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarSalaRequest request) {
        SalaDto salaActualizada = salaService.actualizarSala(id, request);
        return ResponseEntity.ok(salaActualizada);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<SalaDto> eliminarSala(@PathVariable Long id) {
        SalaDto salaEliminada = salaService.eliminarSala(id);
        return ResponseEntity.ok(salaEliminada);
    }

    private boolean estaAutenticado(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
