package cl.duoc.reservasalas.usuarios.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Crea las piezas que firman (JwtEncoder) y verifican (JwtDecoder) los tokens.
 * Ambas usan la misma clave JWT_SECRET con el algoritmo HS256.
 */
@Configuration
public class JwtConfig {

    // HS256 exige una clave de al menos 256 bits = 32 bytes.
    private static final int LARGO_MINIMO_CLAVE = 32;

    private final SecretKey clave;

    public JwtConfig(@Value("${jwt.secret}") String secreto) {
        // La clave se toma como texto UTF-8 (no Base64), igual en los tres microservicios.
        byte[] bytes = secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < LARGO_MINIMO_CLAVE) {
            throw new IllegalStateException(
                    "La variable de entorno JWT_SECRET falta o tiene menos de 32 caracteres.");
        }
        this.clave = new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    // Spring Security usa este decoder para validar el encabezado Authorization: Bearer.
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
