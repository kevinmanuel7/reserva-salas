package cl.duoc.reservasalas.salas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.reservasalas.salas.dto.ActualizarSalaRequest;
import cl.duoc.reservasalas.salas.dto.CrearSalaRequest;
import cl.duoc.reservasalas.salas.dto.SalaDto;
import cl.duoc.reservasalas.salas.exception.CodigoSalaDuplicadoException;
import cl.duoc.reservasalas.salas.exception.SalaNoEncontradaException;
import cl.duoc.reservasalas.salas.model.Sala;
import cl.duoc.reservasalas.salas.repository.SalaRepository;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    @Transactional(readOnly = true)
    public List<SalaDto> listarSalas(Integer capacidadMin, Boolean proyector, Boolean computadores,
                                     Boolean climatizacion, boolean incluirInactivas) {
        return salaRepository.buscarSalasConFiltros(capacidadMin, proyector, computadores, climatizacion, incluirInactivas)
                .stream()
                .map(SalaDto::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalaDto obtenerPorId(Long id) {
        return salaRepository.findById(id)
                .map(SalaDto::desde)
                .orElseThrow(() -> new SalaNoEncontradaException("No existe la sala con id " + id));
    }

    @Transactional
    public SalaDto crearSala(CrearSalaRequest request) {
        if (salaRepository.existsByCodigo(request.codigo())) {
            throw new CodigoSalaDuplicadoException("Ya existe una sala con el código " + request.codigo());
        }

        Sala sala = new Sala(
                request.codigo(),
                request.nombre(),
                request.tipo(),
                request.capacidad(),
                request.tieneProyector(),
                request.tieneComputadores(),
                request.tieneClimatizacion(),
                request.activa() != null ? request.activa() : true
        );

        return SalaDto.desde(salaRepository.save(sala));
    }

    @Transactional
    public SalaDto actualizarSala(Long id, ActualizarSalaRequest request) {
        Sala sala = salaRepository.findById(id)
                .orElseThrow(() -> new SalaNoEncontradaException("No existe la sala con id " + id));

        if (salaRepository.existsByCodigoAndIdNot(request.codigo(), id)) {
            throw new CodigoSalaDuplicadoException("Ya existe otra sala con el código " + request.codigo());
        }

        sala.setCodigo(request.codigo());
        sala.setNombre(request.nombre());
        sala.setTipo(request.tipo());
        sala.setCapacidad(request.capacidad());
        sala.setTieneProyector(request.tieneProyector());
        sala.setTieneComputadores(request.tieneComputadores());
        sala.setTieneClimatizacion(request.tieneClimatizacion());
        sala.setActiva(request.activa());

        return SalaDto.desde(salaRepository.save(sala));
    }

    @Transactional
    public SalaDto eliminarSala(Long id) {
        Sala sala = salaRepository.findById(id)
                .orElseThrow(() -> new SalaNoEncontradaException("No existe la sala con id " + id));

        sala.setActiva(false);
        return SalaDto.desde(salaRepository.save(sala));
    }
}
