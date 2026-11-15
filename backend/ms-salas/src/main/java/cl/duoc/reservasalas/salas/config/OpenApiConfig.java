package cl.duoc.reservasalas.salas.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Datos que muestra Swagger y tipo de seguridad de la API.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "ms-salas",
        version = "v1",
        description = "Catálogo y administración de salas"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class OpenApiConfig {
}
