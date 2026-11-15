package cl.duoc.reservasalas.reservas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import cl.duoc.reservasalas.reservas.dto.CrearReservaRequest;
import cl.duoc.reservasalas.reservas.dto.DisponibilidadDto;
import cl.duoc.reservasalas.reservas.dto.ReservaDto;
import cl.duoc.reservasalas.reservas.exception.ManejadorGlobalErrores;
import cl.duoc.reservasalas.reservas.model.EstadoReserva;
import cl.duoc.reservasalas.reservas.service.ReservaService;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReservaService reservaService;

    @InjectMocks
    private ReservaController reservaController;

    private Jwt dummyJwt;
    private ReservaDto reservaDto;

    @BeforeEach
    void setUp() {
        dummyJwt = new Jwt(
                "token-falso",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256"),
                Map.of("sub", "3", "nombre", "Relator Ficticio Uno", "rol", "RELATOR")
        );

        HandlerMethodArgumentResolver jwtResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                        && parameter.getParameterType().isAssignableFrom(Jwt.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return dummyJwt;
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(reservaController)
                .setControllerAdvice(new ManejadorGlobalErrores())
                .setCustomArgumentResolvers(jwtResolver)
                .build();

        reservaDto = new ReservaDto(
                1L, 1L, 3L, "Relator Ficticio Uno", "Taller Excel",
                LocalDate.of(2026, 11, 20), 2, 4, EstadoReserva.ACTIVA
        );
    }

    @Test
    void listarReservas_devuelve200() throws Exception {
        LocalDate fecha = LocalDate.of(2026, 11, 20);
        when(reservaService.listarReservas(fecha, 1L)).thenReturn(List.of(reservaDto));

        mockMvc.perform(get("/api/v1/reservas?fecha=2026-11-20&salaId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].actividad").value("Taller Excel"));
    }

    @Test
    void obtenerDisponibilidad_devuelve200() throws Exception {
        LocalDate fecha = LocalDate.of(2026, 11, 20);
        DisponibilidadDto disp = new DisponibilidadDto(1L, fecha, 2, 4);
        when(reservaService.obtenerDisponibilidad(fecha, null)).thenReturn(List.of(disp));

        mockMvc.perform(get("/api/v1/reservas/disponibilidad?fecha=2026-11-20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].salaId").value(1))
                .andExpect(jsonPath("$[0].bloqueInicio").value(2));
    }

    @Test
    void listarMias_devuelve200() throws Exception {
        when(reservaService.listarMias(3L, null)).thenReturn(List.of(reservaDto));

        mockMvc.perform(get("/api/v1/reservas/mias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(3));
    }

    @Test
    void crearReserva_conDatosValidos_devuelve201() throws Exception {
        when(reservaService.crearReserva(any(CrearReservaRequest.class), eq(3L), eq("Relator Ficticio Uno")))
                .thenReturn(reservaDto);

        String json = """
                {
                    "salaId": 1,
                    "fecha": "2026-11-20",
                    "bloqueInicio": 2,
                    "bloqueFin": 4,
                    "actividad": "Taller Excel"
                }
                """;

        mockMvc.perform(post("/api/v1/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombreRelator").value("Relator Ficticio Uno"));
    }

    @Test
    void crearLote_conDatosValidos_devuelve201() throws Exception {
        when(reservaService.crearLote(any(), eq(3L), eq("Relator Ficticio Uno")))
                .thenReturn(List.of(reservaDto));

        String json = """
                [
                    {
                        "salaId": 1,
                        "fecha": "2026-11-20",
                        "bloqueInicio": 2,
                        "bloqueFin": 4,
                        "actividad": "Taller Excel"
                    }
                ]
                """;

        mockMvc.perform(post("/api/v1/reservas/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void cancelarReserva_devuelve200ConEstadoCancelada() throws Exception {
        ReservaDto cancelada = new ReservaDto(
                1L, 1L, 3L, "Relator Ficticio Uno", "Taller Excel",
                LocalDate.of(2026, 11, 20), 2, 4, EstadoReserva.CANCELADA
        );
        when(reservaService.cancelarReserva(eq(1L), eq(3L), eq(false))).thenReturn(cancelada);

        mockMvc.perform(put("/api/v1/reservas/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }
}
