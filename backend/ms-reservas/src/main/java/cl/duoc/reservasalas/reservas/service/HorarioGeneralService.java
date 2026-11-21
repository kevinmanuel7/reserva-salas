package cl.duoc.reservasalas.reservas.service;

import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.reservasalas.reservas.dto.ActualizarHorarioRequest;
import cl.duoc.reservasalas.reservas.dto.HorarioGeneralDto;
import cl.duoc.reservasalas.reservas.exception.DatosInvalidosException;
import cl.duoc.reservasalas.reservas.exception.SinPermisoException;
import cl.duoc.reservasalas.reservas.model.HorarioGeneral;
import cl.duoc.reservasalas.reservas.repository.HorarioGeneralRepository;

/**
 * Lógica del horario general (Contrato 1 versión 7, sección 5 de docs/plan-proyecto.md).
 * Todavía no se usa para validar reservas: las reservas siguen usando los bloques.
 */
@Service
public class HorarioGeneralService {

    // Límites del horario: ninguna hora puede quedar fuera de 08:00–21:00.
    private static final LocalTime HORA_MINIMA = LocalTime.of(8, 0);
    private static final LocalTime HORA_MAXIMA = LocalTime.of(21, 0);

    private final HorarioGeneralRepository horarioGeneralRepository;

    public HorarioGeneralService(HorarioGeneralRepository horarioGeneralRepository) {
        this.horarioGeneralRepository = horarioGeneralRepository;
    }

    @Transactional
    public HorarioGeneralDto obtener() {
        return HorarioGeneralDto.desde(buscarOCrear());
    }

    @Transactional
    public HorarioGeneralDto actualizar(ActualizarHorarioRequest request, boolean esCoordinador) {
        if (!esCoordinador) {
            throw new SinPermisoException("Solo el coordinador puede editar el horario general");
        }
        validarHoraExacta(request.apertura(), "apertura");
        validarHoraExacta(request.cierre(), "cierre");
        if (!request.apertura().isBefore(request.cierre())) {
            throw new DatosInvalidosException("La hora de apertura debe ser anterior a la hora de cierre");
        }

        HorarioGeneral horario = buscarOCrear();
        horario.setApertura(request.apertura());
        horario.setCierre(request.cierre());
        horario.setPausaMinutos(request.pausaMinutos());
        // Sin días repetidos y en orden de la semana (lunes primero).
        horario.setDias(request.dias().stream().distinct().sorted().toList());
        return HorarioGeneralDto.desde(horarioGeneralRepository.save(horario));
    }

    /**
     * Se llama al arrancar el servicio: si la tabla está vacía, inserta la fila con
     * los valores por defecto. Devuelve true si la creó.
     */
    @Transactional
    public boolean crearPorDefectoSiFalta() {
        if (horarioGeneralRepository.count() > 0) {
            return false;
        }
        horarioGeneralRepository.save(HorarioGeneral.porDefecto());
        return true;
    }

    private HorarioGeneral buscarOCrear() {
        return horarioGeneralRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> horarioGeneralRepository.save(HorarioGeneral.porDefecto()));
    }

    // Una hora válida es exacta (minutos en 0) y está entre 08:00 y 21:00.
    private void validarHoraExacta(LocalTime hora, String nombre) {
        if (hora.getMinute() != 0 || hora.getSecond() != 0 || hora.getNano() != 0) {
            throw new DatosInvalidosException("La hora de " + nombre + " debe ser exacta (minutos en 00)");
        }
        if (hora.isBefore(HORA_MINIMA) || hora.isAfter(HORA_MAXIMA)) {
            throw new DatosInvalidosException("La hora de " + nombre + " debe estar entre 08:00 y 21:00");
        }
    }
}
