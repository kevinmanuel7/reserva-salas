package cl.duoc.reservasalas.reservas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.reservasalas.reservas.dto.BloqueDto;
import cl.duoc.reservasalas.reservas.repository.BloqueRepository;

@Service
public class BloqueService {

    private final BloqueRepository bloqueRepository;

    public BloqueService(BloqueRepository bloqueRepository) {
        this.bloqueRepository = bloqueRepository;
    }

    @Transactional(readOnly = true)
    public List<BloqueDto> listarBloques() {
        return bloqueRepository.findAllByOrderByNumeroAsc()
                .stream()
                .map(BloqueDto::desde)
                .toList();
    }
}
