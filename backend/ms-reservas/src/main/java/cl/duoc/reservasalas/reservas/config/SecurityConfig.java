package cl.duoc.reservasalas.reservas.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import cl.duoc.reservasalas.reservas.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Reglas de seguridad de ms-reservas:
 * - Sin sesiones: cada petición se identifica con su token.
 * - Rutas públicas: GET /api/v1/bloques, GET /api/v1/reservas/disponibilidad y Swagger.
 * - Todo lo demás exige el token Bearer del usuario.
 */
@Configuration
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final String[] origenesPermitidos;

    public SecurityConfig(ObjectMapper objectMapper,
                          @Value("${cors.origins}") String[] origenesPermitidos) {
        this.objectMapper = objectMapper;
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/bloques").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reservas/disponibilidad").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(recurso -> recurso
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(tokenInvalido())
                        .accessDeniedHandler(sinPermiso()))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(tokenInvalido())
                        .accessDeniedHandler(sinPermiso()));
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("rol");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(origenesPermitidos));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }

    private AuthenticationEntryPoint tokenInvalido() {
        return (request, response, excepcion) -> escribirError(response, HttpStatus.UNAUTHORIZED,
                new ErrorResponse("TOKEN_INVALIDO", "Debes iniciar sesión nuevamente"));
    }

    private AccessDeniedHandler sinPermiso() {
        return (request, response, excepcion) -> escribirError(response, HttpStatus.FORBIDDEN,
                new ErrorResponse("SIN_PERMISO", "No tienes permiso para realizar esta acción"));
    }

    private void escribirError(HttpServletResponse response, HttpStatus estado, ErrorResponse error)
            throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
