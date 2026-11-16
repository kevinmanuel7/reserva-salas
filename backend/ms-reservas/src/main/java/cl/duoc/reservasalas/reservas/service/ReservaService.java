package cl.duoc.reservasalas.reservas.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.reservasalas.reservas.client.SalaClient;
import cl.duoc.reservasalas.reservas.dto.CrearReservaRequest;
import cl.duoc.reservasalas.reservas.dto.DisponibilidadDto;
import cl.duoc.reservasalas.reservas.dto.ReservaDto;
import cl.duoc.reservasalas.reservas.exception.BloqueInvalidoException;
import cl.duoc.reservasalas.reservas.exception.CruceHorarioException;
import cl.duoc.reservasalas.reservas.exception.DatosInvalidosException;
import cl.duoc.reservasalas.reservas.exception.FechaPasadaException;
import cl.duoc.reservasalas.reservas.exception.ReservaNoEncontradaException;
import cl.duoc.reservasalas.reservas.exception.SalaInactivaException;
import cl.duoc.reservasalas.reservas.exception.SalaNoEncontradaException;
import cl.duoc.reservasalas.reservas.exception.SinPermisoException;
import cl.duoc.reservasalas.reservas.exception.UsuarioOcupadoException;
import cl.duoc.reservasalas.reservas.model.EstadoReserva;
import cl.duoc.reservasalas.reservas.model.Reserva;
import cl.duoc.reservasalas.reservas.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final SalaClient salaClient;

    public ReservaService(ReservaRepository reservaRepository, SalaClient salaClient) {
        this.reservaRepository = reservaRepository;
        this.salaClient = salaClient;
    }

    @Transactional(readOnly = true)
    public List<ReservaDto> listarReservas(LocalDate fecha, Long salaId) {
        if (fecha == null) {
            throw new DatosInvalidosException("El parámetro fecha es obligatorio");
        }
        return reservaRepository.buscarActivasPorFechaYSala(fecha, salaId)
                .stream()
                .map(ReservaDto::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadDto> obtenerDisponibilidad(LocalDate fecha, Long salaId) {
        if (fecha == null) {
            throw new DatosInvalidosException("El parámetro fecha es obligatorio");
        }
        return reservaRepository.buscarActivasPorFechaYSala(fecha, salaId)
                .stream()
                .map(DisponibilidadDto::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaDto> listarMias(Long usuarioId, LocalDate fecha) {
        return reservaRepository.buscarMiasActivas(usuarioId, fecha)
                .stream()
                .map(ReservaDto::desde)
                .toList();
    }

    @Transactional
    public ReservaDto crearReserva(CrearReservaRequest request, Long usuarioId, String nombreRelator) {
        validarReservaIndividual(request, usuarioId, "");

        Reserva reserva = new Reserva(
                request.salaId(),
                usuarioId,
                nombreRelator,
                request.actividad(),
                request.fecha(),
                request.bloqueInicio(),
                request.bloqueFin(),
                EstadoReserva.ACTIVA
        );

        return ReservaDto.desde(reservaRepository.save(reserva));
    }

    @Transactional
    public List<ReservaDto> crearLote(List<CrearReservaRequest> lote, Long usuarioId, String nombreRelator) {
        // Validación 1 del lote: la lista viene y tiene al menos una reserva
        if (lote == null || lote.isEmpty()) {
            throw new DatosInvalidosException("La lista de reservas no puede estar vacía");
        }

        // Validación 2 del lote: cada reserva pasa las validaciones 1 a 7 de POST /reservas.
        // Se hace ANTES de comparar entre ellas, porque esa comparación necesita fecha y bloques.
        // El mensaje dice cuál reserva falló ("Reserva 2 de 3: ...").
        for (int i = 0; i < lote.size(); i++) {
            validarReservaIndividual(lote.get(i), usuarioId, prefijoLote(i, lote.size()));
        }

        // Validación 3 del lote: las reservas no se cruzan entre sí
        for (int i = 0; i < lote.size(); i++) {
            for (int j = i + 1; j < lote.size(); j++) {
                CrearReservaRequest r1 = lote.get(i);
                CrearReservaRequest r2 = lote.get(j);

                if (r1.fecha().equals(r2.fecha()) && seSuperponen(r1, r2)) {
                    String reservas = "Reservas " + (i + 1) + " y " + (j + 1) + " de " + lote.size() + ": ";
                    if (r1.salaId().equals(r2.salaId())) {
                        throw new CruceHorarioException(reservas + "se cruzan en la misma sala y horario");
                    } else {
                        throw new UsuarioOcupadoException(reservas + "no puedes estar en dos salas a la misma hora");
                    }
                }
            }
        }

        // Validación 4 del lote: todo bien → se guardan todas en una sola transacción (@Transactional)
        List<Reserva> entidades = new ArrayList<>();
        for (CrearReservaRequest request : lote) {
            entidades.add(new Reserva(
                    request.salaId(),
                    usuarioId,
                    nombreRelator,
                    request.actividad(),
                    request.fecha(),
                    request.bloqueInicio(),
                    request.bloqueFin(),
                    EstadoReserva.ACTIVA
            ));
        }

        List<Reserva> guardadas = reservaRepository.saveAll(entidades);
        return guardadas.stream().map(ReservaDto::desde).toList();
    }

    @Transactional
    public ReservaDto cancelarReserva(Long id, Long usuarioId, boolean esCoordinador) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaException("No existe la reserva con id " + id));

        if (!reserva.getUsuarioId().equals(usuarioId) && !esCoordinador) {
            throw new SinPermisoException("No tienes permiso para cancelar esta reserva");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        return ReservaDto.desde(reservaRepository.save(reserva));
    }

    // "Reserva 2 de 3: " para los mensajes de error del lote.
    private String prefijoLote(int indice, int total) {
        return "Reserva " + (indice + 1) + " de " + total + ": ";
    }

    // Validaciones 1 a 7 de POST /reservas, en el orden del contrato.
    // prefijo: "" en POST /reservas; "Reserva N de M: " en el lote, para saber cuál falló.
    private void validarReservaIndividual(CrearReservaRequest request, Long usuarioId, String prefijo) {
        // 1. Campos obligatorios
        if (request == null || request.salaId() == null || request.fecha() == null
                || request.bloqueInicio() == null || request.bloqueFin() == null
                || request.actividad() == null || request.actividad().isBlank()) {
            throw new DatosInvalidosException(prefijo + "Todos los campos son obligatorios y la actividad no puede estar vacía");
        }

        // 2. Bloques entre 1 y 13 y fin >= inicio
        if (request.bloqueInicio() < 1 || request.bloqueInicio() > 13
                || request.bloqueFin() < 1 || request.bloqueFin() > 13
                || request.bloqueFin() < request.bloqueInicio()) {
            throw new BloqueInvalidoException(prefijo + "Los bloques deben estar entre 1 y 13, y el bloque de término debe ser mayor o igual al de inicio");
        }

        // 3. Fecha no anterior a hoy
        if (request.fecha().isBefore(LocalDate.now())) {
            throw new FechaPasadaException(prefijo + "No se pueden realizar reservas en fechas pasadas");
        }

        // 4. Sala existe & 5. Sala activa (consulta a ms-salas)
        try {
            salaClient.obtenerYValidarSala(request.salaId());
        } catch (SalaNoEncontradaException e) {
            throw new SalaNoEncontradaException(prefijo + e.getMessage());
        } catch (SalaInactivaException e) {
            throw new SalaInactivaException(prefijo + e.getMessage());
        }

        // 6. Regla de cruce de horario (sala)
        List<Reserva> crucesSala = reservaRepository.buscarCrucesSala(
                request.salaId(), request.fecha(), request.bloqueInicio(), request.bloqueFin());
        if (!crucesSala.isEmpty()) {
            throw new CruceHorarioException(prefijo + "La sala ya está ocupada en alguno de esos bloques");
        }

        // 7. Regla de cruce del usuario
        List<Reserva> crucesUsuario = reservaRepository.buscarCrucesUsuario(
                usuarioId, request.fecha(), request.bloqueInicio(), request.bloqueFin());
        if (!crucesUsuario.isEmpty()) {
            throw new UsuarioOcupadoException(prefijo + "Ya tienes otra reserva en alguno de esos bloques");
        }
    }

    private boolean seSuperponen(CrearReservaRequest r1, CrearReservaRequest r2) {
        return r1.bloqueInicio() <= r2.bloqueFin() && r1.bloqueFin() >= r2.bloqueInicio();
    }
}
