package cl.duoc.reservasalas.reservas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.reservasalas.reservas.dto.ActualizarHorarioRequest;
import cl.duoc.reservasalas.reservas.dto.HorarioGeneralDto;
import cl.duoc.reservasalas.reservas.service.HorarioGeneralService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

/**
 * Endpoints del horario general (Contrato 1 versión 7, sección 5 de docs/plan-proyecto.md).
 */
@RestController
@RequestMapping("/api/v1/horario")
public class HorarioGeneralController {

    private final HorarioGeneralService horarioGeneralService;

    public HorarioGeneralController(HorarioGeneralService horarioGeneralService) {
        this.horarioGeneralService = horarioGeneralService;
    }

    @GetMapping
    public ResponseEntity<HorarioGeneralDto> obtenerHorario() {
        return ResponseEntity.ok(horarioGeneralService.obtener());
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping
    public ResponseEntity<HorarioGeneralDto> actualizarHorario(
            @Valid @RequestBody ActualizarHorarioRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(horarioGeneralService.actualizar(request, esCoordinador(authentication)));
    }

    // true si el token trae rol COORDINADOR (SecurityConfig lo convierte en ROLE_COORDINADOR).
    private boolean esCoordinador(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_COORDINADOR".equals(a.getAuthority()));
    }
}
