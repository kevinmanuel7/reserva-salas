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
├── AppContainer.kt   ← inyección manual: crea Retrofit, repositorios y reloj
├── model/            ← Usuario, Rol y Sesion (los del login)
├── data/
│   ├── modelo/       ← Sala, Equipo, Bloque, HorarioGeneral, Reserva y formatos
│   ├── local/        ← DataStore de la sesión, del perfil (foto) y de los recordatorios, archivos de la foto (y Room cuando se agregue)
│   ├── remote/       ← interfaces de Retrofit, TokenInterceptor y dto/ (JSON del backend)
│   ├── repository/   ← interfaces de repositorios y repositorios remotos: único punto
│   │                   desde donde los ViewModel piden datos
│   └── falso/        ← repositorios falsos y reloj fijo: solo para pruebas unitarias
├── ui/
│   ├── pantallas/<nombre>/ ← <Nombre>Pantalla.kt y <Nombre>ViewModel.kt
│   ├── componentes/  ← piezas reutilizables
│   ├── navegacion/   ← rutas y NavHost (incluye la barra inferior)
│   └── theme/        ← colores, tipografía y formas
├── ui/screens/ y viewmodel/ ← pantalla y ViewModel del login
└── notificaciones/   ← recordatorio local 15 minutos antes de cada reserva (AlarmManager)
```

Reglas de MVVM:
- Una pantalla (`@Composable`) **solo muestra datos y avisa acciones** al ViewModel.
  No llama a Retrofit, a Room ni a repositorios.
- El **ViewModel** guarda el estado de la pantalla (cargando, con datos, vacío,
  error, guardando) y llama a los repositorios.
- El **repositorio** decide si los datos vienen del backend o de Room.
- Los repositorios remotos de `data/repository/` piden los datos al backend real
  (ver «Etapa integración»).

## 4. Conexión con el backend

| Microservicio | Puerto |
|---------------|--------|
| ms-usuarios   | 8081   |
| ms-salas      | 8082   |
| ms-reservas   | 8083   |

- Retrofit se configura con una conexión por microservicio.
- Contrato 1 versión 2: las URL base terminan en `/api/v1/` (ejemplo:
  `http://localhost:8081/api/v1/`), así las rutas de las interfaces Retrofit
  (`auth/login`, `salas`...) no llevan el prefijo.
- Las URL base no se escriben fijas dentro del código: se definen como
  `buildConfigField` en `app/build.gradle.kts` (`URL_MS_USUARIOS`, `URL_MS_SALAS`,
  `URL_MS_RESERVAS`). En desarrollo apuntan a `localhost`; en la entrega las tres
  apuntan a `http://34.194.115.43/api/v1/` (Nginx en AWS).
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
- Toda petición con sesión iniciada envía el encabezado
  `Authorization: Bearer <token>`, que agrega automáticamente
  `data/remote/TokenInterceptor.kt`. Sin sesión (por ejemplo, en el login) la
  petición sale sin el encabezado.
- Si el backend responde 401, se borra la sesión y se vuelve a la pantalla de login.
- Los errores del servidor se convierten en el `mensaje` que muestra la interfaz.
  Nunca se muestra el error técnico al usuario.

## 5. Recordatorios y funcionalidades pendientes

