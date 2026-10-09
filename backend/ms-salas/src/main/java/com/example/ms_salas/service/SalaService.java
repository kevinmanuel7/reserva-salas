package com.example.ms_salas.service;

import com.example.ms_salas.model.Sala;
import com.example.ms_salas.repository.SalaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaService {

    @Autowired
    private SalaRepository salaRepository;

    public List<Sala> listarSalas(Integer capacidadMin, Boolean proyector, Boolean computadores, Boolean climatizacion, boolean incluirInactivas) {
        return salaRepository.buscarSalasConFiltros(capacidadMin, proyector, computadores, climatizacion, incluirInactivas);
    }

    public Sala obtenerPorId(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SALA_NO_ENCONTRADA: No existe la sala con id " + id));
    }

    public Sala crearSala(Sala salaRequest) {
        if (salaRequest.getActiva() == null) {
            salaRequest.setActiva(true);
        }
        return salaRepository.save(salaRequest);
    }

    public Sala actualizarSala(Long id, Sala salaRequest) {
        Sala salaExistente = obtenerPorId(id);

        salaExistente.setCodigo(salaRequest.getCodigo());
        salaExistente.setNombre(salaRequest.getNombre());
        salaExistente.setTipo(salaRequest.getTipo());
        salaExistente.setCapacidad(salaRequest.getCapacidad());
        salaExistente.setTieneProyector(salaRequest.getTieneProyector());
        salaExistente.setTieneComputadores(salaRequest.getTieneComputadores());
        salaExistente.setTieneClimatizacion(salaRequest.getTieneClimatizacion());
        salaExistente.setActiva(salaRequest.getActiva());

        if (salaRequest.getComentariosEquipamiento() != null) {
            salaExistente.setComentariosEquipamiento(salaRequest.getComentariosEquipamiento());
        }

        return salaRepository.save(salaExistente);
    }

    public Sala actualizarEquipamiento(Long id, Sala equipamientoRequest) {
        Sala salaExistente = obtenerPorId(id);

        if (equipamientoRequest.getTieneProyector() != null) {
            salaExistente.setTieneProyector(equipamientoRequest.getTieneProyector());
        }
        if (equipamientoRequest.getTieneComputadores() != null) {
            salaExistente.setTieneComputadores(equipamientoRequest.getTieneComputadores());
        }
        if (equipamientoRequest.getTieneClimatizacion() != null) {
            salaExistente.setTieneClimatizacion(equipamientoRequest.getTieneClimatizacion());
        }
        if (equipamientoRequest.getComentariosEquipamiento() != null) {
            salaExistente.setComentariosEquipamiento(equipamientoRequest.getComentariosEquipamiento());
        }

        return salaRepository.save(salaExistente);
    }
}