package cl.duoc.reservasalas.reservas.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import cl.duoc.reservasalas.reservas.exception.SalaInactivaException;
import cl.duoc.reservasalas.reservas.exception.SalaNoEncontradaException;

/**
 * Cliente HTTP para consultar a ms-salas si una sala existe y está activa.
 */
@Component
public class SalaClient {

    private static final Logger log = LoggerFactory.getLogger(SalaClient.class);

    public record SalaInfo(
            Long id,
            String codigo,
            String nombre,
            String tipo,
            Integer capacidad,
            Boolean tieneProyector,
            Boolean tieneComputadores,
            Boolean tieneClimatizacion,
            Boolean activa
    ) {
    }

    private final RestClient restClient;

    // El RestClient se arma aquí mismo con RestClient.builder(). En Spring Boot 4 el
    // RestClient.Builder ya no viene configurado por defecto con spring-boot-starter-webmvc,
    // así que pedirlo en el constructor hacía que ms-reservas no pudiera arrancar.
    public SalaClient(@Value("${servicios.ms-salas.url:http://localhost:8082}") String msSalasUrl) {
        this.restClient = RestClient.builder().baseUrl(msSalasUrl).build();
    }

    public SalaInfo obtenerYValidarSala(Long salaId) {
        try {
            SalaInfo sala = restClient.get()
                    .uri("/api/v1/salas/{id}", salaId)
                    .retrieve()
                    .body(SalaInfo.class);

            if (sala == null) {
                throw new SalaNoEncontradaException("No existe la sala con id " + salaId);
            }
            if (sala.activa() != null && !sala.activa()) {
                throw new SalaInactivaException("La sala está fuera de servicio");
            }
            return sala;
        } catch (HttpClientErrorException.NotFound e) {
            throw new SalaNoEncontradaException("No existe la sala con id " + salaId);
        } catch (SalaNoEncontradaException | SalaInactivaException e) {
            throw e;
        } catch (Exception e) {
            // ms-salas apagado o con error: no es que la sala no exista, es una falla del sistema.
            // Se lanza una excepción genérica para que el manejador global responda 500 ERROR_INTERNO.
            log.error("Error al consultar ms-salas para id {}: {}", salaId, e.getMessage());
            throw new IllegalStateException("No se pudo consultar ms-salas", e);
        }
    }
}