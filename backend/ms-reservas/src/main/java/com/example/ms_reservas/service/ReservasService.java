package com.example.ms_reservas.service;

import org.springframework.web.client.RestTemplate;
import com.example.ms_reservas.model.Bloque;
import com.example.ms_reservas.model.Reservas;
import com.example.ms_reservas.repository.ReservasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import cl.duoc.reservasalas.reservas.dto.SalaDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ReservasService {

    @Autowired
    private ReservasRepository reservaRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpServletRequest httpServletRequest;

    public List<Reservas> obtenerTodas() {
        return reservaRepository.findAll();
    }

    public List<Bloque> obtenerBloques() {
        List<Bloque> bloques = new ArrayList<>();
        String[] horasInicio = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"};
        String[] horasFin = {"09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00"};

        for (int i = 1; i <= 13; i++) {
            bloques.add(new Bloque(i, horasInicio[i - 1], horasFin[i - 1]));
        }
        return bloques;
    }

    public List<Reservas> obtenerReservas(String fecha, Long salaId) {
        if (salaId != null) {
            return reservaRepository.findByFechaAndSalaIdAndEstado(fecha, salaId, "ACTIVA");
        }
        return reservaRepository.findByFechaAndEstado(fecha, "ACTIVA");
    }

    public List<Reservas> obtenerReservasMias(Long usuarioId, String fecha) {
        if (fecha != null && !fecha.isEmpty()) {
            return reservaRepository.findByUsuarioIdAndFechaAndEstado(usuarioId, fecha, "ACTIVA");
        }
        return reservaRepository.findByUsuarioIdAndEstado(usuarioId, "ACTIVA");
    }

    public Reservas crearReserva(Long usuarioId, String nombreRelator, Reservas nuevaReserva) {
        if (nuevaReserva.getSalaId() == null || nuevaReserva.getFecha() == null ||
                nuevaReserva.getBloqueInicio() == null || nuevaReserva.getBloqueFin() == null ||
                nuevaReserva.getActividad() == null || nuevaReserva.getActividad().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS");
        }

        Integer inicio = nuevaReserva.getBloqueInicio();
        Integer fin = nuevaReserva.getBloqueFin();
        if (inicio < 1 || inicio > 13 || fin < 1 || fin > 13 || fin < inicio) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BLOQUE_INVALIDO");
        }

        LocalDate fechaReserva = LocalDate.parse(nuevaReserva.getFecha());
        LocalDate hoy = LocalDate.now();
        if (fechaReserva.isBefore(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FECHA_PASADA");
        }

        // Validación real consultando al microservicio de salas (puerto 8082)
        String urlSala = "http://localhost:8082/api/salas/" + nuevaReserva.getSalaId();

        HttpHeaders headers = new HttpHeaders();
        String token = httpServletRequest.getHeader("Authorization");

        if (token != null && !token.isBlank()) {
            headers.set("Authorization", token);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<SalaDto> response = restTemplate.exchange(
                urlSala,
                HttpMethod.GET,
                entity,
                SalaDto.class
        );

        SalaDto sala = response.getBody();

        List<Reservas> cruces = reservaRepository.encontrarCruces(
                nuevaReserva.getSalaId(),
                nuevaReserva.getFecha(),
                inicio,
                fin
        );

        if (!cruces.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CRUCE_HORARIO");
        }

        nuevaReserva.setUsuarioId(usuarioId);
        nuevaReserva.setNombreRelator(nombreRelator);
        nuevaReserva.setEstado("ACTIVA");

        return reservaRepository.save(nuevaReserva);
    }

    public Reservas cancelarReserva(Long id, Long usuarioIdUsuarioActual, boolean esCoordinador) {
        Reservas reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "RESERVA_NO_ENCONTRADA"));

        if (!esCoordinador && !reserva.getUsuarioId().equals(usuarioIdUsuarioActual)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SIN_PERMISO");
        }

        reserva.setEstado("CANCELADA");
        return reservaRepository.save(reserva);
    }
}