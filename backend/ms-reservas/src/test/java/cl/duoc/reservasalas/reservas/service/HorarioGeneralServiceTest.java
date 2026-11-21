package cl.duoc.reservasalas.reservas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.reservasalas.reservas.dto.ActualizarHorarioRequest;
import cl.duoc.reservasalas.reservas.dto.HorarioGeneralDto;
import cl.duoc.reservasalas.reservas.exception.DatosInvalidosException;
import cl.duoc.reservasalas.reservas.exception.SinPermisoException;
import cl.duoc.reservasalas.reservas.model.HorarioGeneral;
import cl.duoc.reservasalas.reservas.repository.HorarioGeneralRepository;

@ExtendWith(MockitoExtension.class)
class HorarioGeneralServiceTest {

    @Mock
    private HorarioGeneralRepository horarioGeneralRepository;

    @InjectMocks
    private HorarioGeneralService horarioGeneralService;

    private static ActualizarHorarioRequest pedido(LocalTime apertura, LocalTime cierre) {
        return new ActualizarHorarioRequest(apertura, cierre, 10, List.of(DayOfWeek.MONDAY));
    }

    @Test
    void obtener_devuelveElHorarioGuardado() {
        HorarioGeneral guardado = new HorarioGeneral(LocalTime.of(9, 0), LocalTime.of(18, 0), 10,
                List.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY));
        when(horarioGeneralRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(guardado));

        HorarioGeneralDto resultado = horarioGeneralService.obtener();

        assertEquals(LocalTime.of(9, 0), resultado.apertura());
        assertEquals(LocalTime.of(18, 0), resultado.cierre());
        assertEquals(10, resultado.pausaMinutos());
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY), resultado.dias());
    }

    @Test
    void obtener_conTablaVacia_creaElHorarioPorDefecto() {
        when(horarioGeneralRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(horarioGeneralRepository.save(any(HorarioGeneral.class))).thenAnswer(i -> i.getArgument(0));

        HorarioGeneralDto resultado = horarioGeneralService.obtener();

        assertEquals(LocalTime.of(8, 0), resultado.apertura());
        assertEquals(LocalTime.of(21, 0), resultado.cierre());
        assertEquals(5, resultado.pausaMinutos());
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY), resultado.dias());
    }

    @Test
    void crearPorDefectoSiFalta_conTablaVacia_insertaLaFila() {
        when(horarioGeneralRepository.count()).thenReturn(0L);

        assertTrue(horarioGeneralService.crearPorDefectoSiFalta());
        verify(horarioGeneralRepository).save(any(HorarioGeneral.class));
    }

    @Test
    void crearPorDefectoSiFalta_conDatos_noInserta() {
        when(horarioGeneralRepository.count()).thenReturn(1L);

        assertFalse(horarioGeneralService.crearPorDefectoSiFalta());
        verify(horarioGeneralRepository, never()).save(any());
    }

    @Test
    void actualizar_conDatosValidos_guardaLosValores() {
        when(horarioGeneralRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(HorarioGeneral.porDefecto()));
        when(horarioGeneralRepository.save(any(HorarioGeneral.class))).thenAnswer(i -> i.getArgument(0));
        ActualizarHorarioRequest request = new ActualizarHorarioRequest(
                LocalTime.of(9, 0), LocalTime.of(20, 0), 0,
                List.of(DayOfWeek.SATURDAY, DayOfWeek.MONDAY, DayOfWeek.MONDAY));

        HorarioGeneralDto resultado = horarioGeneralService.actualizar(request, true);

        assertEquals(LocalTime.of(9, 0), resultado.apertura());
        assertEquals(LocalTime.of(20, 0), resultado.cierre());
        assertEquals(0, resultado.pausaMinutos());
        // Sin repetidos y en orden de la semana.
        assertEquals(List.of(DayOfWeek.MONDAY, DayOfWeek.SATURDAY), resultado.dias());
    }

    @Test
    void actualizar_aceptaLosLimites0800y2100() {
        when(horarioGeneralRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(HorarioGeneral.porDefecto()));
        when(horarioGeneralRepository.save(any(HorarioGeneral.class))).thenAnswer(i -> i.getArgument(0));

        HorarioGeneralDto resultado = horarioGeneralService.actualizar(
                pedido(LocalTime.of(8, 0), LocalTime.of(21, 0)), true);

        assertEquals(LocalTime.of(8, 0), resultado.apertura());
        assertEquals(LocalTime.of(21, 0), resultado.cierre());
    }

    @Test
    void actualizar_porRelator_lanzaSinPermiso() {
        assertThrows(SinPermisoException.class, () -> horarioGeneralService.actualizar(
                pedido(LocalTime.of(9, 0), LocalTime.of(18, 0)), false));
        verify(horarioGeneralRepository, never()).save(any());
    }

    @Test
    void actualizar_aperturaConMinutos_lanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> horarioGeneralService.actualizar(
                pedido(LocalTime.of(9, 30), LocalTime.of(18, 0)), true));
    }

    @Test
    void actualizar_cierreConMinutos_lanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> horarioGeneralService.actualizar(
                pedido(LocalTime.of(9, 0), LocalTime.of(18, 5)), true));
    }

    @Test
    void actualizar_aperturaAntesDe0800_lanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> horarioGeneralService.actualizar(
                pedido(LocalTime.of(7, 0), LocalTime.of(18, 0)), true));
    }

    @Test
    void actualizar_cierreDespuesDe2100_lanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> horarioGeneralService.actualizar(
                pedido(LocalTime.of(9, 0), LocalTime.of(22, 0)), true));
    }

    @Test
    void actualizar_aperturaIgualACierre_lanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> horarioGeneralService.actualizar(
                pedido(LocalTime.of(10, 0), LocalTime.of(10, 0)), true));
    }

    @Test
    void actualizar_aperturaDespuesDelCierre_lanzaDatosInvalidos() {
        assertThrows(DatosInvalidosException.class, () -> horarioGeneralService.actualizar(
                pedido(LocalTime.of(18, 0), LocalTime.of(9, 0)), true));
        verify(horarioGeneralRepository, never()).save(any());
    }
}
