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
- El dispositivo llega al backend del computador con `adb reverse`, que redirige un
  puerto del dispositivo al mismo puerto del computador:
  `adb reverse tcp:8081 tcp:8081`.
- `adb reverse` funciona igual en un teléfono físico conectado por USB y en un
  emulador. Por eso la URL de ms-usuarios en la app es `http://localhost:8081` en
  ambos casos, y no se usa `10.0.2.2`.
- El comando `adb reverse` se ejecuta cada vez que se conecta el teléfono o se
  inicia el emulador.
- En desarrollo, el tráfico `http` sin cifrar se permite solo para `localhost`,
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

Ejecutar dentro de `app-android/` (Windows, PowerShell).

| Acción                               | Comando                         |
|--------------------------------------|---------------------------------|
| Compilar la app                      | `.\gradlew.bat assembleDebug`   |
| Ejecutar las pruebas unitarias       | `.\gradlew.bat test`            |
| Instalar en el teléfono conectado    | `.\gradlew.bat installDebug`    |

Antes de dar una tarea por terminada, `.\gradlew.bat assembleDebug` y
`.\gradlew.bat test` deben terminar sin errores.

## Etapa maqueta

Esta sección manda sobre cualquier otra regla de este archivo mientras dure la etapa maqueta.
Si algo de este archivo contradice esta sección, se sigue esta sección.

### Objetivo
Presentar el front completo de la app funcionando con datos falsos. No hay backend en esta etapa.

### Referencias obligatorias
- Especificación de cada pantalla, elemento por elemento: `docs/maqueta/especificacion.md`
- Imagen de cada pantalla: `docs/maqueta/pantallas/<persona>/`
- Antes de programar una pantalla, lee su sección en la especificación y mira su imagen.
  La pantalla debe verse igual a la imagen.

### Sin red
- La app no hace llamadas de red. No usar Retrofit, ApiService ni los DTO del login.
- Ese código existe en el proyecto pero queda desconectado: no se borra ni se modifica.
- Todos los datos vienen de los repositorios falsos en `data/falso/`, incluido el login.

### Estructura de carpetas
Rutas relativas a `app/src/main/java/cl/duoc/reservasalas/app/`.

| Carpeta | Contenido | Quién la modifica |
| --- | --- | --- |
| `ui/theme/` | Colores y tema | Solo Kevin |
| `ui/componentes/` | Componentes compartidos | Solo Kevin |
| `ui/navegacion/` | Rutas y NavHost | Solo Kevin |
| `data/modelo/` | Sala, Equipo, Reserva, EstadoReserva | Solo Kevin |
| `data/falso/` | Repositorios falsos y sus datos | Solo Kevin |
| `ui/pantallas/<nombre>/` | Una carpeta por pantalla: `<Nombre>Pantalla.kt` y `<Nombre>ViewModel.kt` | El dueño de la pantalla |

Si una pantalla necesita un cambio en una carpeta de "Solo Kevin", no lo hagas:
avisa al usuario que debe pedírselo a Kevin.

### Reglas de diseño
- Usar solo los colores de `ui/theme/`. Prohibido escribir colores directamente (ej. `Color(0xFF...)`) dentro de una pantalla.
- Usar solo los componentes de `ui/componentes/`: BarraSuperior, BarraInferior, BotonPrincipal,
  BotonSecundario, BotonPeligro, TarjetaSala, ChipEstado, ChipEquipo, TarjetaMetrica, AvisoDestacado.
- Prohibido crear botones, tarjetas o chips propios dentro de una pantalla.
- Si un componente todavía no existe en `ui/componentes/`, usar el equivalente de Material 3
  y dejar el comentario `// TODO: reemplazar por <NombreComponente>`.
- Solo modo claro y orientación vertical. Todos los textos en español.

### Reglas de cada pantalla
- Arquitectura MVVM: la pantalla solo dibuja; los datos y la lógica van en su ViewModel.
- El ViewModel obtiene los datos de un repositorio falso de `data/falso/`.
  Prohibido escribir datos de ejemplo dentro de la pantalla o del ViewModel.
- Cada pantalla tiene al menos una `@Preview`.
- Cada lista tiene su mensaje para el caso vacío (sin datos).
- Navegar solo con los nombres de ruta de la tabla de abajo. Prohibido inventar rutas nuevas.

### Rutas definitivas
| Ruta | Pantalla | Dueño |
| --- | --- | --- |
| login | Login | Kevin |
| perfil | Perfil | Kevin |
| checkin/{idReserva} | Check-in con cámara | Kevin |
| inicioRelator | Inicio del relator | Simón |
| salas | Lista de salas | Simón |
| sala/{codigo} | Detalle de sala | Simón |
| reservar/{codigo} | Reservar | Simón |
| reservaConfirmada/{idReserva} | Reserva confirmada | Simón |
| agenda | Mi agenda | Simón |
| reserva/{idReserva} | Detalle de reserva | Simón |
| inicioCoordinador | Inicio del coordinador | Bastián |
| reservasDia | Reservas del día | Bastián |
| supervisar/{idReserva} | Supervisar reserva | Bastián |
| editarSala/{codigo} | Editar sala | Bastián |
| horarioGeneral | Horario general | Bastián |

`{codigo}` es el código de la sala (ej. S-201). `{idReserva}` es el número de la reserva.

### Horarios
Bloques con 5 minutos de pausa: 08:00–09:00, 09:05–10:00, 10:05–11:00 … 20:05–21:00 (13 bloques).
Una reserva de varios bloques se muestra de corrido (ej. 10:05–12:00).

### Flujo Git en esta etapa
- Cada integrante trabaja solo en su rama personal: `feature/kevin`, `feature/simon`, `feature/bastian`.
- Nunca hacer commit, merge ni push a `develop` ni a `main`. Solo Kevin fusiona en `develop`.
- Para recibir la base nueva de Kevin: `git pull origin develop` estando en la rama propia.
- Antes de cada commit, verificar la rama actual con `git status`.
- Commits en español, verbo en presente (ej. "Agrega la lista de salas").
- Avisar a Kevin cada vez que una pantalla esté terminada, para que la fusione.

### Una pantalla está terminada cuando
- Se ve igual a su imagen en `docs/maqueta/pantallas/`.
- Usa solo colores del tema y componentes compartidos (o tiene el TODO si el componente aún no existe).
- Muestra datos de los repositorios falsos.
- Navega a las rutas de la tabla.
- Tiene mensaje para el caso vacío.
- Se probó en un teléfono o emulador, no solo en la vista previa.