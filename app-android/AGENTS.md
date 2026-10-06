# AGENTS.md — app-android

Reglas para trabajar dentro de `app-android/`. Complementa al `AGENTS.md` de la raíz:
las reglas de negocio, de seguridad y de Git siguen siendo las de la raíz.

---

## 1. Tecnologías

- Lenguaje: **Kotlin**. No usar Java en la app.
- Interfaz: **Jetpack Compose** con Material Design 3. No usar archivos XML de
  diseño (layouts) ni Fragments.
- Navegación entre pantallas: Navigation Compose.
- Arquitectura: **MVVM** (Model – View – ViewModel), explicada en la sección 3.
- Llamadas al backend: Retrofit, con JSON.
- Base de datos local del teléfono: Room. Es una copia local: los repositorios piden
  al servidor, guardan la respuesta en Room y, sin conexión, leen de Room.
- Sesión (token JWT y usuario): se guarda en DataStore.
- Inyección de dependencias: manual, sin Hilt, para mantener el proyecto simple.
- Las versiones de las librerías están en `gradle/libs.versions.toml`.
  No agregar librerías nuevas sin explicar para qué se necesitan y esperar aprobación.

## 2. Versiones de Android

- `minSdk = 26` (Android 8.0). La app debe funcionar en teléfonos antiguos.
- Se eligió Android 8.0 porque desde esa versión está disponible `java.time`
  (por ejemplo `LocalDate` y `LocalTime`) sin configuración adicional.
- Antes de usar una función de Android, verificar que exista en Android 8.0.
  Si no existe, avisar en vez de subir el `minSdk`.
- La app se prueba en un teléfono con Android 12 (recursos limitados), un emulador
  reciente y un emulador con Android 8.0 para probar la versión mínima.

## 3. Estructura del código

Paquete base: `cl.duoc.reservasalas.app`

```
cl.duoc.reservasalas.app/
├── model/          ← Sala, Usuario, Bloque, Reserva (campos exactos de la sección 4 del plan)
├── data/
│   ├── local/      ← Room (entidades, DAO, base de datos) y DataStore
│   ├── remote/     ← interfaces de Retrofit
│   └── repository/ ← repositorios: único punto desde donde los ViewModel piden datos
├── viewmodel/      ← un ViewModel por pantalla
├── ui/
│   ├── screens/    ← las 8 pantallas del contrato de pantallas (sección 6 del plan)
│   ├── components/ ← piezas reutilizables
│   ├── navigation/ ← rutas, grafo de navegación y barra inferior
│   └── theme/      ← colores (modo claro y oscuro), tipografía y formas
└── notifications/  ← recordatorios locales
```

Reglas de MVVM:
- Una pantalla (`@Composable`) **solo muestra datos y avisa acciones** al ViewModel.
  No llama a Retrofit, a Room ni a repositorios.
- El **ViewModel** guarda el estado de la pantalla (cargando, con datos, vacío,
  error, guardando) y llama a los repositorios.
- El **repositorio** decide si los datos vienen del backend o de Room.
- Mientras el backend no esté listo, los repositorios entregan el dataset desde
  memoria (repositorio falso), según las fases de la sección 8 del plan.

## 4. Conexión con el backend

| Microservicio | Puerto |
|---------------|--------|
| ms-usuarios   | 8081   |
| ms-salas      | 8082   |
| ms-reservas   | 8083   |

- Retrofit se configura con una conexión por microservicio.
- Las URL base no se escriben fijas dentro del código: se definen como
  `buildConfigField` en `app/build.gradle.kts`.
- En el emulador, el computador se alcanza con la IP `10.0.2.2`.
  En un teléfono real se usa la IP local del computador en la red Wi-Fi.
- En desarrollo, el tráfico `http` sin cifrar se permite solo para la IP local,
  mediante la configuración de seguridad de red de la app.
- Toda petición, salvo `POST /auth/login`, envía el encabezado
  `Authorization: Bearer <token>`, agregado automáticamente.
- Si el backend responde 401, se borra la sesión y se vuelve a la pantalla de login.
- Los errores del servidor se convierten en el `mensaje` que muestra la interfaz.
  Nunca se muestra el error técnico al usuario.

## 5. Recordatorios y funcionalidades pendientes

- **Recordatorio:** notificación local, programada en el teléfono, 15 minutos antes
  del inicio de cada reserva (sala, horario y actividad). Se elimina si la reserva se
  cancela. Se usa WorkManager o AlarmManager (ver Fase 6 del plan). Desde Android 13
  se pide el permiso de notificaciones.

### Decisiones pendientes

No implementar estas partes sin que la decisión esté escrita aquí:

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

| Acción                               | Comando                    |
|--------------------------------------|----------------------------|
| Compilar la app                      | `./gradlew assembleDebug`  |
| Ejecutar las pruebas unitarias       | `./gradlew test`           |
| Instalar en el dispositivo conectado | `./gradlew installDebug`   |

Antes de dar una tarea por terminada, `./gradlew assembleDebug` y `./gradlew test`
deben terminar sin errores.
