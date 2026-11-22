# Pruebas unitarias de la app Android

La pauta pide "pruebas unitarias que cubran al menos el 80% de la lógica, implementadas en
módulos como ViewModel o Repository, usando JUnit, MockK o Kotest". Este documento explica
cómo ejecutarlas, cómo se mide la cobertura y qué prueba cada grupo.

- Herramientas: **JUnit 4**, **MockK** (simula objetos), **kotlinx-coroutines-test** (corrutinas)
  y **JaCoCo** (cobertura; viene con el plugin de Android).
- Las pruebas están en `app-android/app/src/test/java/cl/duoc/reservasalas/app/`.
- Corren en el computador (JVM): no necesitan teléfono, emulador, internet ni backend.

## 1. Cómo ejecutarlas (Windows, PowerShell)

Todos los comandos se ejecutan dentro de `app-android/`.

| Acción | Comando |
|--------|---------|
| Ejecutar todas las pruebas unitarias | `.\gradlew.bat testDebugUnitTest` |
| Volver a ejecutarlas aunque nada haya cambiado | `.\gradlew.bat testDebugUnitTest --rerun` |
| Pruebas + reporte de cobertura | `.\gradlew.bat testDebugUnitTest createDebugUnitTestCoverageReport` |
| Porcentaje de cobertura de la lógica | `.\cobertura-logica.ps1` |
| Una sola clase de prueba | `.\gradlew.bat testDebugUnitTest --tests "*.CheckInViewModelTest"` |
| Una sola prueba | `.\gradlew.bat testDebugUnitTest --tests "*.CheckInViewModelTest.qrDeOtraSala_muestraErrorYNoRegistraNada"` |

Resultados:
- Pruebas: `app-android/app/build/reports/tests/testDebugUnitTest/index.html`.
- Cobertura (todas las clases): `app-android/app/build/reports/coverage/test/debug/index.html`.

Si `.\cobertura-logica.ps1` no se deja ejecutar ("la ejecución de scripts está deshabilitada"),
usar: `powershell -ExecutionPolicy Bypass -File .\cobertura-logica.ps1`.

## 2. Cobertura: qué cuenta como lógica

La cobertura se activó con `enableUnitTestCoverage = true` en el `buildType` `debug` de
`app-android/app/build.gradle.kts`. No se agregó ningún plugin.

El reporte de JaCoCo mide **todas** las clases, también las pantallas de Compose, que se prueban
en el teléfono y no con pruebas unitarias. El plugin de Android **no permite excluir clases** de ese
reporte, así que `app-android/cobertura-logica.ps1` lee el reporte XML
(`app/build/reports/coverage/test/debug/report.xml`) y calcula el porcentaje de **líneas** solo de la
lógica. El script no cambia la compilación: solo lee el reporte. Si el total queda bajo 80 %,
termina con error.

**Cuenta como lógica:**

| Carpeta (`app/src/main/java/cl/duoc/reservasalas/app/`) | Qué es |
|---|---|
| `data/repository/` | Repositorios remotos y `AuthRepository` |
| `data/modelo/` | Reglas de la reserva, bloques y horario, formatos |
| `data/remote/dto/` | Conversiones entre el JSON del backend y los modelos |
| `data/remote/TokenInterceptor.kt` | Agrega el token a cada petición |
| `data/local/*DataStore.kt` | Sesión, foto de perfil e ids de recordatorios |
| `model/` | Usuario, perfil y rol |
| `viewmodel/` y `ui/pantallas/**/*ViewModel*.kt` | Todos los ViewModel |
| `ui/navegacion/Rutas.kt` | Armado de rutas |
| `notificaciones/SincronizadorRecordatorios.kt` | Qué recordatorios programar o borrar |

**No cuenta (y por qué):**

