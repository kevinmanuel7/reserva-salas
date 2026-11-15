package cl.duoc.reservasalas.reservas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.duoc.reservasalas.reservas.client.SalaClient;
import cl.duoc.reservasalas.reservas.dto.CrearReservaRequest;
import cl.duoc.reservasalas.reservas.dto.DisponibilidadDto;
import cl.duoc.reservasalas.reservas.dto.ReservaDto;
import cl.duoc.reservasalas.reservas.exception.BloqueInvalidoException;
import cl.duoc.reservasalas.reservas.exception.CruceHorarioException;
import cl.duoc.reservasalas.reservas.exception.FechaPasadaException;
import cl.duoc.reservasalas.reservas.exception.ReservaNoEncontradaException;
import cl.duoc.reservasalas.reservas.exception.SalaInactivaException;
import cl.duoc.reservasalas.reservas.exception.SinPermisoException;
import cl.duoc.reservasalas.reservas.exception.UsuarioOcupadoException;
import cl.duoc.reservasalas.reservas.model.EstadoReserva;
import cl.duoc.reservasalas.reservas.model.Reserva;
import cl.duoc.reservasalas.reservas.repository.ReservaRepository;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private SalaClient salaClient;

    @InjectMocks
    private ReservaService reservaService;

    private LocalDate fechaFutura;
    private Reserva reservaExistente;

    @BeforeEach
    void setUp() {
        fechaFutura = LocalDate.now().plusDays(2);
        reservaExistente = new Reserva(1L, 3L, "Relator Ficticio Uno", "Taller Excel",
                fechaFutura, 2, 4, EstadoReserva.ACTIVA);
        reservaExistente.setId(10L);
    }

    @Test
    void listarReservas_devuelveListaActivas() {
        when(reservaRepository.buscarActivasPorFechaYSala(fechaFutura, 1L))
                .thenReturn(List.of(reservaExistente));

        List<ReservaDto> resultado = reservaService.listarReservas(fechaFutura, 1L);

        assertEquals(1, resultado.size());
        assertEquals("Taller Excel", resultado.get(0).actividad());
    }

    @Test
    void obtenerDisponibilidad_devuelveSoloCamposPermitidos() {
        when(reservaRepository.buscarActivasPorFechaYSala(fechaFutura, 1L))
                .thenReturn(List.of(reservaExistente));

        List<DisponibilidadDto> resultado = reservaService.obtenerDisponibilidad(fechaFutura, 1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).salaId());
        assertEquals(2, resultado.get(0).bloqueInicio());
        assertEquals(4, resultado.get(0).bloqueFin());
    }

    @Test
    void crearReserva_exitosa() {
        CrearReservaRequest request = new CrearReservaRequest(
                1L, fechaFutura, 5, 6, "Python básico"
        );

        when(salaClient.obtenerYValidarSala(1L)).thenReturn(
                new SalaClient.SalaInfo(1L, "LAB-01", "Lab 1", "LABORATORIO", 20, true, true, true, true));
        when(reservaRepository.buscarCrucesSala(1L, fechaFutura, 5, 6)).thenReturn(Collections.emptyList());
        when(reservaRepository.buscarCrucesUsuario(3L, fechaFutura, 5, 6)).thenReturn(Collections.emptyList());
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(15L);
            return r;
        });

        ReservaDto resultado = reservaService.crearReserva(request, 3L, "Relator Ficticio Uno");

        assertNotNull(resultado);
        assertEquals(15L, resultado.id());
        assertEquals("Python básico", resultado.actividad());
        assertEquals(EstadoReserva.ACTIVA, resultado.estado());
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void crearReserva_bloqueInvalido_lanzaBloqueInvalidoException() {
        CrearReservaRequest request = new CrearReservaRequest(
                1L, fechaFutura, 5, 3, "Invertido"
        );

        assertThrows(BloqueInvalidoException.class, () ->
                reservaService.crearReserva(request, 3L, "Relator"));
    }

    @Test
    void crearReserva_fechaPasada_lanzaFechaPasadaException() {
        CrearReservaRequest request = new CrearReservaRequest(
                1L, LocalDate.now().minusDays(1), 2, 3, "Pasada"
        );

        assertThrows(FechaPasadaException.class, () ->
                reservaService.crearReserva(request, 3L, "Relator"));
    }

    @Test
    void crearReserva_salaInactiva_lanzaSalaInactivaException() {
        CrearReservaRequest request = new CrearReservaRequest(
                9L, fechaFutura, 2, 3, "Taller en sala inactiva"
        );
        when(salaClient.obtenerYValidarSala(9L))
                .thenThrow(new SalaInactivaException("La sala está fuera de servicio"));

        assertThrows(SalaInactivaException.class, () ->
                reservaService.crearReserva(request, 3L, "Relator"));
    }

    @Test
    void crearReserva_cruceHorarioSala_lanzaCruceHorarioException() {
        CrearReservaRequest request = new CrearReservaRequest(
                1L, fechaFutura, 3, 5, "Cruce sala"
        );
        when(salaClient.obtenerYValidarSala(1L)).thenReturn(
                new SalaClient.SalaInfo(1L, "LAB-01", "Lab 1", "LABORATORIO", 20, true, true, true, true));
        when(reservaRepository.buscarCrucesSala(1L, fechaFutura, 3, 5))
                .thenReturn(List.of(reservaExistente));

        assertThrows(CruceHorarioException.class, () ->
                reservaService.crearReserva(request, 4L, "Otro Relator"));
    }

    @Test
    void crearReserva_cruceUsuario_lanzaUsuarioOcupadoException() {
        CrearReservaRequest request = new CrearReservaRequest(
                2L, fechaFutura, 3, 5, "Cruce usuario en otra sala"
        );
        when(salaClient.obtenerYValidarSala(2L)).thenReturn(
                new SalaClient.SalaInfo(2L, "LAB-02", "Lab 2", "LABORATORIO", 20, true, true, true, true));
        when(reservaRepository.buscarCrucesSala(2L, fechaFutura, 3, 5)).thenReturn(Collections.emptyList());
        when(reservaRepository.buscarCrucesUsuario(3L, fechaFutura, 3, 5))
                .thenReturn(List.of(reservaExistente));

        assertThrows(UsuarioOcupadoException.class, () ->
                reservaService.crearReserva(request, 3L, "Relator Ficticio Uno"));
    }

    @Test
    void crearLote_exitoso() {
        CrearReservaRequest r1 = new CrearReservaRequest(1L, fechaFutura, 1, 2, "Taller 1");
        CrearReservaRequest r2 = new CrearReservaRequest(1L, fechaFutura, 3, 4, "Taller 2");

        when(salaClient.obtenerYValidarSala(1L)).thenReturn(
                new SalaClient.SalaInfo(1L, "LAB-01", "Lab 1", "LABORATORIO", 20, true, true, true, true));
        when(reservaRepository.buscarCrucesSala(eq(1L), eq(fechaFutura), any(), any())).thenReturn(Collections.emptyList());
        when(reservaRepository.buscarCrucesUsuario(eq(3L), eq(fechaFutura), any(), any())).thenReturn(Collections.emptyList());
        when(reservaRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ReservaDto> resultado = reservaService.crearLote(List.of(r1, r2), 3L, "Relator Ficticio Uno");

        assertEquals(2, resultado.size());
    }

    @Test
    void crearLote_cruceInternoMismaSala_lanzaCruceHorarioException() {
        CrearReservaRequest r1 = new CrearReservaRequest(1L, fechaFutura, 2, 4, "Taller 1");
        CrearReservaRequest r2 = new CrearReservaRequest(1L, fechaFutura, 3, 5, "Taller 2");

        assertThrows(CruceHorarioException.class, () ->
                reservaService.crearLote(List.of(r1, r2), 3L, "Relator"));
    }

    @Test
    void crearLote_cruceInternoDistintaSala_lanzaUsuarioOcupadoException() {
        CrearReservaRequest r1 = new CrearReservaRequest(1L, fechaFutura, 2, 4, "Taller 1");
        CrearReservaRequest r2 = new CrearReservaRequest(2L, fechaFutura, 3, 5, "Taller 2");

        assertThrows(UsuarioOcupadoException.class, () ->
                reservaService.crearLote(List.of(r1, r2), 3L, "Relator"));
    }

    @Test
    void cancelarReserva_porDuenio_exitosa() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reservaExistente));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaDto cancelada = reservaService.cancelarReserva(10L, 3L, false);

        assertEquals(EstadoReserva.CANCELADA, cancelada.estado());
        assertEquals(EstadoReserva.CANCELADA, reservaExistente.getEstado());
    }

    @Test
    void cancelarReserva_porCoordinador_exitosa() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reservaExistente));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaDto cancelada = reservaService.cancelarReserva(10L, 1L, true);

        assertEquals(EstadoReserva.CANCELADA, cancelada.estado());
    }

    @Test
    void cancelarReserva_porOtroRelator_lanzaSinPermisoException() {
        when(reservaRepository.findById(10L)).thenReturn(Optional.of(reservaExistente));

        assertThrows(SinPermisoException.class, () ->
                reservaService.cancelarReserva(10L, 99L, false));
    }

    @Test
    void cancelarReserva_noExiste_lanzaReservaNoEncontradaException() {
        when(reservaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ReservaNoEncontradaException.class, () ->
                reservaService.cancelarReserva(999L, 3L, false));
    }
}