- **Recordatorio** (segundo recurso nativo de la pauta): notificación local, programada en
  el teléfono con **AlarmManager** (alarma exacta, sin librerías extra), `Reserva.MINUTOS_RECORDATORIO`
  minutos antes del inicio de cada reserva. Funciona aunque la app esté cerrada.
  - **Por qué AlarmManager y no WorkManager:** en el teléfono de prueba (Android 12) las tareas de
    WorkManager quedaban en ENQUEUED y no se ejecutaban a la hora (WorkManager no garantiza la hora
    y el fabricante las atrasa). Una alarma exacta suena a la hora pedida.
  - **Por qué `setAlarmClock` y no `setExactAndAllowWhileIdle`:** en el mismo teléfono (OPPO,
    ColorOS) `canScheduleExactAlarms()` daba true y la app pedía `setExactAndAllowWhileIdle` sin
    error, pero ColorOS la convertía en inexacta (`dumpsys alarm`: `windowLength 3600000`, una
    ventana de 1 hora). `setAlarmClock` es la alarma tipo despertador: ningún sistema la atrasa
    (`windowLength 0`). Efecto visible: ícono de reloj en la barra de estado mientras haya un
    recordatorio pendiente, y "próxima alarma" en la pantalla de bloqueo; al tocarla abre la app.
  - **Cuándo:** solo reservas del usuario con sesión, en estado RESERVADA y cuyo aviso
    (`Reserva.momentoRecordatorio`) todavía no llega: regla `Reserva.necesitaRecordatorio(ahora)`.
  - **Contenido:** título "Tu reserva empieza en 15 minutos" y texto
    "Sala 101 · 10:05–11:00 · Excel intermedio" (sala, horario y actividad). Al tocarla abre la app.
    Canal "Recordatorios de reservas", creado en `ReservaSalasApp.onCreate`.
  - **Archivos** (paquete `notificaciones/`):
    - `CanalRecordatorios.kt`: constantes, creación del canal, `textoRecordatorio(...)` e
      `intentAbrirApp(...)` (abre `MainActivity`; lo usan la notificación y `setAlarmClock`).
    - `PermisoAlarmasExactas.kt`: `puedeProgramarAlarmasExactas(context)` (true en Android 11 o
      menos, o si `canScheduleExactAlarms()`) e `intentPermisoAlarmasExactas(context)`
      (`Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` con el paquete de la app).
    - `RecordatorioReceiver.kt`: `BroadcastReceiver` que despierta la alarma y muestra la
      notificación. Sin permiso, no hace nada. Registrado en el manifiesto con `exported="false"`.
    - `ProgramadorRecordatorios.kt`: interfaz (`programar`, `cancelar`, `cancelarTodos`) y su
      versión con alarmas, `ProgramadorRecordatoriosAlarmas`: un `PendingIntent` por reserva
      (`requestCode` = id de la reserva, `FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT`) hacia
      `RecordatorioReceiver`, con el id y el texto como extras. Usa `setAlarmClock(...)`; si en
      Android 12 o superior `canScheduleExactAlarms()` es false (o lanza `SecurityException`), usa
      `setAndAllowWhileIdle` (puede atrasarse hasta una hora).
    - `data/local/RecordatoriosDataStore.kt`: DataStore **"recordatorios"** con los ids programados.
      AlarmManager no puede listar las alarmas de la app, así que `cancelarTodos()` los lee de ahí.
    - `SincronizadorRecordatorios.kt`: observa `repositorioReservas.reservas` y la sesión; programa
      las que cumplen la regla y borra las que dejaron de estar RESERVADA (cancelada, check-in, no
      presentado). Una reserva que solo no aparece en la lista no se borra. Se inicia una vez en
      `ReservaSalasApp`. No usa Android: se prueba con un programador falso
      (`SincronizadorRecordatoriosTest`). `reprogramarTodo()` vuelve a programar todas las
      reservas que necesitan aviso con la última lista y sesión recibidas (`@Synchronized`).
  - **Cerrar sesión** (`PerfilViewModel.cerrarSesion`) llama `cancelarTodos()`. Si la sesión
    queda en null por otra causa (ej. 401), el sincronizador también borra todo.
  - **Permisos:** `POST_NOTIFICATIONS` en el manifiesto. Desde Android 13 se pide al abrir Inicio
    del relator (`PedirPermisoNotificaciones` en `InicioRelatorPantalla.kt`); en Android 12 o menos
    ese permiso no existe y no se revisa. Si se rechaza, la app funciona igual, sin recordatorios.
    Alarmas exactas: `SCHEDULE_EXACT_ALARM` (con `maxSdkVersion="32"`, para Android 12) y
    `USE_EXACT_ALARM` (Android 13 o superior).
  - **Pedir el permiso de alarmas exactas:** en Android 12 o superior, si
    `canScheduleExactAlarms()` es false, Inicio del relator muestra un diálogo
    (`PedirPermisoAlarmasExactas` en `InicioRelatorPantalla.kt`) que explica para qué sirve
    ("para avisarte 15 minutos antes de tu reserva"). «Permitir» abre los ajustes; «Ahora no»
    deja el respaldo inexacto. Se pregunta una vez mientras Inicio siga en el historial.
  - **Al volver de los ajustes:** `MainActivity.onResume` compara con el valor anterior; si el
    permiso pasó de no concedido a concedido, llama `sincronizadorRecordatorios.reprogramarTodo()`
    y las alarmas inexactas se reemplazan por exactas.
  - **Límites:** las reservas se cargan cuando una pantalla llama a `refrescar()`, así que una
    reserva creada desde la web se programa la próxima vez que se abre la app. Sin permiso de
    alarma exacta, Android puede atrasar el aviso hasta una hora.
  - **Reinicio del teléfono:** las alarmas se pierden al reiniciar. No hay un receiver de
    `BOOT_COMPLETED` a propósito: el sincronizador las vuelve a programar al abrir la app.

