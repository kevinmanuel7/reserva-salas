package cl.duoc.reservasalas.reservas.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.reservasalas.reservas.model.Bloque;
import cl.duoc.reservasalas.reservas.repository.BloqueRepository;
import cl.duoc.reservasalas.reservas.repository.ReservaRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Al arrancar el servicio, carga los bloques y reservas de prueba de docs/dataset/
 * (copiados al classpath por el pom.xml). Solo carga si las tablas están vacías.
 */
@Component
public class CargaReservasDataset implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaReservasDataset.class);

    private static final String RUTA_BLOQUES = "dataset/bloques.json";
    private static final String RUTA_RESERVAS = "dataset/reservas.json";

    record BloqueDataset(Integer numero, String horaInicio, String horaFin) {
    }

    record ReservaDataset(
            Long id,
            LocalDate fecha,
            Long salaId,
            Integer bloqueInicio,
            Integer bloqueFin,
            Long usuarioId,
            String nombreRelator,
            String actividad,
            String estado
    ) {
    }

    private final BloqueRepository bloqueRepository;
    private final ReservaRepository reservaRepository;
    private final ObjectMapper objectMapper;

    public CargaReservasDataset(BloqueRepository bloqueRepository,
                                ReservaRepository reservaRepository,
                                ObjectMapper objectMapper) {
        this.bloqueRepository = bloqueRepository;
        this.reservaRepository = reservaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        cargarBloques();
        cargarReservas();
    }

    private void cargarBloques() throws IOException {
        if (bloqueRepository.count() > 0) {
            log.info("La tabla bloque ya tiene datos; no se carga el dataset.");
            return;
        }

        List<BloqueDataset> bloques;
        try (InputStream json = new ClassPathResource(RUTA_BLOQUES).getInputStream()) {
            bloques = objectMapper.readValue(json, new TypeReference<List<BloqueDataset>>() { });
        }

        for (BloqueDataset b : bloques) {
            bloqueRepository.save(new Bloque(b.numero(), b.horaInicio(), b.horaFin()));
        }
        log.info("Dataset cargado: {} bloques desde {}.", bloques.size(), RUTA_BLOQUES);
    }

    private void cargarReservas() throws IOException {
        if (reservaRepository.count() > 0) {
            log.info("La tabla reserva ya tiene datos; no se carga el dataset.");
            return;
        }

        List<ReservaDataset> reservas;
        try (InputStream json = new ClassPathResource(RUTA_RESERVAS).getInputStream()) {
            reservas = objectMapper.readValue(json, new TypeReference<List<ReservaDataset>>() { });
        }

        for (ReservaDataset r : reservas) {
            reservaRepository.insertarConId(
                    r.id(),
                    r.fecha(),
                    r.salaId(),
                    r.bloqueInicio(),
                    r.bloqueFin(),
                    r.usuarioId(),
                    r.nombreRelator(),
                    r.actividad(),
                    r.estado()
            );
        }
        log.info("Dataset cargado: {} reservas desde {}.", reservas.size(), RUTA_RESERVAS);
    }
}