| Excluido | Motivo |
|---|---|
| `*Pantalla*`, `LoginScreen`, `ui/componentes/`, `ui/theme/`, `NavegacionApp` | Interfaz de Compose: se prueba en el teléfono |
| `MainActivity`, `ReservaSalasApp`, `AppContainer` | Arranque de Android e inyección manual: solo crean objetos |
| `RecordatorioReceiver`, `ProgramadorRecordatoriosAlarmas`, `PermisoAlarmasExactas`, `CanalRecordatorios` | Usan AlarmManager y notificaciones de Android |
| `data/local/AlmacenFotoPerfil` | Archivos e imágenes del teléfono |
| Interfaces `*Api` de Retrofit | Solo declaran endpoints; no tienen código |
| `data/falso/` | Repositorios falsos: se usan como dobles de prueba, no son lógica de la app |
| `Factory` de cada ViewModel | Solo sacan los repositorios del `AppContainer` |
| Clases generadas (`$$serializer`, `Companion`, la línea `by preferencesDataStore`) | Las genera Kotlin o necesitan Android |

### Leerlo a mano en el reporte HTML

1. Abrir `app-android/app/build/reports/coverage/test/debug/index.html`.
2. Mirar la columna **Lines** de los paquetes de la tabla de arriba (`...data.repository`,
   `...data.modelo`, `...data.remote.dto`, etc.).
3. En los paquetes `...ui.pantallas.<nombre>`, entrar al paquete y mirar solo la fila del
   `<Nombre>ViewModel` (las demás filas son la pantalla).

## 3. Resultado (21/11/2026)

170 pruebas, todas pasan. **Cobertura de la lógica: 88,1 %** (1056 de 1198 líneas).

| Paquete | Líneas | Cubiertas | % |
|---|---:|---:|---:|
| data.local | 55 | 51 | 92,7 |
| data.modelo | 123 | 111 | 90,2 |
| data.remote | 10 | 10 | 100 |
| data.remote.dto | 178 | 175 | 98,3 |
| data.repository | 253 | 231 | 91,3 |
| model | 15 | 15 | 100 |
| notificaciones | 40 | 40 | 100 |
| ui.navegacion | 12 | 12 | 100 |
| ui.pantallas.agenda | 17 | 17 | 100 |
| ui.pantallas.checkin | 78 | 49 | 62,8 |
| ui.pantallas.detallereserva | 36 | 34 | 94,4 |
| ui.pantallas.detallesala | 21 | 20 | 95,2 |
| ui.pantallas.iniciorelator | 58 | 58 | 100 |
| ui.pantallas.perfil | 201 | 136 | 67,7 |
| ui.pantallas.reservaconfirmada | 7 | 7 | 100 |
| ui.pantallas.reservar | 50 | 46 | 92 |
| ui.pantallas.salas | 10 | 10 | 100 |
| viewmodel (login) | 34 | 34 | 100 |
| **Total lógica** | **1198** | **1056** | **88,1** |

Lo que falta cubrir y por qué:
- **Check-in (62,8 %)**: `crearArchivoFoto` y `procesarFoto` usan la cámara y ML Kit, que necesitan
  un teléfono. Se prueba todo lo que pasa después de leer el código (sala correcta, otra sala,
  fuera de horario, error del servidor).
- **Perfil (67,7 %)**: la foto de perfil (cámara, galería y archivos) depende de Android.

## 4. Qué se simula en cada grupo y por qué

