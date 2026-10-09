package com.example.ms_salas.controller;

import com.example.ms_salas.model.Sala;
import com.example.ms_salas.service.SalaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salas")
public class SalaController {

    @Autowired
    private SalaService salaService;

    @GetMapping
    public ResponseEntity<List<Sala>> listarSalas(
            @RequestParam(required = false) Integer capacidadMin,
            @RequestParam(required = false) Boolean proyector,
            @RequestParam(required = false) Boolean computadores,
            @RequestParam(required = false) Boolean climatizacion,
            @RequestParam(required = false, defaultValue = "false") boolean incluirInactivas,
            Authentication authentication) {

        if (incluirInactivas && !hasRole(authentication, "COORDINADOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Sala> salas = salaService.listarSalas(capacidadMin, proyector, computadores, climatizacion, incluirInactivas);
        return ResponseEntity.ok(salas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sala> obtenerSalaPorId(@PathVariable Long id) {
        Sala sala = salaService.obtenerPorId(id);
        return ResponseEntity.ok(sala);
    }

    @PostMapping
    public ResponseEntity<Sala> crearSala(
            @Valid @RequestBody Sala salaRequest,
            Authentication authentication) {

        if (!hasRole(authentication, "COORDINADOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sala nuevaSala = salaService.crearSala(salaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaSala);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sala> actualizarSala(
            @PathVariable Long id,
            @Valid @RequestBody Sala salaRequest,
            Authentication authentication) {

        if (!hasRole(authentication, "COORDINADOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sala salaActualizada = salaService.actualizarSala(id, salaRequest);
        return ResponseEntity.ok(salaActualizada);
    }

    @PatchMapping("/{id}/equipamiento")
    public ResponseEntity<Sala> actualizarEquipamiento(
            @PathVariable Long id,
            @RequestBody Sala equipamientoRequest,
            Authentication authentication) {

        if (!hasRole(authentication, "COORDINADOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Sala salaActualizada = salaService.actualizarEquipamiento(id, equipamientoRequest);
        return ResponseEntity.ok(salaActualizada);
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}