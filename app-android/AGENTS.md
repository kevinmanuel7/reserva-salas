# AGENTS.md — app-android

Reglas para trabajar dentro de `app-android/`. Complementa al `AGENTS.md` de la raíz:
las reglas de negocio, de seguridad y de Git siguen siendo las de la raíz.

---

## 1. Tecnologías

- Lenguaje: **Kotlin**. No usar Java en la app.
- Interfaz: **Jetpack Compose**. No usar archivos XML de diseño (layouts) ni Fragments.
- Navegación entre pantallas: Navigation Compose.
- Arquitectura: **MVVM** (Model – View – ViewModel), explicada en la sección 3.
- Llamadas al backend: Retrofit, con JSON.
- Base de datos local del teléfono: Room. Qué datos se guardan localmente: PENDIENTE.
- Token de sesión (JWT): se guarda en DataStore.
- Inyección de dependencias: manual, sin Hilt, para mantener el proyecto simple.
- Las versiones de las librerías están en `gradle/libs.versions.toml`.
  No agregar librerías nuevas sin explicar para qué se necesitan y esperar aprobación.

## 2. Versiones de Android

- `minSdk = 26` (Android 8.0). La app debe funcionar en teléfonos antiguos.
- Antes de usar una función de Android, verificar que exista en Android 8.0.
  Si no existe, avisar en vez de subir el `minSdk`.
- La app se prueba también en un teléfono real con Android 12, para medir el
  rendimiento en equipos limitados.

## 3. Estructura del código

Paquete base: `cl.duoc.reservasalas.app`

| Paquete           | Contenido                                                          |
|-------------------|--------------------------------------------------------------------|
| `data/remote`     | Interfaces de Retrofit y DTO que llegan desde el backend.          |
| `data/local`      | Room (entidades, DAO, base de datos) y DataStore.                  |
| `data/repository` | Repositorios: único punto desde donde los ViewModel piden datos.   |
| `domain/model`    | Clases de la app (Usuario, Espacio, Reserva) usadas por la interfaz. |
| `ui/<funcionalidad>` | Una carpeta por funcionalidad: pantallas (`...Screen.kt`) y su `...ViewModel.kt`. |
| `ui/navigation`   | Rutas y grafo de navegación.                                       |
| `ui/theme`        | Colores, tipografía y tema de la app.                              |

Funcionalidades previstas en `ui/`: `login`, `espacios`, `reservas`, `checkin`, `admin`.

Reglas de MVVM:
- Una pantalla (`@Composable`) **solo muestra datos y avisa acciones** al ViewModel.
  No llama a Retrofit, a Room ni a repositorios.
- El **ViewModel** guarda el estado de la pantalla y llama a los repositorios.
- El **repositorio** decide si los datos vienen del backend o de Room.
- Los DTO del backend no se usan directamente en la interfaz: se convierten a las
  clases de `domain/model`.

## 4. Conexión con el backend

| Microservicio | Puerto |
|---------------|--------|
| usuarios      | 8081   |
| reservas      | 8082   |
| espacios      | 8083   |

- Las URL base no se escriben fijas dentro del código: se definen como
  `buildConfigField` en `app/build.gradle.kts`.
- En el emulador, el computador se alcanza con la IP `10.0.2.2`.
  En un teléfono real se usa la IP local del computador en la red Wi-Fi.
- Toda petición, salvo registro e inicio de sesión, envía el encabezado
  `Authorization: Bearer <token>`.
- Si el backend responde 401, se borra el token y se vuelve a la pantalla de login.
- Mientras se espera una respuesta, la pantalla muestra un indicador de carga.
  Si hay error, muestra un mensaje entendible para el usuario, nunca el error técnico.

## 5. Funcionalidades con decisiones pendientes

No implementar estas partes sin que la decisión esté escrita aquí:

- **Recordatorio 15 minutos antes de la reserva:** PENDIENTE decidir si se programa
  en el teléfono o lo envía el backend.
- **Aviso al coordinador cuando un relator hace check-in:** PENDIENTE decidir cómo
  llega la notificación al teléfono del coordinador.
- **Lectura del código QR para el check-in:** PENDIENTE elegir librería.

## 6. Cómo explicar el código de Android

El equipo está aprendiendo Kotlin y Compose desde cero. Además de lo indicado en la
raíz, al crear o modificar código de la app:

- Explicar cada sintaxis de Kotlin la primera vez que aparece (por ejemplo `val`,
  `?`, `?.`, `data class`, lambdas, `suspend`).
- Explicar qué hace cada función `@Composable` nueva y cómo se maneja su estado
  (`remember`, `mutableStateOf`, `StateFlow`).
- Indicar siempre en qué archivo y en qué paquete va cada cosa, y por qué.

## 7. Comandos

Ejecutar dentro de `app-android/`. En Windows usar `gradlew.bat` en lugar de `./gradlew`.

| Acción                          | Comando                    |
|---------------------------------|----------------------------|
| Compilar la app                 | `./gradlew assembleDebug`  |
| Ejecutar las pruebas unitarias  | `./gradlew test`           |
| Instalar en el dispositivo conectado | `./gradlew installDebug` |

Antes de dar una tarea por terminada, `./gradlew assembleDebug` y `./gradlew test`
deben terminar sin errores.
