package cl.duoc.reservasalas.salas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.reservasalas.salas.dto.ActualizarSalaRequest;
import cl.duoc.reservasalas.salas.dto.CrearSalaRequest;
import cl.duoc.reservasalas.salas.dto.SalaDto;
import cl.duoc.reservasalas.salas.exception.CodigoSalaDuplicadoException;
import cl.duoc.reservasalas.salas.exception.SalaNoEncontradaException;
import cl.duoc.reservasalas.salas.model.Sala;
import cl.duoc.reservasalas.salas.model.TipoSala;
import cl.duoc.reservasalas.salas.repository.SalaRepository;

/**
 * Pruebas unitarias de SalaService. No levantan Spring ni MySQL:
 * el repositorio se reemplaza con un mock de Mockito.
 */
@ExtendWith(MockitoExtension.class)
class SalaServiceTest {

    @Mock
    private SalaRepository salaRepository;

    @InjectMocks
    private SalaService salaService;

    private Sala salaPrueba;

    @BeforeEach
    void setUp() {
        salaPrueba = new Sala("LAB-01", "Laboratorio Computación 1", TipoSala.LABORATORIO, 24, true, true, true, true);
        salaPrueba.setId(1L);
    }

    @Test
    void listarSalas_devuelveListaDeSalasDto() {
        when(salaRepository.buscarSalasConFiltros(20, true, null, null, false))
                .thenReturn(List.of(salaPrueba));

        List<SalaDto> resultado = salaService.listarSalas(20, true, null, null, false);

        assertEquals(1, resultado.size());
        assertEquals("LAB-01", resultado.get(0).codigo());
        assertEquals(24, resultado.get(0).capacidad());
        verify(salaRepository).buscarSalasConFiltros(20, true, null, null, false);
    }

    @Test
    void obtenerPorId_cuandoExiste_devuelveSalaDto() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(salaPrueba));

        SalaDto resultado = salaService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("LAB-01", resultado.codigo());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_lanzaSalaNoEncontradaException() {
        when(salaRepository.findById(99L)).thenReturn(Optional.empty());

        SalaNoEncontradaException ex = assertThrows(SalaNoEncontradaException.class, () ->
                salaService.obtenerPorId(99L));

        assertEquals("No existe la sala con id 99", ex.getMessage());
    }

    @Test
    void crearSala_cuandoDatosSonValidos_guardaYDevuelveSalaDto() {
        CrearSalaRequest request = new CrearSalaRequest(
                "SC-101", "Sala 101", TipoSala.SALA_CLASES, 30, true, false, true, true
        );
        when(salaRepository.existsByCodigo("SC-101")).thenReturn(false);
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocation -> {
            Sala s = invocation.getArgument(0);
            s.setId(3L);
            return s;
        });

        SalaDto resultado = salaService.crearSala(request);

        assertNotNull(resultado);
        assertEquals(3L, resultado.id());
        assertEquals("SC-101", resultado.codigo());
        assertEquals(TipoSala.SALA_CLASES, resultado.tipo());
        verify(salaRepository).save(any(Sala.class));
    }

    @Test
    void crearSala_cuandoCodigoYaExiste_lanzaCodigoSalaDuplicadoException() {
        CrearSalaRequest request = new CrearSalaRequest(
                "LAB-01", "Otro lab", TipoSala.LABORATORIO, 20, true, true, false, true
        );
        when(salaRepository.existsByCodigo("LAB-01")).thenReturn(true);

        assertThrows(CodigoSalaDuplicadoException.class, () -> salaService.crearSala(request));
        verify(salaRepository, never()).save(any());
    }

    @Test
    void actualizarSala_cuandoDatosSonValidos_actualizaYDevuelveDto() {
        ActualizarSalaRequest request = new ActualizarSalaRequest(
                "LAB-01-MOD", "Laboratorio Renovado", TipoSala.LABORATORIO, 30, true, true, true, true
        );
        when(salaRepository.findById(1L)).thenReturn(Optional.of(salaPrueba));
        when(salaRepository.existsByCodigoAndIdNot("LAB-01-MOD", 1L)).thenReturn(false);
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalaDto resultado = salaService.actualizarSala(1L, request);

        assertEquals("LAB-01-MOD", resultado.codigo());
        assertEquals("Laboratorio Renovado", resultado.nombre());
        assertEquals(30, resultado.capacidad());
    }

    @Test
    void actualizarSala_cuandoNuevoCodigoPerteneceAOtraSala_lanzaCodigoSalaDuplicadoException() {
        ActualizarSalaRequest request = new ActualizarSalaRequest(
                "LAB-02", "Laboratorio Duplicado", TipoSala.LABORATORIO, 20, true, true, false, true
        );
        when(salaRepository.findById(1L)).thenReturn(Optional.of(salaPrueba));
        when(salaRepository.existsByCodigoAndIdNot("LAB-02", 1L)).thenReturn(true);

        assertThrows(CodigoSalaDuplicadoException.class, () -> salaService.actualizarSala(1L, request));
        verify(salaRepository, never()).save(any());
    }

    @Test
    void eliminarSala_realizaBajaLogica_cambiandoActivaAFalso() {
        when(salaRepository.findById(1L)).thenReturn(Optional.of(salaPrueba));
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalaDto resultado = salaService.eliminarSala(1L);

        assertFalse(resultado.activa());
        assertFalse(salaPrueba.getActiva());
        verify(salaRepository).save(salaPrueba);
    }
}
