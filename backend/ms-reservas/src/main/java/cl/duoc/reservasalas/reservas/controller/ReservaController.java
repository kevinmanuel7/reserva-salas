package cl.duoc.reservasalas.reservas.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.reservasalas.reservas.dto.CrearReservaRequest;
import cl.duoc.reservasalas.reservas.dto.DisponibilidadDto;
import cl.duoc.reservasalas.reservas.dto.ReservaDto;
import cl.duoc.reservasalas.reservas.service.ReservaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

/**
 * Endpoints de reservas de espacios (Contrato 1, sección 5 de docs/plan-proyecto.md).
 */
@RestController
@RequestMapping("/api/v1/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<ReservaDto>> listarReservas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long salaId) {
        return ResponseEntity.ok(reservaService.listarReservas(fecha, salaId));
    }

    @GetMapping("/disponibilidad")
    public ResponseEntity<List<DisponibilidadDto>> obtenerDisponibilidad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Long salaId) {
        return ResponseEntity.ok(reservaService.obtenerDisponibilidad(fecha, salaId));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/mias")
    public ResponseEntity<List<ReservaDto>> listarMias(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = Long.parseLong(jwt.getSubject());
        return ResponseEntity.ok(reservaService.listarMias(usuarioId, fecha));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ReservaDto> crearReserva(
            @Valid @RequestBody CrearReservaRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = Long.parseLong(jwt.getSubject());
        String nombreRelator = jwt.getClaimAsString("nombre");
        ReservaDto creada = reservaService.crearReserva(request, usuarioId, nombreRelator);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/lote")
    public ResponseEntity<List<ReservaDto>> crearLote(
            @RequestBody List<CrearReservaRequest> lote,
            @AuthenticationPrincipal Jwt jwt) {
        Long usuarioId = Long.parseLong(jwt.getSubject());
        String nombreRelator = jwt.getClaimAsString("nombre");
        List<ReservaDto> creadas = reservaService.crearLote(lote, usuarioId, nombreRelator);
        return ResponseEntity.status(HttpStatus.CREATED).body(creadas);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ReservaDto> cancelarReserva(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication) {
        Long usuarioId = Long.parseLong(jwt.getSubject());
        boolean esCoordinador = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_COORDINADOR".equals(a.getAuthority()));
        ReservaDto cancelada = reservaService.cancelarReserva(id, usuarioId, esCoordinador);
        return ResponseEntity.ok(cancelada);
    }
}