| Grupo (clases de prueba) | Qué se simula | Por qué |
|---|---|---|
| Repositorios remotos (`RepositorioReservasRemotoTest`, `RepositorioSalasRemotoTest`, `RepositorioHorarioRemotoTest`, `RepositorioPerfilRemotoTest`) | Las APIs de Retrofit, con **MockK** (`coEvery { api.crear(any()) } throws ...`). Los errores son `HttpException` con el JSON de error del contrato, o `IOException` (sin conexión). | Probar sin backend ni internet, y provocar a voluntad errores difíciles de lograr con el servidor real (409, 400, sin conexión). |
| `RepositorioFeriadosTest`, `AuthRepositoryTest` | APIs falsas escritas a mano (son anteriores a MockK) y DataStore en carpeta temporal. | Igual que arriba; `AuthRepositoryTest` revisa además que la sesión quede guardada de verdad. |
| ViewModels (`CheckIn`, `InicioRelator`, `DetalleReserva`, `ViewModelsSimplesTest`) | Repositorios y sesión con **MockK**. Reloj fijo (`relojFijo`). `Dispatchers.setMain`. | Decidir qué datos ve la pantalla y a qué hora "es", para que las reglas de horario den siempre lo mismo. `setMain` reemplaza el hilo principal de Android, que no existe en el computador. |
| `PerfilViewModelTest`, `ReservarViewModelTest`, `LoginViewModelTest` | Repositorios falsos de `data/falso/` o escritos a mano, DataStore en carpeta temporal. | Probar el flujo completo del formulario con datos reales en memoria. |
| Modelo y conversiones (`ReservaTest`, `HorarioTest`, `DtosTest`, `RutasTest`) | Nada. | Son funciones puras: reciben la hora o el DTO como parámetro. |
| `DataStoresTest` | Un DataStore en memoria. | Se prueba la lógica de las claves, no la escritura del archivo (en Windows el antivirus puede bloquear escrituras seguidas). |
| `TokenInterceptorTest` | La sesión y la cadena de OkHttp con **MockK**. | Ver la petición final sin enviarla a ningún servidor. |
| `SincronizadorRecordatoriosTest` | Un programador de recordatorios falso que anota las llamadas. | No depender de AlarmManager. |

**¿Por qué MockK?** Antes, cada prueba escribía una API falsa que implementaba **todos** los métodos
de la interfaz (con `TODO(...)` en los que no usaba). Con MockK se indica en una línea qué responde
cada endpoint y se verifica que se llamó (`coVerify`). Se agregó solo para las pruebas
(`testImplementation`): no entra al APK.

**Prueba inestable corregida.** `LoginViewModelTest` fallaba a veces con
`UncaughtExceptionsBeforeTest`. La culpa no era suya: en otra prueba, una corrutina del ViewModel
seguía leyendo el DataStore cuando la prueba ya había quitado el hilo principal de prueba
(`Dispatchers.resetMain()`). Al despertar no lo encontraba, lanzaba un error y kotlinx-coroutines-test
se lo atribuía a la siguiente prueba con `runTest`. Ahora todas las pruebas de ViewModel lo crean en
un `ViewModelStore` y en `@After` llaman `cerrarYEsperar(...)` y `cancelarYEsperar()` **antes** de
`resetMain()` (ver `AyudasPrueba.kt`).

## 5. Dos pruebas para mostrar en vivo en la defensa

### 1) El servidor rechaza una reserva cruzada (409 USUARIO_OCUPADO)

```powershell
.\gradlew.bat testDebugUnitTest --tests "*.RepositorioReservasRemotoTest.crear_409UsuarioOcupado_muestraMensajeClaro"
```

Qué muestra: MockK hace que `POST /reservas` responda **409** con
`{"codigo":"USUARIO_OCUPADO",...}`, igual que ms-reservas cuando el relator ya tiene una reserva a
esa hora. La prueba verifica que la app no muestra el error técnico, sino
"Ya tienes una reserva en ese horario". Demuestra: uso de MockK, el contrato de errores de la API
y la regla "nunca mostrar el error técnico al usuario".

### 2) Check-in con el QR de otra sala

```powershell
.\gradlew.bat testDebugUnitTest --tests "*.CheckInViewModelTest.qrDeOtraSala_muestraErrorYNoRegistraNada"
```

Qué muestra: la reserva es en S-101 y el relator escanea el QR de LAB-01. El ViewModel muestra
"Este código es de la sala LAB-01. Tu reserva es en S-101." y, con `coVerify(exactly = 0)`, se
comprueba que **no** se envió el check-in al servidor. Demuestra: prueba de un ViewModel, la regla de
negocio del check-in y cómo se verifica que algo NO ocurrió.

Para que se vea en vivo que la prueba de verdad revisa algo: cambiar el texto esperado en el
`assertEquals`, ejecutar (falla en rojo) y volverlo a dejar como estaba.