### Decisiones tomadas

- **Aviso al coordinador cuando un relator hace check-in:** no hay notificación push.
  El coordinador ve el check-in en «Reservas del día» y en «Supervisar reserva», que
  piden los datos al servidor cada vez que se abren.
- **Lectura del código QR para el check-in:** ML Kit Barcode Scanning (ya está en el
  proyecto, en `gradle/libs.versions.toml`).

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

## Etapa integración

Terminó la etapa maqueta: ahora cada parte de la app se conecta al backend real.

### Conexión con el backend
- La app usa el backend real mediante los repositorios remotos de `data/repository/`:
  `RepositorioSalasRemoto`, `RepositorioReservasRemoto`, `RepositorioHorarioRemoto` y
  `RepositorioPerfilRemoto`.
- **Perfil: qué es remoto y qué es local.**
  - Remoto (ms-usuarios, Contrato 1 v8 y v10, vía `RepositorioPerfil`): fecha de nacimiento,
    teléfono, área y contraseña. Nombre, email y rol solo se muestran: no se editan.
    `PerfilViewModel` valida antes de enviar (mayor de 18, celular "+569" + 8 dígitos, área de
    `data/modelo/Areas.kt`, contraseña nueva de 8 o más caracteres) y envía solo lo que cambió.
  - Local (solo en el teléfono, no sube al servidor): la foto de perfil. El archivo va en
    `filesDir/foto_perfil_<idUsuario>.jpg` (reducido a 512 px) y su ruta en el DataStore
    **"perfil"** (`data/local/PerfilDataStore.kt`), separado de "sesion" para que cerrar sesión
    no la borre. Los archivos los maneja `data/local/AlmacenFotoPerfil.kt`.
- Supervisar reserva pide el relator con `repositorioPerfil.obtenerUsuario(id)` (solo
  coordinador) para mostrar su teléfono y área, con el botón "Llamar" (`Intent.ACTION_DIAL`).

### API externa: feriados de Chile (Nager.Date)
- Es el requisito de "API externa" de la pauta. Endpoint público y sin clave:
  `GET https://date.nager.at/api/v3/PublicHolidays/{anio}/CL`. La URL base está en
  `app/build.gradle.kts` como `URL_API_FERIADOS`.
- Archivos: `data/remote/FeriadosApi.kt`, `data/remote/dto/FeriadoDtos.kt`,
  `data/modelo/Feriado.kt` y `data/repository/RepositorioFeriados.kt`
  (`RepositorioFeriadosRemoto`, interfaz en `Repositorios.kt`).
- **Va sin token.** En `AppContainer` usa su propio `OkHttpClient`, sin `TokenInterceptor`:
  el token de nuestros usuarios solo viaja a nuestro backend, nunca a un servidor externo.
  No reutilizar `clienteHttp` para APIs externas.
- **Filtro de región:** solo se conservan los feriados nacionales (`global == true`) y los que
  incluyen la Región de Valparaíso (`"CL-VS"` en `counties`), porque la sede es Viña del Mar.
