package cl.duoc.reservasalas.reservas.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.reservasalas.reservas.dto.BloqueDto;
import cl.duoc.reservasalas.reservas.service.BloqueService;

/**
 * Endpoints del horario y bloques predeterminados (Contrato 1, sección 5 de docs/plan-proyecto.md).
 */
@RestController
@RequestMapping("/api/v1/bloques")
public class BloqueController {

    private final BloqueService bloqueService;

    public BloqueController(BloqueService bloqueService) {
        this.bloqueService = bloqueService;
    }

    @GetMapping
    public ResponseEntity<List<BloqueDto>> listarBloques() {
        return ResponseEntity.ok(bloqueService.listarBloques());
    }
}
