package cl.duoc.reservasalas.usuarios.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import cl.duoc.reservasalas.usuarios.model.Usuario;

/**
 * Genera el token JWT que se entrega al iniciar sesión.
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long expiracionMinutos;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${jwt.expiration-minutos}") long expiracionMinutos) {
        if (expiracionMinutos <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION debe ser un número de minutos mayor que 0.");
        }
        this.jwtEncoder = jwtEncoder;
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();

        // Los "claims" son los datos que viajan dentro del token.
        // sub (subject) es el id del usuario; JWT exige que sea texto.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(expiracionMinutos, ChronoUnit.MINUTES))
                .build();

        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(encabezado, claims)).getTokenValue();
    }
}
