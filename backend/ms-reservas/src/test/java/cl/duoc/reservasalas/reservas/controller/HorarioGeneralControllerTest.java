package cl.duoc.reservasalas.reservas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.duoc.reservasalas.reservas.dto.ActualizarHorarioRequest;
import cl.duoc.reservasalas.reservas.dto.HorarioGeneralDto;
import cl.duoc.reservasalas.reservas.exception.DatosInvalidosException;
import cl.duoc.reservasalas.reservas.exception.ManejadorGlobalErrores;
import cl.duoc.reservasalas.reservas.exception.SinPermisoException;
import cl.duoc.reservasalas.reservas.service.HorarioGeneralService;

@ExtendWith(MockitoExtension.class)
class HorarioGeneralControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HorarioGeneralService horarioGeneralService;

    @InjectMocks
    private HorarioGeneralController horarioGeneralController;

    private static final String JSON_VALIDO = """
            {
                "apertura": "09:00",
                "cierre": "20:00",
                "pausaMinutos": 10,
                "dias": ["MONDAY", "WEDNESDAY"]
            }
            """;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(horarioGeneralController)
                .setControllerAdvice(new ManejadorGlobalErrores())
                .build();
    }

    // Simula un token con rol COORDINADOR (lo que SecurityConfig arma a partir del claim rol).
    private static UsernamePasswordAuthenticationToken coordinador() {
        return new UsernamePasswordAuthenticationToken("1", null,
                List.of(new SimpleGrantedAuthority("ROLE_COORDINADOR")));
    }

    private static UsernamePasswordAuthenticationToken relator() {
        return new UsernamePasswordAuthenticationToken("3", null,
                List.of(new SimpleGrantedAuthority("ROLE_RELATOR")));
    }

    // Envía un PUT de coordinador con el JSON dado y espera 400 DATOS_INVALIDOS
    // sin que el servicio llegue a llamarse (lo frena Bean Validation o Jackson).
    private void putInvalido(String json) throws Exception {
        mockMvc.perform(put("/api/v1/horario")
                        .principal(coordinador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"));
        verify(horarioGeneralService, never()).actualizar(any(), eq(true));
    }

    @Test
    void obtenerHorario_devuelve200ConHorasHHmmYDiasEnTexto() throws Exception {
        when(horarioGeneralService.obtener()).thenReturn(new HorarioGeneralDto(
                LocalTime.of(8, 0), LocalTime.of(21, 0), 5,
                List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)));

        mockMvc.perform(get("/api/v1/horario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apertura").value("08:00"))
                .andExpect(jsonPath("$.cierre").value("21:00"))
                .andExpect(jsonPath("$.pausaMinutos").value(5))
                .andExpect(jsonPath("$.dias[0]").value("MONDAY"))
                .andExpect(jsonPath("$.dias[4]").value("FRIDAY"));
    }

    @Test
    void actualizarHorario_porCoordinador_devuelve200() throws Exception {
        when(horarioGeneralService.actualizar(any(ActualizarHorarioRequest.class), eq(true)))
                .thenReturn(new HorarioGeneralDto(LocalTime.of(9, 0), LocalTime.of(20, 0), 10,
                        List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)));

        mockMvc.perform(put("/api/v1/horario")
                        .principal(coordinador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apertura").value("09:00"))
                .andExpect(jsonPath("$.cierre").value("20:00"))
                .andExpect(jsonPath("$.pausaMinutos").value(10))
                .andExpect(jsonPath("$.dias[1]").value("WEDNESDAY"));
    }

    @Test
    void actualizarHorario_porRelator_devuelve403() throws Exception {
        // Sin rol COORDINADOR, el controlador debe pasar esCoordinador = false.
        when(horarioGeneralService.actualizar(any(ActualizarHorarioRequest.class), eq(false)))
                .thenThrow(new SinPermisoException("Solo el coordinador puede editar el horario general"));

        mockMvc.perform(put("/api/v1/horario")
                        .principal(relator())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("SIN_PERMISO"));
    }

    @Test
    void actualizarHorario_reglaDelServicioNoCumplida_devuelve400() throws Exception {
        // Apertura posterior al cierre, horas no exactas o fuera de 08:00–21:00.
        when(horarioGeneralService.actualizar(any(ActualizarHorarioRequest.class), eq(true)))
                .thenThrow(new DatosInvalidosException("La hora de apertura debe ser anterior a la hora de cierre"));

        mockMvc.perform(put("/api/v1/horario")
                        .principal(coordinador())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("DATOS_INVALIDOS"))
                .andExpect(jsonPath("$.mensaje").value("La hora de apertura debe ser anterior a la hora de cierre"));
    }

    @Test
    void actualizarHorario_sinApertura_devuelve400() throws Exception {
        putInvalido("""
                {"cierre": "20:00", "pausaMinutos": 5, "dias": ["MONDAY"]}
                """);
    }

    @Test
    void actualizarHorario_sinCierre_devuelve400() throws Exception {
        putInvalido("""
                {"apertura": "09:00", "pausaMinutos": 5, "dias": ["MONDAY"]}
                """);
    }

    @Test
    void actualizarHorario_sinPausa_devuelve400() throws Exception {
        putInvalido("""
                {"apertura": "09:00", "cierre": "20:00", "dias": ["MONDAY"]}
                """);
    }

    @Test
    void actualizarHorario_pausaNegativa_devuelve400() throws Exception {
        putInvalido("""
                {"apertura": "09:00", "cierre": "20:00", "pausaMinutos": -1, "dias": ["MONDAY"]}
                """);
    }

    @Test
    void actualizarHorario_pausaMayorA30_devuelve400() throws Exception {
        putInvalido("""
                {"apertura": "09:00", "cierre": "20:00", "pausaMinutos": 31, "dias": ["MONDAY"]}
                """);
    }

    @Test
    void actualizarHorario_diasVacio_devuelve400() throws Exception {
        putInvalido("""
                {"apertura": "09:00", "cierre": "20:00", "pausaMinutos": 5, "dias": []}
                """);
    }

    @Test
    void actualizarHorario_sinDias_devuelve400() throws Exception {
        putInvalido("""
                {"apertura": "09:00", "cierre": "20:00", "pausaMinutos": 5}
                """);
    }

    @Test
    void actualizarHorario_diaDesconocido_devuelve400() throws Exception {
        putInvalido("""
                {"apertura": "09:00", "cierre": "20:00", "pausaMinutos": 5, "dias": ["LUNES"]}
                """);
    }

    @Test
    void actualizarHorario_horaSinDosDigitos_devuelve400() throws Exception {
        putInvalido("""
                {"apertura": "9:00", "cierre": "20:00", "pausaMinutos": 5, "dias": ["MONDAY"]}
                """);
    }
}