- Se guardan en memoria por año (se piden una vez por año y por apertura de la app). Si la API
  falla, el repositorio devuelve null o vacío sin lanzar excepciones y no guarda nada, para
  reintentar después: sin conexión la app funciona igual, pero sin avisos de feriados.
- Para qué se usa:
  - **Reservar:** `ReservarViewModel.seleccionarFecha` consulta `feriadoEn(fecha)`. Si es feriado
    se muestra "Feriado: <nombre>. La sede no funciona este día." y no se puede confirmar;
    `crearReserva` también lo rechaza.
  - **Inicio del relator y del coordinador:** tarjeta `TarjetaFeriado` (en `ui/componentes/`) con
    el próximo feriado. Sin datos, no se muestra.
- El backend no valida feriados (fuera de alcance).
- El horario general es remoto (`GET /horario` de ms-reservas, público). Se carga al abrir
  la app y cada pantalla que lo usa llama a `repositorioHorario.refrescar()` al abrirse
  (Detalle de sala, Reservar y Horario general). Al guardar una sala, la app envía
  `usaHorarioGeneral = true` cuando la sala no tiene horario propio (Contrato 1 v9).
- Los repositorios de `data/falso/` se conservan solo para las pruebas unitarias.
  Ninguna pantalla ni ViewModel nuevo debe depender de ellos.
- Las reservas no se cargan solas: cada pantalla que las muestra llama a
  `repositorioReservas.refrescar()` al abrirse (en el `init` de su ViewModel o con
  `LaunchedEffect`).
- Las URL base se definen en `app/build.gradle.kts` como BuildConfig: `URL_MS_USUARIOS`,
  `URL_MS_SALAS` y `URL_MS_RESERVAS`. En desarrollo apuntan a `localhost` con
  `adb reverse` (ver sección 4); en la entrega las tres apuntan a
  `http://34.194.115.43/api/v1/` (Nginx en AWS).
- El token se agrega a cada petición con `data/remote/TokenInterceptor.kt`. No agregar
  el encabezado `Authorization` a mano en las interfaces de Retrofit.

### Referencias de diseño
- Especificación de cada pantalla, elemento por elemento: `docs/maqueta/especificacion.md`
- Imagen de cada pantalla: `docs/maqueta/pantallas/<persona>/`
- Antes de modificar una pantalla, lee su sección en la especificación y mira su imagen.

### Estructura de carpetas
Rutas relativas a `app/src/main/java/cl/duoc/reservasalas/app/`.

| Carpeta | Contenido | Quién la modifica |
| --- | --- | --- |
| `ui/theme/` | Colores y tema | Solo Kevin |
| `ui/componentes/` | Componentes compartidos | Solo Kevin |
| `ui/navegacion/` | Rutas y NavHost | Solo Kevin |
| `data/modelo/` | Sala, Equipo, Reserva, EstadoReserva | Solo Kevin |
| `data/remote/` y `data/repository/` | Retrofit, DTO y repositorios remotos | Solo Kevin |
| `data/falso/` | Repositorios falsos (solo pruebas) | Solo Kevin |
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
- El ViewModel obtiene los datos de los repositorios del `container` (las interfaces de
  `data/repository/Repositorios.kt`), nunca directamente de `data/falso/` ni de Retrofit.
  Prohibido escribir datos de ejemplo dentro de la pantalla o del ViewModel.
- Cada pantalla tiene al menos una `@Preview`.
- Cada lista tiene su mensaje para el caso vacío (sin datos).
- Navegar solo con los nombres de ruta de la tabla de abajo. Prohibido inventar rutas nuevas.

### Rutas definitivas (16)
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
| reservaCancelada/{idReserva} | Reserva cancelada | Simón |
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
- Todo se trabaja en la rama `feature/kevin`.
- Solo Kevin hace commit, push y fusiones (`develop` y `main` siguen las reglas de la raíz).
- Antes de cada commit, verificar la rama actual con `git status`.
- Commits en español, verbo en presente (ej. "Conecta las reservas con ms-reservas").

