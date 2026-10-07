package cl.duoc.reservasalas.usuarios.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.reservasalas.usuarios.model.Rol;
import cl.duoc.reservasalas.usuarios.repository.UsuarioRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Al arrancar el servicio, carga los usuarios de prueba de docs/dataset/usuarios.json
 * (copiado al classpath por el pom.xml). Solo carga si la tabla usuario está vacía,
 * para no duplicarlos al reiniciar.
 */
@Component
public class CargaUsuariosDataset implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaUsuariosDataset.class);
    private static final String RUTA_DATASET = "dataset/usuarios.json";

    // Forma de cada usuario en el JSON. No trae password: se agrega al cargar.
    record UsuarioDataset(Long id, String nombre, String email, Rol rol) {
    }

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final String passwordPrueba;

    public CargaUsuariosDataset(UsuarioRepository usuarioRepository,
                                PasswordEncoder passwordEncoder,
                                ObjectMapper objectMapper,
                                @Value("${dataset.password-prueba}") String passwordPrueba) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.passwordPrueba = passwordPrueba;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws IOException {
        if (usuarioRepository.count() > 0) {
            log.info("La tabla usuario ya tiene datos; no se carga el dataset.");
            return;
        }
        if (passwordPrueba == null || passwordPrueba.isBlank()) {
            throw new IllegalStateException(
                    "La tabla usuario está vacía y falta la variable de entorno PASSWORD_PRUEBA.");
        }

        List<UsuarioDataset> usuarios;
        try (InputStream json = new ClassPathResource(RUTA_DATASET).getInputStream()) {
            usuarios = objectMapper.readValue(json, new TypeReference<List<UsuarioDataset>>() { });
        }

        // Se calcula un hash por usuario: BCrypt usa una "sal" distinta cada vez,
        // así que dos usuarios con la misma contraseña quedan con hashes distintos.
        for (UsuarioDataset u : usuarios) {
            usuarioRepository.insertarConId(u.id(), u.nombre(), u.email(), u.rol().name(),
                    passwordEncoder.encode(passwordPrueba));
        }
        log.info("Dataset cargado: {} usuarios desde {}.", usuarios.size(), RUTA_DATASET);
    }
}
