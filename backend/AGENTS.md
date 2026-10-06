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
├── ms-usuarios/     ← login y usuarios
├── ms-salas/        ← catálogo de salas
└── ms-reservas/     ← bloques, reservas y validación de cruces
```

| Microservicio | Carpeta        | Responsable | Puerto local | Base de datos MySQL |
|---------------|----------------|-------------|--------------|---------------------|
| ms-usuarios   | `ms-usuarios/` | Kevin       | 8081         | `bd_usuarios`       |
| ms-salas      | `ms-salas/`    | Bastián     | 8082         | `bd_salas`          |
| ms-reservas   | `ms-reservas/` | Bastián     | 8083         | `bd_reservas`       |

- Un microservicio **no** debe leer ni escribir directamente en la base de datos de
  otro. Si necesita datos de otro microservicio, los pide por su API REST
  (ejemplo: `ms-reservas` consulta a `ms-salas` para verificar que la sala existe).
- No crear un microservicio nuevo sin que esté agregado a esta tabla.

## 2. Tecnologías

- Java con Spring Boot. Las versiones exactas están en el `pom.xml` de cada
  microservicio; no cambiarlas sin aviso.
- Dependencias base de cada proyecto: Spring Web, Spring Data JPA, MySQL Driver,
  Validation y Spring Security.
- Herramienta de construcción: **Maven**, usando el wrapper incluido en cada proyecto
  (`mvnw` / `mvnw.cmd`). No depender de un Maven instalado en el computador.
- Base de datos: **MySQL**, con Spring Data JPA.
- Comunicación con la app Android: API REST con JSON.

## 3. Estructura interna de cada microservicio

Paquete base: `cl.duoc.reservasalas.<nombre>` (ejemplo: `cl.duoc.reservasalas.usuarios`).
El paquete no lleva el prefijo `ms-` porque Java no permite guiones en los nombres
de paquete.

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

- Las rutas, los cuerpos, las respuestas y el formato de error son los del
  Contrato 1 (sección 5 de `docs/plan-proyecto.md`). No crear rutas fuera del
  contrato sin aviso.
- Los nombres de campo del JSON son exactamente los del modelo de datos
  (sección 4 del plan).
- Usar los métodos HTTP según su significado: `GET` consulta, `POST` crea,
  `PUT` modifica.
- Responder con el código HTTP que indica el contrato (200, 201, 400, 401, 403,
  404, 409). Un cruce de horario responde `409`.
- Cada microservicio tiene un manejador global de errores que responde siempre con
  el formato de error del contrato.
- Validar los datos de entrada en los DTO (Bean Validation) antes de llegar al servicio.
- Al arrancar, cada microservicio carga sus datos desde `docs/dataset/`.

## 5. Configuración y secretos

- Los datos de conexión a MySQL se leen desde variables de entorno:
  `DB_URL`, `DB_USER`, `DB_PASSWORD`. Los datos de JWT, desde `JWT_SECRET` y
  `JWT_EXPIRATION` (ver sección 7).
- `application.properties` solo contiene referencias a esas variables
  (ejemplo: `spring.datasource.password=${DB_PASSWORD}`), nunca valores reales.
- Incluir un archivo `.env.example` con los nombres de las variables y valores vacíos.

### Nombres de tablas y columnas (los tres microservicios)

- En `application.properties` de cada microservicio usar:
  `spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl`.
  Sin esto, Spring cambia `bloqueInicio` por `bloque_inicio` en MySQL y rompe la
  regla de nombres idénticos del modelo de datos.
- Cada entidad declara el nombre de su tabla de forma explícita y en minúsculas,
  con `@Table(name = "...")` (ejemplo: `@Table(name = "usuario")`). Con la estrategia
  anterior, sin `@Table` la tabla se llamaría `Usuario`, y en MySQL sobre Linux los
  nombres de tabla distinguen mayúsculas.

## 6. Microservicio de usuarios (`ms-usuarios/`)

- Gestiona el inicio de sesión y los datos de los usuarios.
- Perfiles posibles: `RELATOR` y `COORDINADOR` (no existen otros).
- Las contraseñas se guardan con hash BCrypt. Nunca en texto plano y nunca se
  devuelven en una respuesta.
- Es el **único** microservicio que emite tokens JWT.

## 7. Autenticación con JWT (aplica a los tres microservicios)

- Al iniciar sesión correctamente con `POST /auth/login`, `ms-usuarios` devuelve un
  token JWT firmado que contiene el id, el nombre y el rol del usuario.
- La app envía ese token en cada petición, en el encabezado HTTP
  `Authorization: Bearer <token>`.
- `ms-salas` y `ms-reservas` validan la firma del token por su cuenta, sin llamar a
  `ms-usuarios` en cada petición.
- La clave de firma se lee desde la variable de entorno `JWT_SECRET` y debe ser la
  misma en los tres microservicios. Nunca se escribe en el código.
- El tiempo de expiración del token se lee desde la variable de entorno
  `JWT_EXPIRATION`.
- Las acciones de administración solo se permiten si el token tiene el rol
  `COORDINADOR`; si no, se responde `403`.
- Única ruta pública (sin token): `POST /auth/login`.

## 8. Comandos

Ejecutar dentro de la carpeta del microservicio (ejemplo: `backend/ms-usuarios/`).
En Windows usar `mvnw.cmd` en lugar de `./mvnw`.

| Acción               | Comando                    |
|----------------------|----------------------------|
| Compilar             | `./mvnw clean compile`     |
| Ejecutar las pruebas | `./mvnw test`              |
| Levantar el servicio | `./mvnw spring-boot:run`   |

## 9. Pruebas

- Toda lógica nueva en `service` debe tener pruebas unitarias.
- Los casos de prueba de la API están en la sección 7 del plan y en la colección de
  Postman de `docs/postman/`.
- Antes de dar una tarea por terminada, `./mvnw test` debe pasar sin errores.
