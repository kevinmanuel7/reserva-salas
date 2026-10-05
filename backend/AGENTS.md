# AGENTS.md — backend

Reglas para trabajar dentro de `backend/`. Complementa al `AGENTS.md` de la raíz:
las reglas de negocio, de seguridad y de Git siguen siendo las de la raíz.

---

## 1. Organización

El backend está formado por microservicios. **Cada microservicio es un proyecto de
Spring Boot independiente, en su propia carpeta:**

```
backend/
├── AGENTS.md
├── CLAUDE.md
├── usuarios/        ← microservicio de usuarios y login
├── reservas/        ← microservicio de reservas y check-in
└── espacios/        ← microservicio de espacios, horarios y equipamiento
```

| Microservicio | Carpeta     | Responsable | Puerto local | Base de datos MySQL |
|---------------|-------------|-------------|--------------|---------------------|
| Usuarios      | `usuarios/` | Kevin       | 8081         | `usuarios_db`       |
| Reservas      | `reservas/` | Bastián     | 8082         | `reservas_db`       |
| Espacios      | `espacios/` | Bastián     | 8083         | `espacios_db`       |

- Un microservicio **no** debe leer ni escribir directamente en la base de datos de
  otro. Si necesita datos de otro microservicio, los pide por su API REST.
- No crear un microservicio nuevo sin que esté agregado a esta tabla.

## 2. Tecnologías

- Java con Spring Boot. Las versiones exactas están en el `pom.xml` de cada
  microservicio; no cambiarlas sin aviso.
- Herramienta de construcción: **Maven**, usando el wrapper incluido en cada proyecto
  (`mvnw` / `mvnw.cmd`). No depender de un Maven instalado en el computador.
- Base de datos: **MySQL**, con Spring Data JPA.
- Comunicación con la app Android: API REST con JSON.

## 3. Estructura interna de cada microservicio

Paquete base: `cl.duoc.reservasalas.<nombre-del-microservicio>`
(ejemplo: `cl.duoc.reservasalas.usuarios`).

Dentro del paquete base, separar por capas:

| Paquete       | Contenido                                                      |
|---------------|----------------------------------------------------------------|
| `controller`  | Clases `@RestController`: reciben las peticiones HTTP.         |
| `service`     | Lógica de negocio. Los controladores solo llaman a servicios.  |
| `repository`  | Interfaces de Spring Data JPA para acceder a MySQL.            |
| `model`       | Entidades JPA (`@Entity`), una por tabla.                      |
| `dto`         | Objetos que entran y salen por la API. Nunca devolver una entidad directamente. |
| `config`      | Configuración (seguridad, CORS, etc.).                         |
| `exception`   | Excepciones propias y el manejador global de errores.          |

## 4. Reglas de la API

- Todas las rutas comienzan con `/api/` (ejemplo: `/api/usuarios`).
- Usar los métodos HTTP según su significado: `GET` consulta, `POST` crea,
  `PUT` actualiza, `DELETE` elimina.
- Responder con el código HTTP correcto (200, 201, 400, 401, 403, 404, 409, 500)
  y, cuando hay error, con un JSON que incluya un mensaje entendible.
- Validar los datos de entrada en los DTO (Bean Validation) antes de llegar al servicio.
- Cada endpoint nuevo se documenta en `docs/` (ruta, método, cuerpo de entrada,
  respuesta y errores posibles). La app Android depende de esa documentación.

## 5. Configuración y secretos

- Los datos de conexión a MySQL se leen desde variables de entorno:
  `DB_URL`, `DB_USER`, `DB_PASSWORD`. Los datos de JWT, desde `JWT_SECRET` y
  `JWT_EXPIRATION` (ver sección 7).
- `application.properties` solo contiene referencias a esas variables
  (ejemplo: `spring.datasource.password=${DB_PASSWORD}`), nunca valores reales.
- Incluir un archivo `.env.example` con los nombres de las variables y valores vacíos.

## 6. Microservicio de usuarios (`usuarios/`)

- Gestiona el registro, el inicio de sesión y los datos de los usuarios.
- Perfiles posibles: `RELATOR` y `COORDINADOR` (no existen otros).
- Las contraseñas se guardan con hash BCrypt. Nunca en texto plano y nunca se
  devuelven en una respuesta.
- Es el **único** microservicio que emite tokens JWT.

## 7. Autenticación con JWT (aplica a los tres microservicios)

- Al iniciar sesión correctamente, `usuarios` devuelve un token JWT firmado que
  contiene el id del usuario y su perfil (`RELATOR` o `COORDINADOR`).
- La app envía ese token en cada petición, en el encabezado HTTP
  `Authorization: Bearer <token>`.
- `reservas` y `espacios` validan la firma del token por su cuenta, sin llamar a
  `usuarios` en cada petición.
- La clave de firma se lee desde la variable de entorno `JWT_SECRET` y debe ser la
  misma en los tres microservicios. Nunca se escribe en el código.
- El tiempo de expiración del token se lee desde la variable de entorno
  `JWT_EXPIRATION`.
- Las acciones de administración (editar horarios, equipamiento y comentarios)
  solo se permiten si el token tiene el perfil `COORDINADOR`.
- Rutas públicas (sin token): solo registro e inicio de sesión de `usuarios`.

## 8. Comandos

Ejecutar dentro de la carpeta del microservicio (ejemplo: `backend/usuarios/`).
En Windows usar `mvnw.cmd` en lugar de `./mvnw`.

| Acción               | Comando                    |
|----------------------|----------------------------|
| Compilar             | `./mvnw clean compile`     |
| Ejecutar las pruebas | `./mvnw test`              |
| Levantar el servicio | `./mvnw spring-boot:run`   |

## 9. Pruebas

- Toda lógica nueva en `service` debe tener pruebas unitarias.
- Antes de dar una tarea por terminada, `./mvnw test` debe pasar sin errores.
