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

import java.time.LocalTime;
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
import cl.duoc.reservasalas.salas.dto.EquipoDto;
import cl.duoc.reservasalas.salas.dto.SalaDto;
import cl.duoc.reservasalas.salas.exception.CodigoSalaDuplicadoException;
import cl.duoc.reservasalas.salas.exception.SalaNoEncontradaException;
import cl.duoc.reservasalas.salas.model.Equipo;
import cl.duoc.reservasalas.salas.model.EstadoEquipo;
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
                "SC-101", "Sala 101", TipoSala.SALA_CLASES, 30, true, false, true, true,
                null, null, null, null
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
                "LAB-01", "Otro lab", TipoSala.LABORATORIO, 20, true, true, false, true,
                null, null, null, null
        );
        when(salaRepository.existsByCodigo("LAB-01")).thenReturn(true);

        assertThrows(CodigoSalaDuplicadoException.class, () -> salaService.crearSala(request));
        verify(salaRepository, never()).save(any());
    }

    @Test
    void actualizarSala_cuandoDatosSonValidos_actualizaYDevuelveDto() {
        ActualizarSalaRequest request = new ActualizarSalaRequest(
                "LAB-01-MOD", "Laboratorio Renovado", TipoSala.LABORATORIO, 30, true, true, true, true,
                null, null, null, null
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
                "LAB-02", "Laboratorio Duplicado", TipoSala.LABORATORIO, 20, true, true, false, true,
                null, null, null, null
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

    @Test
    void crearSala_conEquipos_calculaLosBooleanosDesdeLaLista() {
        // Se envían los tres booleanos en false, pero la lista dice otra cosa: manda la lista.
        CrearSalaRequest request = new CrearSalaRequest(
                "SC-401", "Sala 401", TipoSala.SALA_CLASES, 30, false, false, false, true,
                "Edificio B, piso 4", LocalTime.of(9, 0), LocalTime.of(18, 0),
                List.of(new EquipoDto("Proyector", EstadoEquipo.CON_FALLA, "No enciende"),
                        new EquipoDto("Climatización", EstadoEquipo.OPERATIVO, null))
        );
        when(salaRepository.existsByCodigo("SC-401")).thenReturn(false);
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalaDto resultado = salaService.crearSala(request);

        assertTrue(resultado.tieneProyector());
        assertFalse(resultado.tieneComputadores());
        assertTrue(resultado.tieneClimatizacion());
        assertEquals("Edificio B, piso 4", resultado.ubicacion());
        assertEquals(LocalTime.of(9, 0), resultado.horaApertura());
        assertEquals(2, resultado.equipos().size());
        assertEquals("No enciende", resultado.equipos().get(0).comentario());
    }

    @Test
    void crearSala_sinEquipos_conservaLosBooleanosEnviados() {
        CrearSalaRequest request = new CrearSalaRequest(
                "SC-402", "Sala 402", TipoSala.SALA_CLASES, 30, true, false, true, true,
                null, null, null, null
        );
        when(salaRepository.existsByCodigo("SC-402")).thenReturn(false);
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalaDto resultado = salaService.crearSala(request);

        assertTrue(resultado.tieneProyector());
        assertFalse(resultado.tieneComputadores());
        assertTrue(resultado.tieneClimatizacion());
        assertTrue(resultado.equipos().isEmpty());
    }

    @Test
    void actualizarSala_conCamposNuevosEnNull_conservaLosValoresGuardados() {
        // Así actualiza la web: no envía ubicacion, horas ni equipos.
        salaPrueba.setUbicacion("Edificio A, piso 2");
        salaPrueba.setHoraApertura(LocalTime.of(8, 0));
        salaPrueba.setHoraCierre(LocalTime.of(14, 0));
        salaPrueba.getEquipos().add(new Equipo("Proyector", EstadoEquipo.OPERATIVO, null));
        salaPrueba.getEquipos().add(new Equipo("Computadores", EstadoEquipo.OPERATIVO, null));

        ActualizarSalaRequest request = new ActualizarSalaRequest(
                "LAB-01", "Laboratorio Renovado", TipoSala.LABORATORIO, 30, true, true, true, true,
                null, null, null, null
        );
        when(salaRepository.findById(1L)).thenReturn(Optional.of(salaPrueba));
        when(salaRepository.existsByCodigoAndIdNot("LAB-01", 1L)).thenReturn(false);
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalaDto resultado = salaService.actualizarSala(1L, request);

        assertEquals("Laboratorio Renovado", resultado.nombre());
        assertEquals("Edificio A, piso 2", resultado.ubicacion());
        assertEquals(LocalTime.of(8, 0), resultado.horaApertura());
        assertEquals(LocalTime.of(14, 0), resultado.horaCierre());
        assertEquals(2, resultado.equipos().size());
        assertEquals("Proyector", resultado.equipos().get(0).nombre());
    }

    @Test
    void actualizarSala_conEquipos_reemplazaLaListaYRecalculaLosBooleanos() {
        salaPrueba.getEquipos().add(new Equipo("Proyector", EstadoEquipo.OPERATIVO, null));

        ActualizarSalaRequest request = new ActualizarSalaRequest(
                "LAB-01", "Laboratorio Computación 1", TipoSala.LABORATORIO, 24, true, true, true, true,
                null, null, null,
                List.of(new EquipoDto("Computadores", EstadoEquipo.OPERATIVO, null),
                        new EquipoDto("Pizarra", EstadoEquipo.OPERATIVO, null))
        );
        when(salaRepository.findById(1L)).thenReturn(Optional.of(salaPrueba));
        when(salaRepository.existsByCodigoAndIdNot("LAB-01", 1L)).thenReturn(false);
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalaDto resultado = salaService.actualizarSala(1L, request);

        assertFalse(resultado.tieneProyector());
        assertTrue(resultado.tieneComputadores());
        assertFalse(resultado.tieneClimatizacion());
        assertEquals(List.of("Computadores", "Pizarra"),
                resultado.equipos().stream().map(EquipoDto::nombre).toList());
    }

    @Test
    void actualizarSala_conListaDeEquiposVacia_dejaLosTresBooleanosEnFalso() {
        salaPrueba.getEquipos().add(new Equipo("Proyector", EstadoEquipo.OPERATIVO, null));

        ActualizarSalaRequest request = new ActualizarSalaRequest(
                "LAB-01", "Laboratorio Computación 1", TipoSala.LABORATORIO, 24, true, true, true, true,
                null, null, null, List.of()
        );
        when(salaRepository.findById(1L)).thenReturn(Optional.of(salaPrueba));
        when(salaRepository.existsByCodigoAndIdNot("LAB-01", 1L)).thenReturn(false);
        when(salaRepository.save(any(Sala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SalaDto resultado = salaService.actualizarSala(1L, request);

        assertFalse(resultado.tieneProyector());
        assertFalse(resultado.tieneComputadores());
        assertFalse(resultado.tieneClimatizacion());
        assertTrue(resultado.equipos().isEmpty());
    }
}