### Una pantalla está terminada cuando
- Se ve igual a su imagen en `docs/maqueta/pantallas/`.
- Usa solo colores del tema y componentes compartidos (o tiene el TODO si el componente aún no existe).
- Muestra los datos que entrega el repositorio del `container` (backend real).
- Navega a las rutas de la tabla.
- Tiene mensaje para el caso vacío.
- Se probó en un teléfono o emulador con el backend levantado, no solo en la vista previa.

### Base disponible
Todo esto ya existe. Úsalo, no lo dupliques.

- **Colores**: `ui/theme/Color.kt` (NegroDuoc, AmarilloDuoc, AmarilloSuave, Fondo, Superficie, Borde,
  Gris85, RojoError, VerdeExito, GrisCancelada, GrisOcupado, GrisSobreNegro, Blanco).
- **Componentes** (`ui/componentes/`): BarraSuperior, BarraInferior, LogoDuoc, BotonPrincipal,
  BotonSecundario, BotonPeligro, ChipEstado, ChipEquipo, ChipRol, TarjetaSala, TarjetaMetrica,
  TarjetaFeriado, AvisoDestacado, FormaTarjeta y PantallaEnConstruccion. Cada archivo tiene su @Preview.
- **Modelos** (`data/modelo/`): Sala, Equipo, EstadoEquipo, HorarioSala, Bloque, HorarioGeneral,
  Reserva, EstadoReserva, y funciones de formato (formatearHora, formatearFechaLarga, formatearFechaCorta).
  `Usuario` y `Rol` están en `model/` (son del login).
- **Reglas de reserva ya programadas** en `Reserva`: checkInHabilitado, sinCheckIn, sePuedeCancelar,
  mostrarRecordatorio, necesitaRecordatorio y ocupa(bloque). Los bloques de una sala: `sala.bloques(horarioGeneral)`.
  No repitas esas reglas dentro de un ViewModel: llama a estas funciones.
- **Repositorios**: las interfaces están en `data/repository/Repositorios.kt` (RepositorioSalas,
  RepositorioReservas, RepositorioHorario, RepositorioPerfil, RepositorioAutenticacion). Las
  implementaciones remotas, en `data/repository/` (`AuthRepository`, `RepositorioSalasRemoto`,
  `RepositorioReservasRemoto`, `RepositorioHorarioRemoto`, `RepositorioPerfilRemoto`);
  las falsas, en `data/falso/`
  solo para pruebas.
- **Hora actual**: NUNCA usar `LocalDateTime.now()` ni `LocalDate.now()`. Usar `container.reloj.ahora()`
  y `container.reloj.hoy()`. Se usa `RelojSistema` (la hora real del teléfono).
- **Usuario conectado**: `container.sesionDataStore.sesion` (Flow con la sesión; `sesion?.usuario`).
- **Cómo un ViewModel obtiene sus datos**: igual que `ui/pantallas/perfil/PerfilViewModel.kt`, con un
  `Factory` que saca los repositorios de `(this[APPLICATION_KEY] as ReservaSalasApp).container`.
  Ese archivo es el ejemplo a copiar.
- **Login**: real, contra ms-usuarios (`POST /auth/login`) mediante `AuthRepository`. Las
  contraseñas de prueba no se escriben en este archivo.

### Navegación (contrato con las pantallas)
- Cada pantalla existe en `ui/pantallas/<nombre>/` con su firma definitiva.
  Se modifica el CONTENIDO de la función; NO se cambia su nombre ni sus parámetros.
- Las pantallas no reciben el NavController. Reciben:
  - `navegar(ruta)`: para ir a otra pantalla, siempre con `Rutas` (ej. `navegar(Rutas.sala(codigo))`).
  - `volver()`: para la flecha atrás de la BarraSuperior.
- La barra inferior la dibuja la navegación: las pantallas principales NO dibujan BarraInferior.
  Todas las pantallas sí dibujan su propia BarraSuperior (con `alVolver = volver` si es secundaria).
- Al ir a `Rutas.reservaConfirmada(id)`, la navegación saca "Reservar" del historial automáticamente.
- El botón "Editar sala" (solo coordinador) hace `navegar(Rutas.editarSala(codigo))`.
