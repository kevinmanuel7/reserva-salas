package cl.duoc.reservasalas.usuarios.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Datos que muestra Swagger y tipo de seguridad de la API.
 * - @OpenAPIDefinition: título y versión que aparecen arriba en la página.
 * - @SecurityScheme: declara que la API usa tokens JWT en el encabezado
 *   Authorization: Bearer. Con esto aparece el botón "Authorize".
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "ms-usuarios",
        version = "v1",
        description = "Inicio de sesión y renovación del token JWT"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}