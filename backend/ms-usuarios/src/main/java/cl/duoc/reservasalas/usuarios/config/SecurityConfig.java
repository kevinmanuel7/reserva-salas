package cl.duoc.reservasalas.usuarios.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;

import cl.duoc.reservasalas.usuarios.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Reglas de seguridad del servicio:
 * - Sin sesiones: cada petición se identifica solo con su token.
 * - POST /auth/login es la única ruta pública; todo lo demás exige
 *   el encabezado Authorization: Bearer <token>.
 * Al definir esta configuración, Spring deja de crear el usuario "user" con la
 * contraseña generada que mostraba en consola.
 */
@Configuration
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protege formularios con cookies de sesión; aquí no hay cookies ni sesiones.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .anyRequest().authenticated())
                // Valida el token con el JwtDecoder de JwtConfig.
                .oauth2ResourceServer(recurso -> recurso
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(tokenInvalido())
                        .accessDeniedHandler(sinPermiso()))
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint(tokenInvalido())
                        .accessDeniedHandler(sinPermiso()));
        return http.build();
    }

    // Falta el token, está alterado o venció → 401 TOKEN_INVALIDO.
    private AuthenticationEntryPoint tokenInvalido() {
        return (request, response, excepcion) -> escribirError(response, HttpStatus.UNAUTHORIZED,
                new ErrorResponse("TOKEN_INVALIDO", "Debes iniciar sesión nuevamente"));
    }

    // El rol del token no permite la acción → 403 SIN_PERMISO.
    private AccessDeniedHandler sinPermiso() {
        return (request, response, excepcion) -> escribirError(response, HttpStatus.FORBIDDEN,
                new ErrorResponse("SIN_PERMISO", "No tienes permiso para realizar esta acción"));
    }

    // Estos errores ocurren antes de llegar a un controlador, por eso el manejador
    // global no los ve y el JSON se escribe aquí a mano.
    private void escribirError(HttpServletResponse response, HttpStatus estado, ErrorResponse error)
            throws IOException {
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
