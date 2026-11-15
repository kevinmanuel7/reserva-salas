package cl.duoc.reservasalas.salas.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.reservasalas.salas.model.TipoSala;
import cl.duoc.reservasalas.salas.repository.SalaRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Al arrancar el servicio, carga las salas de prueba de docs/dataset/salas.json
 * (copiado al classpath por el pom.xml). Solo carga si la tabla sala está vacía,
 * para no duplicarlas al reiniciar.
 */
@Component
public class CargaSalasDataset implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaSalasDataset.class);
    private static final String RUTA_DATASET = "dataset/salas.json";

    record SalaDataset(
            Long id,
            String codigo,
            String nombre,
            TipoSala tipo,
            Integer capacidad,
            Boolean tieneProyector,
            Boolean tieneComputadores,
            Boolean tieneClimatizacion,
            Boolean activa
    ) {
    }

    private final SalaRepository salaRepository;
    private final ObjectMapper objectMapper;

    public CargaSalasDataset(SalaRepository salaRepository, ObjectMapper objectMapper) {
        this.salaRepository = salaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        if (salaRepository.count() > 0) {
            log.info("La tabla sala ya tiene datos; no se carga el dataset.");
            return;
        }

        List<SalaDataset> salas;
        try (InputStream json = new ClassPathResource(RUTA_DATASET).getInputStream()) {
            salas = objectMapper.readValue(json, new TypeReference<List<SalaDataset>>() { });
        }

        for (SalaDataset s : salas) {
            salaRepository.insertarConId(
                    s.id(),
                    s.codigo(),
                    s.nombre(),
                    s.tipo().name(),
                    s.capacidad(),
                    s.tieneProyector(),
                    s.tieneComputadores(),
                    s.tieneClimatizacion(),
                    s.activa()
            );
        }
        log.info("Dataset cargado: {} salas desde {}.", salas.size(), RUTA_DATASET);
    }
}
