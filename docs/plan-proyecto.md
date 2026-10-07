> Versión en Markdown del plan del proyecto. El original se mantiene en Word; si cambia, se vuelve a exportar este archivo.

# Plan del Proyecto Semestral – Sistema de Reserva de Salas Duoc UC

Sep 30, 2026 · @kevin

## 1. Resumen del proyecto

Construiremos un MVP (producto mínimo funcional) para Duoc UC: una app Android donde relatores y coordinadores reservan salas por bloques de 1 hora, con un backend central que impide los cruces de horario.

**El problema.** Hoy las salas se reservan por llamadas, mensajes y planillas personales. Eso produce dos talleres en la misma sala a la misma hora, salas sobre-reservadas y relatores recorriendo la sede para encontrar un espacio libre.

**La solución.** Un sistema de dos partes que se comunican mediante una API REST:

- **App Android:** la usan las personas. Se construye con Kotlin, Jetpack Compose, Material Design 3, arquitectura MVVM, Room (base de datos local) y Retrofit (conexión al servidor).

- **Backend:** guarda los datos de todos y valida que no existan cruces. Se construye con Spring Boot, dividido en 3 microservicios, y una base de datos MySQL.

arquitectura · app MVVM, API REST, 3 microservicios

La app nunca habla directo con MySQL: todo pasa por la API, y ms-reservas consulta a ms-salas antes de aceptar una reserva.

**Perfiles de usuario**

| Perfil                   | Qué hace en la app                                                                                                                     |
|--------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| Relator (principal)      | Revisa su agenda, busca salas por capacidad o equipamiento, reserva bloques, cancela sus propias reservas y recibe recordatorios       |
| Coordinador (secundario) | Todo lo del relator, más: ve la ocupación general, administra el catálogo de salas y cancela cualquier reserva para resolver problemas |

**Decisiones ya tomadas.** Ninguna se cambia sin acuerdo de los tres integrantes y sin actualizar este documento.

| Tema                      | Decisión                                                                                                                                                                                                                                                                  |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Horarios                  | Bloques fijos de 1 hora, sin pausas entre ellos. Horario predeterminado: 13 bloques, de 08:00 a 21:00. Se define al inicio de cada semestre y el coordinador puede modificarlo (requisitos del mandante, más abajo)                                                       |
| Inicio de sesión          | Email y contraseña reales, con token JWT. Sin inicio de sesión con Google                                                                                                                                                                                                 |
| Sin conexión              | Se puede consultar (datos guardados en Room), pero no reservar ni cancelar                                                                                                                                                                                                |
| Cancelar una reserva      | Cambia su estado a CANCELADA; nunca se borra                                                                                                                                                                                                                              |
| Retirar una sala          | Se marca como inactiva; nunca se borra                                                                                                                                                                                                                                    |
| Recordatorios             | Notificación local 15 minutos antes del inicio de cada reserva                                                                                                                                                                                                            |
| Datos de prueba           | Solo datos ficticios, por las leyes N.º 21.719 y N.º 19.628. Nunca nombres, correos ni contraseñas reales                                                                                                                                                                 |
| Repositorio               | Uno solo en GitHub, con la app y el backend en carpetas separadas                                                                                                                                                                                                         |
| Ramas de Git              | Una rama feature/ por tarea, que se fusiona en develop (integración y pruebas). develop pasa a main solo cuando el proyecto esté funcionando. Sin pull requests (sección 3)                                                                                               |
| Agentes de IA             | Archivos AGENTS.md en la raíz, en app-android/ y en backend/, con las mismas decisiones de este documento. Si se cambia una decisión, se actualizan ambos                                                                                                                 |
| Dispositivos de prueba    | Un teléfono con Android 12 (recursos limitados), un emulador reciente y un emulador con Android 8.0 para probar la versión mínima                                                                                                                                         |
| Versión mínima de Android | minSdk 26 (Android 8.0). Desde esta versión se puede usar java.time (clases de fechas y horas como LocalDate y LocalTime) sin configuración adicional, y sigue cubriendo la gran mayoría de teléfonos en uso. targetSdk y compileSdk: la versión más reciente disponible. |
| Paquete de la app         | cl.duoc.reservasalas.app. En el backend: cl.duoc.reservasalas.\<nombre\> (por ejemplo cl.duoc.reservasalas.usuarios)                                                                                                                                                      |
| Backend                   | Java con Spring Boot y Maven                                                                                                                                                                                                                                              |
| Metodología               | Scrum ligero: sprints de 2 semanas con tablero Kanban en Trello (sección 3)                                                                                                                                                                                               |
| Mensajes de commit        | En español, en presente y describiendo el cambio. Ejemplo: Agrega entidad Usuario y su repositorio (sección 3)                                                                                                                                                            |

**Entregables obligatorios del ramo:** repositorio en GitHub, tablero en Trello y APK firmado en modo release.

**Requisitos del mandante**

Requisitos obligatorios agregados por el mandante después del diseño inicial. Antes de programarlos, su diseño detallado (campos, endpoints y pantallas) se agrega a las secciones 4, 5 y 6.

- **Horarios editables:** el coordinador edita el horario general de bloques y el horario propio de cada sala. El horario se define al comenzar cada semestre.

- **Equipamiento:** el coordinador edita el equipamiento de cada sala y deja comentarios sobre su estado (por ejemplo, «el proyector del teatro tiene fallas»).

- **Check-in con QR:** el relator confirma el uso del espacio escaneando el código QR de la sala. El check-in notifica al coordinador, para que pueda verificar el uso real.

- **No presentado:** si el relator no hace check-in, la reserva se marca como no presentado y la sala no se libera.

- **Espacios:** además de salas de clases y laboratorios, se reservan otros espacios, como la sala de graduaciones o la capilla. El catálogo actual es provisional y se reemplazará por la lista real del mandante.

- **Fuera de alcance por ahora:** sistema de calificación de relatores.

**Pendientes por resolver**

- [x] Revisar la versión de Android del teléfono de pruebas: Android 12. El minSdk queda en 26 (Android 8.0).

- [ ] Conseguir la fecha de la última presentación o del cierre del semestre, para calendarizar los sprints.

- [x] Lenguaje del backend: Java con Spring Boot y Maven.

- [ ] Agregar la pauta mínima de presentaciones cuando el docente la envíe.

- [x] Escribir el nombre de cada integrante en el organigrama.

- [ ] Diseñar en las secciones 4, 5 y 6 los requisitos del mandante (campos, endpoints y pantallas).

- [ ] Decidir cómo llega al coordinador el aviso de check-in.

- [ ] Elegir la librería para leer el código QR.

## 2. Organigrama y responsabilidades

El equipo tiene tres roles: el líder trabaja en la capa de datos de la app y en el microservicio ms-usuarios, y coordina a los integrantes de Backend e Interfaz.

organigrama del equipo · 3 roles, 2 contratos

Cada flecha es una frontera de trabajo: el líder acuerda la API con Backend (Contrato 1) y las pantallas con Interfaz (Contrato 2).

| Rol                     | Integrante     | Es responsable de                                                                                                                                                                                                                                                                    | Fases principales      |
|-------------------------|----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------|
| Líder · Datos de la app | Kevin Maturana | Modelos de datos en Kotlin, Room, Retrofit, repositorios, ViewModels, sesión y token, recordatorios locales y el microservicio ms-usuarios (login y JWT). Coordina el equipo, mantiene este documento y los AGENTS.md, y revisa los cambios que tocan la capa de datos o un contrato | 3, 4 (ms-usuarios) y 5 |
| Backend                 | Bastian Veas   | Microservicios ms-salas y ms-reservas en Java con Spring Boot, base de datos MySQL, validación del JWT, validación de cruces, carga del dataset en el servidor, colección de Postman                                                                                                 | 4                      |
| Interfaz                | Simon Stuardo  | Pantallas en Jetpack Compose, tema Material Design 3, navegación, grilla de agenda, filtros, selectores de fecha, diálogos y mensajes de error                                                                                                                                       | 2 y 6                  |

**Qué entrega y qué recibe cada rol**

- **Backend → Datos:** endpoints funcionando tal como dice el Contrato 1 (sección 5) y la colección de Postman para probarlos.

- **Datos → Interfaz:** un ViewModel por pantalla que entrega los datos y estados del Contrato 2 (sección 6).

- **Interfaz → Datos:** las acciones del usuario (tocar, filtrar, confirmar), avisadas al ViewModel. La interfaz nunca llama al servidor ni a Room directamente.

**Trabajo compartido por los tres**

- Fase 1 (diseño) y Fase 7 (pruebas y entrega).

- Aprender lo básico de Kotlin y MVVM: en la presentación el docente puede preguntarle cualquier parte a cualquiera.

- Mantener sus tarjetas de Trello al día.

- Revisar el trabajo de otro integrante cuando se lo pidan.

**Reglas de coordinación**

1.  Los contratos (secciones 4, 5 y 6) son la fuente de verdad. Si alguien necesita cambiar un campo, un endpoint o una pantalla, lo propone al equipo, se acuerda y el líder actualiza este documento **antes** de programar el cambio.

2.  Si alguien está bloqueado más de un día por una tarea, lo avisa al grupo.

3.  El líder revisa todo cambio que toque la capa de datos o un contrato antes de que se fusione en develop.

## 3. Reglas de trabajo en equipo

Nadie trabaja directamente sobre develop ni sobre main: cada tarea de Trello se trabaja en su propia rama feature/, que se fusiona en develop al terminar. develop pasa a main solo cuando el proyecto esté funcionando y el equipo lo decida.

**Conceptos**

- **Rama (branch):** copia paralela del código. Trabajas en ella sin afectar las ramas develop y main.

- **Commit:** una foto guardada de tus cambios, con un mensaje que explica qué hiciste.

- **develop:** rama de integración y pruebas generales del equipo. Recibe cada rama feature/ terminada.

- **main:** versión estable del proyecto. Recibe develop solo cuando el proyecto esté funcionando y el equipo lo decida.

- **Merge (fusión):** unir los commits de una rama con otra, por ejemplo una rama feature/ con develop.

**Estructura del repositorio reserva-salas**

reserva-salas/  
├── AGENTS.md ← instrucciones para agentes de IA  
├── CLAUDE.md ← importa AGENTS.md para Claude Code  
├── app-android/ ← proyecto de Android Studio y su AGENTS.md (Líder e Interfaz)  
├── backend/ ← microservicios y su AGENTS.md  
│ ├── ms-usuarios/ ← login y usuarios (Líder)  
│ ├── ms-salas/ ← catálogo de salas (Backend)  
│ └── ms-reservas/ ← bloques, reservas y validación de cruces (Backend)  
├── docs/  
│ ├── plan-proyecto.md ← este documento, en Markdown  
│ ├── dataset/ ← salas.json, usuarios.json, reservas.json  
│ ├── postman/ ← colección para probar la API  
│ └── bocetos/ ← diseños de las pantallas  
└── README.md

Git no guarda carpetas vacías. Por eso cada carpeta nueva lleva un archivo vacío llamado .gitkeep hasta que tenga archivos reales.

El archivo .gitignore de la raíz lista lo que Git nunca sube: los archivos que se generan al compilar y, sobre todo, llaves y contraseñas. Su contenido:

\# Android / Gradle  
.gradle/  
build/  
local.properties  
\*.iml  
.idea/  
captures/  
.cxx/  
\*.apk  
\*.aab  
  
\# Java / Spring Boot  
target/  
\*.class  
\*.log  
  
\# Llaves y secretos: NUNCA al repositorio  
\*.jks  
\*.keystore  
keystore.properties  
.env  
application-local.properties  
  
\# Sistema operativo  
.DS_Store  
Thumbs.db

**Nombres de ramas**

| Prefijo  | Para qué                                                   | Ejemplo                 |
|----------|------------------------------------------------------------|-------------------------|
| feature/ | Cualquier tarea: funcionalidad, corrección o documentación | feature/pantalla-agenda |

Solo se usa el prefijo feature/, también para correcciones y documentación. Nombres en minúsculas, sin tildes ni espacios, con guiones.

**Mensajes de commit**

Cada mensaje se escribe en español, en presente y describe el cambio, por ejemplo Agrega entidad Usuario y su repositorio o Corrige validación de bloques. No se usan prefijos de tipo como feat o fix.

**Flujo de cada tarea, paso a paso**

1.  En Trello, asígnate la tarjeta y muévela a **En progreso**.

2.  Actualiza tu copia de develop:

> git switch develop  
> git pull

3.  Crea la rama de la tarea (cambia pantalla-agenda por el nombre de tu tarea):

> git switch -c feature/pantalla-agenda

4.  Programa. Guarda tus avances en commits pequeños, cada uno con un mensaje que diga qué hiciste:

> git add .  
> git commit -m "Agrega lista de reservas en Mi agenda"

5.  Sube la rama a GitHub:

> git push -u origin feature/pantalla-agenda

6.  Cuando la tarea está lista y compila, mueve la tarjeta a **En revisión** y avisa al equipo. Si toca la capa de datos o un contrato, la revisa el líder.

7.  Con la revisión hecha, fusiona la rama en develop y súbela:

> git switch develop  
> git pull  
> git merge feature/pantalla-agenda  
> git push

8.  Mueve la tarjeta a **Terminado** y borra la rama con git branch -d feature/pantalla-agenda.

**Reglas que no se rompen**

- Nunca trabajar directamente sobre develop ni sobre main. develop pasa a main solo cuando el proyecto esté funcionando y el equipo lo decida. No se usan pull requests ni reglas de protección en GitHub.

- Nunca subir al repositorio la llave de firma del APK (keystore), contraseñas ni claves. Se agregan al archivo .gitignore.

- Antes de fusionar una rama en develop, el código debe compilar y la app o el microservicio debe arrancar sin errores.

- Un agente de IA (Claude Code u otro) nunca fusiona ni sube cambios a develop o main sin confirmación de quien trabaja con él.

**Tablero de Trello**

| Lista         | Qué contiene                                                         |
|---------------|----------------------------------------------------------------------|
| Backlog       | Todas las tareas del proyecto que aún no entran a un sprint          |
| Sprint actual | Las tareas elegidas en la planificación del sprint                   |
| En progreso   | Alguien está trabajando en ella. Máximo 2 tarjetas por persona       |
| En revisión   | La tarea está lista y espera revisión antes de fusionarse en develop |
| Terminado     | Rama fusionada en develop                                            |

- **Etiquetas de área:** Diseño · App-Datos · App-Interfaz · Backend · Documentación.

- **Etiquetas de sprint:** Sprint 1, Sprint 2, etc. La del siguiente sprint se crea en su planificación.

- **Cada tarjeta tiene:** título con verbo («Crear endpoint POST /reservas», no «Reservas»), un solo responsable, fecha límite, descripción con el criterio de terminado y, si la tarea es larga, una checklist.

- **Power-Up de GitHub:** cada tarjeta lleva adjunta su rama, para ver sus commits sin salir de Trello.

- Al cerrar un sprint, las tarjetas de Terminado se archivan.

- Una tarjeta = una rama feature/.

**Metodología: Scrum ligero**

Trabajamos en sprints de 2 semanas, con las reuniones de Scrum reducidas a lo que un equipo de 3 estudiantes puede sostener.

| Elemento      | Cómo se aplica                                                                        |
|---------------|---------------------------------------------------------------------------------------|
| Sprint        | 2 semanas, con una meta tomada de las fases (sección 8)                               |
| Planificación | 30 minutos al inicio: se eligen tarjetas del Backlog y cada uno se asigna las suyas   |
| Seguimiento   | 2 o 3 veces por semana, por el chat del grupo: qué hice, qué haré, si estoy bloqueado |
| Revisión      | Al cierre: se muestra en el teléfono lo que funciona                                  |
| Retrospectiva | 10 minutos al cierre: qué salió bien y qué mejorar                                    |
| Product Owner | El líder: prioriza el Backlog y decide qué entra en cada sprint                       |

**Sprints**

| Sprint | Meta                                             | Fases |
|--------|--------------------------------------------------|-------|
| 1      | Equipo preparado y diseño cerrado                | 0 y 1 |
| 2      | App navegable con datos falsos; backend iniciado | 2 y 4 |
| 3      | Datos locales en Room; backend completo          | 3 y 4 |
| 4      | App conectada al backend real                    | 5     |
| 5      | Funciones completas y recordatorios              | 6     |
| 6      | Pruebas, APK firmado y presentación              | 7     |

Son 12 semanas. Cuando se conozca la fecha de cierre del semestre se ponen fechas a cada sprint; si quedan menos semanas, se juntan los sprints 4 y 5.

**Releases por sprint.** Desde el sprint 2, al cerrar cada sprint se crea una versión en GitHub (Releases → Draft a new release) con el APK de ese momento adjunto: v0.1 al cerrar el sprint 2, v0.2 al cerrar el 3, y así sucesivamente. El APK va ahí, nunca dentro del repositorio.

## 4. Modelo de datos

El sistema maneja cuatro entidades (Sala, Usuario, Bloque y Reserva) con los mismos nombres de campo en MySQL, en el JSON de la API y en Kotlin.

**Reglas para todos los integrantes**

- Los nombres de campo se escriben **exactamente** como aparecen aquí, respetando mayúsculas y minúsculas: bloqueInicio, no bloque_inicio ni BloqueInicio.

- Fechas: texto con formato AAAA-MM-DD, por ejemplo "2026-10-05".

- Horas: texto con formato HH:MM de 24 horas, siempre con dos dígitos: "08:00", nunca "8:00".

- Los valores de listas cerradas (tipo, rol, estado) se escriben en MAYÚSCULAS y sin tildes.

- El id lo asigna la base de datos. La app nunca lo inventa.

### Sala

| Campo              | Tipo            | Ejemplo                     | Para qué sirve                                                                  |
|--------------------|-----------------|-----------------------------|---------------------------------------------------------------------------------|
| id                 | Entero          | 1                           | Identificador interno único. Nunca cambia                                       |
| codigo             | Texto           | "LAB-01"                    | Código legible para las personas                                                |
| nombre             | Texto           | "Laboratorio Computación 1" | Nombre que se muestra en la app                                                 |
| tipo               | Lista cerrada   | "LABORATORIO"               | Solo SALA_CLASES, LABORATORIO o MULTIPROPOSITO                                  |
| capacidad          | Entero          | 24                          | Máximo de alumnos. Siempre mayor que 0                                          |
| tieneProyector     | Verdadero/falso | true                        | Filtro de equipamiento                                                          |
| tieneComputadores  | Verdadero/falso | true                        | Filtro de equipamiento                                                          |
| tieneClimatizacion | Verdadero/falso | false                       | Filtro de equipamiento                                                          |
| activa             | Verdadero/falso | true                        | false = fuera de servicio. No aparece para reservar, pero conserva su historial |

### Usuario

| Campo    | Tipo          | Ejemplo                | Para qué sirve                                          |
|----------|---------------|------------------------|---------------------------------------------------------|
| id       | Entero        | 3                      | Identificador interno único                             |
| nombre   | Texto         | "Relator Ficticio Uno" | Nombre visible                                          |
| email    | Texto         | "relator1@duocuc.test" | Se usa para iniciar sesión. No se repite entre usuarios |
| rol      | Lista cerrada | "RELATOR"              | Solo RELATOR o COORDINADOR                              |
| password | Texto         | (nunca se muestra)     | Solo existe en el backend, guardada como hash           |

Dos reglas sobre la contraseña:

1.  **El backend nunca guarda la contraseña tal cual.** Guarda un **hash**: una transformación irreversible de la contraseña. Al iniciar sesión, el servidor transforma lo que escribió el usuario y compara ambos hashes.

2.  **La contraseña nunca viaja del servidor a la app.** Solo va de la app al servidor, en el login. Ninguna respuesta de la API incluye el campo password.

### Bloque

Esta tabla es el horario predeterminado. Por requisito del mandante, el coordinador podrá modificar el horario general y el de cada sala (sección 1); el diseño de esa edición está pendiente.

| numero | horaInicio | horaFin |
|--------|------------|---------|
| 1      | 08:00      | 09:00   |
| 2      | 09:00      | 10:00   |
| 3      | 10:00      | 11:00   |
| 4      | 11:00      | 12:00   |
| 5      | 12:00      | 13:00   |
| 6      | 13:00      | 14:00   |
| 7      | 14:00      | 15:00   |
| 8      | 15:00      | 16:00   |
| 9      | 16:00      | 17:00   |
| 10     | 17:00      | 18:00   |
| 11     | 18:00      | 19:00   |
| 12     | 19:00      | 20:00   |
| 13     | 20:00      | 21:00   |

El bloque 6 (almuerzo) también se puede reservar.

### Reserva

| Campo         | Tipo             | Ejemplo                    | Para qué sirve                                                                                         |
|---------------|------------------|----------------------------|--------------------------------------------------------------------------------------------------------|
| id            | Entero           | 31                         | Identificador único                                                                                    |
| salaId        | Entero           | 1                          | Sala reservada. Apunta al id de una Sala                                                               |
| usuarioId     | Entero           | 3                          | Quién reservó. Apunta al id de un Usuario. Lo asigna el servidor a partir del token                    |
| nombreRelator | Texto            | "Relator Ficticio Uno"     | Solo en respuestas de la API. Lo completa el servidor para que la Agenda general muestre quién reservó |
| actividad     | Texto            | "Taller de Excel avanzado" | Nombre del curso. No puede ir vacío                                                                    |
| fecha         | Texto (fecha)    | "2026-10-05"               | Día de la reserva                                                                                      |
| bloqueInicio  | Entero de 1 a 13 | 2                          | Primer bloque reservado                                                                                |
| bloqueFin     | Entero de 1 a 13 | 4                          | Último bloque reservado. Igual o mayor que bloqueInicio                                                |
| estado        | Lista cerrada    | "ACTIVA"                   | Solo ACTIVA o CANCELADA                                                                                |

Ejemplos: una reserva de un solo bloque tiene bloqueInicio y bloqueFin iguales. Un taller de 3 horas desde las 09:00 tiene bloqueInicio: 2 y bloqueFin: 4 (09:00 a 12:00).

### Regla de cruce de horario

Dos reservas se cruzan cuando se cumplen **las cuatro** condiciones:

1.  Son de la misma sala (salaId igual).

2.  Son de la misma fecha (fecha igual).

3.  Ambas están en estado ACTIVA.

4.  Sus bloques se superponen: bloqueInicio de la nueva ≤ bloqueFin de la existente **y** bloqueFin de la nueva ≥ bloqueInicio de la existente.

| Reserva existente        | Reserva nueva | ¿Cruce? | Por qué                                                         |
|--------------------------|---------------|---------|-----------------------------------------------------------------|
| Bloques 2 a 4            | Bloques 2 a 4 | Sí      | Mismos bloques                                                  |
| Bloques 2 a 4            | Bloques 4 a 5 | Sí      | Comparten el bloque 4                                           |
| Bloques 2 a 4            | Bloques 1 a 6 | Sí      | La nueva contiene a la existente                                |
| Bloques 2 a 4            | Bloques 5 a 6 | No      | La existente termina a las 12:00 y la nueva empieza a las 12:00 |
| Bloques 2 a 4, cancelada | Bloques 2 a 4 | No      | Las canceladas no cuentan                                       |

El backend es quien decide si hay cruce, porque es el único que ve las reservas de todos. La app también revisa antes de enviar, solo para avisar rápido al usuario.

## 5. Contrato 1: API REST (Backend ↔ Datos de la app)

La API tiene 11 endpoints repartidos en 3 microservicios; todos responden JSON y todos, salvo el login, exigen el token del usuario.

### Conceptos

Cada pedido de la app al servidor tiene tres partes:

**1. Método:** qué tipo de acción se pide.

| Método | Significa                | Ejemplo                                |
|--------|--------------------------|----------------------------------------|
| GET    | Obtener datos            | Pedir la lista de salas                |
| POST   | Crear algo nuevo         | Crear una reserva                      |
| PUT    | Modificar algo existente | Editar una sala o cancelar una reserva |

**2. Ruta:** la dirección dentro del servidor, por ejemplo /salas.

- {id} en una ruta se reemplaza por un número real: /salas/5 = la sala con id 5.

- Lo que va después de ? son filtros: /reservas?fecha=2026-10-05 = solo las reservas del 5 de octubre. Varios filtros se unen con &.

**3. Código de respuesta:** número con el que el servidor indica cómo le fue.

| Código | Significa       | Cuándo se usa                                                    |
|--------|-----------------|------------------------------------------------------------------|
| 200    | Todo bien       | Consulta o modificación exitosa                                  |
| 201    | Creado          | Reserva o sala creada                                            |
| 400    | Datos inválidos | Faltan campos, formato incorrecto o regla de negocio no cumplida |
| 401    | No autenticado  | Contraseña incorrecta, falta el token o el token venció          |
| 403    | Sin permiso     | Un relator intenta una acción de coordinador                     |
| 404    | No existe       | Se pide un id que no existe                                      |
| 409    | Conflicto       | Cruce de horario                                                 |

### Token (JWT)

1.  Al iniciar sesión correctamente, el servidor entrega un **token**: un texto largo que funciona como pase de acceso temporal.

2.  La app lo guarda y lo envía en **todos** los pedidos siguientes, en el encabezado Authorization con este formato exacto: Authorization: Bearer \<token\>.

3.  Con el token, el servidor sabe quién hace cada pedido y qué rol tiene. Si falta, es inválido o venció, responde 401 y la app vuelve a la pantalla de Login.

### Direcciones de los microservicios

| Microservicio | Puerto | Responsabilidad                          |
|---------------|--------|------------------------------------------|
| ms-usuarios   | 8081   | Login y usuarios                         |
| ms-salas      | 8082   | Catálogo de salas                        |
| ms-reservas   | 8083   | Bloques, reservas y validación de cruces |

### Endpoints

La columna «Quién» indica qué rol puede usar el endpoint. Si otro rol lo intenta, la respuesta es 403.

**ms-usuarios**

| Método | Ruta        | Quién                 | Recibe          | Responde                                     |
|--------|-------------|-----------------------|-----------------|----------------------------------------------|
| POST   | /auth/login | Cualquiera, sin token | email, password | 200 con token y usuario (sin password) · 401 |

**ms-salas**

| Método | Ruta                         | Quién       | Recibe                                                                                | Responde                                                                |
|--------|------------------------------|-------------|---------------------------------------------------------------------------------------|-------------------------------------------------------------------------|
| GET    | /salas                       | Ambos       | Filtros opcionales: capacidadMin, proyector, computadores, climatizacion (true/false) | 200 con las salas activas que cumplen los filtros                       |
| GET    | /salas?incluirInactivas=true | Coordinador | Mismos filtros                                                                        | 200 con todas las salas, activas e inactivas · 403 si lo usa un relator |
| GET    | /salas/{id}                  | Ambos       | Nada                                                                                  | 200 con una sala · 404                                                  |
| POST   | /salas                       | Coordinador | Todos los campos de Sala menos id                                                     | 201 con la sala creada · 400 · 403                                      |
| PUT    | /salas/{id}                  | Coordinador | Todos los campos de Sala menos id, incluido activa                                    | 200 · 400 · 403 · 404                                                   |

**ms-reservas**

| Método | Ruta                    | Quién                                   | Recibe                                            | Responde                                            |
|--------|-------------------------|-----------------------------------------|---------------------------------------------------|-----------------------------------------------------|
| GET    | /bloques                | Ambos                                   | Nada                                              | 200 con los 13 bloques                              |
| GET    | /reservas               | Ambos                                   | Filtros: fecha (obligatorio), salaId (opcional)   | 200 con las reservas ACTIVA que cumplen los filtros |
| GET    | /reservas/mias          | Ambos                                   | Filtro opcional: fecha                            | 200 con las reservas ACTIVA del usuario del token   |
| POST   | /reservas               | Ambos                                   | salaId, fecha, bloqueInicio, bloqueFin, actividad | 201 con la reserva creada · 400 · 409               |
| PUT    | /reservas/{id}/cancelar | El dueño de la reserva o el coordinador | Nada                                              | 200 con la reserva en estado CANCELADA · 403 · 404  |

POST /reservas **no recibe usuarioId**: el servidor lo obtiene del token. Si la app lo enviara, cualquiera podría reservar a nombre de otra persona cambiando ese número.

### Validaciones de POST /reservas, en este orden

1.  Todos los campos vienen y actividad no está vacía. Si no → 400 DATOS_INVALIDOS.

2.  bloqueInicio y bloqueFin están entre 1 y 13, y bloqueFin ≥ bloqueInicio. Si no → 400 BLOQUE_INVALIDO.

3.  fecha no es anterior a hoy. Si no → 400 FECHA_PASADA.

4.  La sala existe. Si no → 404 SALA_NO_ENCONTRADA. Para esto ms-reservas consulta a ms-salas.

5.  La sala está activa. Si no → 400 SALA_INACTIVA.

6.  No hay cruce según la regla de la sección 4. Si hay → 409 CRUCE_HORARIO.

7.  Todo bien → se guarda con estado: "ACTIVA" y responde 201.

### Formato estándar de errores

Toda respuesta de error (400, 401, 403, 404 y 409) tiene esta misma forma:

{  
"codigo": "CRUCE_HORARIO",  
"mensaje": "La sala LAB-01 ya está reservada en el bloque 2 del 2026-10-05"  
}

- codigo: identificador fijo que la app revisa para decidir qué hacer.

- mensaje: texto en español, pensado para mostrarse tal cual al usuario.

| Código de error        | HTTP | Cuándo                                            |
|------------------------|------|---------------------------------------------------|
| DATOS_INVALIDOS        | 400  | Faltan campos o tienen formato incorrecto         |
| BLOQUE_INVALIDO        | 400  | Bloques fuera de 1 a 13 o fin menor que inicio    |
| FECHA_PASADA           | 400  | Se intenta reservar un día anterior a hoy         |
| SALA_INACTIVA          | 400  | La sala está fuera de servicio                    |
| CREDENCIALES_INVALIDAS | 401  | Email o contraseña incorrectos                    |
| TOKEN_INVALIDO         | 401  | Falta el token, está alterado o venció            |
| SIN_PERMISO            | 403  | El rol no permite la acción                       |
| SALA_NO_ENCONTRADA     | 404  | No existe la sala pedida                          |
| RESERVA_NO_ENCONTRADA  | 404  | No existe la reserva pedida                       |
| CRUCE_HORARIO          | 409  | La sala ya está ocupada en alguno de esos bloques |

### Ejemplos completos

**Login.** La app envía POST /auth/login:

{  
"email": "relator1@duocuc.test",  
"password": "(contraseña de prueba)"  
}

Respuesta 200:

{  
"token": "eyJhbGciOi...",  
"usuario": {  
"id": 3,  
"nombre": "Relator Ficticio Uno",  
"email": "relator1@duocuc.test",  
"rol": "RELATOR"  
}  
}

**Crear una reserva.** La app envía POST /reservas con el token:

{  
"salaId": 1,  
"fecha": "2026-10-05",  
"bloqueInicio": 2,  
"bloqueFin": 4,  
"actividad": "Taller de Excel avanzado"  
}

Respuesta 201 si la sala está libre:

{  
"id": 31,  
"salaId": 1,  
"usuarioId": 3,  
"nombreRelator": "Relator Ficticio Uno",  
"fecha": "2026-10-05",  
"bloqueInicio": 2,  
"bloqueFin": 4,  
"actividad": "Taller de Excel avanzado",  
"estado": "ACTIVA"  
}

Respuesta 409 si la sala está ocupada: el JSON de error con codigo: "CRUCE_HORARIO".

## 6. Contrato 2: Pantallas (Datos de la app ↔ Interfaz)

La app tiene 8 pantallas; para cada una este contrato fija qué muestra, qué acciones permite, en qué estados puede estar y de qué endpoints salen sus datos.

### Cómo se reparte el trabajo en cada pantalla

- **Interfaz** dibuja la pantalla y detecta lo que hace el usuario.

- **Datos** entrega lo que la pantalla muestra y ejecuta las acciones: llamar al servidor, guardar en Room, validar.

- Entre ambos está el **ViewModel**: la pantalla le pide datos y le avisa lo que hizo el usuario. La pantalla nunca habla directamente con el servidor ni con Room.

### Estados de una pantalla

Interfaz debe diseñar cómo se ve cada estado que aparece en la pantalla:

| Estado    | Significa                              | Cómo se ve                                                            |
|-----------|----------------------------------------|-----------------------------------------------------------------------|
| Cargando  | Se esperan datos                       | Indicador de carga                                                    |
| Con datos | Todo normal                            | El contenido de la pantalla                                           |
| Vacío     | La consulta funcionó, pero no hay nada | Un mensaje, por ejemplo «No tienes reservas este día»                 |
| Error     | Algo falló                             | El mensaje del error y, si aplica, un botón «Reintentar»              |
| Guardando | Se envía un cambio al servidor         | Botón deshabilitado con indicador de carga, para evitar envíos dobles |

### Reglas generales

1.  **Sin conexión se consulta, pero no se reserva.** Sin internet, las pantallas muestran los datos guardados en Room con el aviso «Sin conexión: mostrando datos guardados». Crear o cancelar reservas exige conexión.

2.  **Bloques como horas.** El usuario nunca ve números de bloque: se muestran como horas. Bloques 2 a 4 = «09:00 – 12:00».

3.  **Sesión vencida.** Si cualquier pedido responde 401, la app borra la sesión y vuelve a Login con el mensaje «Tu sesión expiró».

4.  **Confirmar antes de cancelar.** Toda cancelación muestra primero un diálogo de confirmación.

### Navegación

- Al abrir la app: si hay una sesión guardada, va a **Mi agenda**; si no, a **Login**.

- **Barra inferior** (íconos fijos abajo de la pantalla) con tres secciones: **Mi agenda**, **Agenda general** y **Buscar salas**.

- Para el coordinador aparece una cuarta sección: **Salas**.

- Las pantallas Detalle de sala, Confirmar reserva y Formulario de sala se abren encima de una sección y tienen botón para volver.

### 1. Login

- **Muestra:** campos de email y contraseña (oculta), botón «Iniciar sesión».

- **Acciones:** iniciar sesión.

- **Estados:** inicial, guardando, error («Email o contraseña incorrectos» o «Sin conexión»). Si el login es exitoso, navega a Mi agenda.

- **Datos desde:** POST /auth/login. Datos guarda el token y el usuario en el teléfono.

### 2. Mi agenda (pantalla de inicio)

- **Muestra:** selector de fecha (hoy por defecto) y las reservas del usuario en esa fecha, cada una con nombre de sala, horario y actividad, ordenadas por hora. Arriba, el nombre del usuario.

- **Acciones:** cambiar fecha, cancelar una reserva, cerrar sesión.

- **Estados:** cargando, con datos, vacío («No tienes reservas este día»), error.

- **Datos desde:** GET /reservas/mias?fecha=..., GET /salas (nombre de la sala), GET /bloques (horas) y PUT /reservas/{id}/cancelar.

### 3. Agenda general

- **Muestra:** selector de fecha y una **grilla**: una fila por sala, una columna por cada uno de los 13 bloques. Las celdas ocupadas muestran la actividad; las libres quedan vacías. Es la vista de calendario del caso y da visibilidad de la ocupación a todo el personal.

- **Acciones:** cambiar fecha; tocar una celda ocupada para ver actividad, relator y horario; tocar una celda libre para ir al Detalle de esa sala en esa fecha. El coordinador, en el detalle de una celda ocupada, puede cancelar cualquier reserva.

- **Estados:** cargando, con datos, error.

- **Datos desde:** GET /salas, GET /bloques, GET /reservas?fecha=... y PUT /reservas/{id}/cancelar.

### 4. Buscar salas

- **Muestra:** filtros (fecha, capacidad mínima, proyector, computadores, climatización) y la lista de salas que los cumplen. Cada sala muestra código, nombre, capacidad, íconos de equipamiento y cuántos bloques libres tiene ese día.

- **Acciones:** cambiar filtros, tocar una sala.

- **Estados:** cargando, con datos, vacío («Ninguna sala cumple los filtros»), error.

- **Datos desde:** GET /salas con filtros y GET /reservas?fecha=... para contar los bloques libres.

### 5. Detalle de sala

- **Muestra:** datos de la sala, selector de fecha y los 13 bloques del día, cada uno marcado libre u ocupado.

- **Acciones:** cambiar fecha; seleccionar bloques libres **consecutivos** (no se puede seleccionar un rango con un bloque ocupado en medio); botón «Reservar», activo solo con al menos un bloque seleccionado.

- **Estados:** cargando, con datos, error.

- **Datos desde:** GET /salas/{id} y GET /reservas?fecha=...&salaId=....

### 6. Confirmar reserva

- **Muestra:** resumen (sala, fecha, horario) y un campo de texto para el nombre de la actividad.

- **Acciones:** confirmar, volver.

- **Estados:** inicial, guardando, éxito (vuelve a Mi agenda con el mensaje «Reserva creada»), error. Caso especial: **cruce** (409), cuando otra persona reservó esos bloques mientras este usuario llenaba el formulario. Se muestra el mensaje del error y un botón para volver al Detalle de sala, que se recarga.

- **Datos desde:** POST /reservas. Si es exitoso, Datos programa el recordatorio local 15 minutos antes del inicio.

### 7. Administrar salas (solo coordinador)

- **Muestra:** todas las salas, incluidas las inactivas, cada una marcada como activa o inactiva.

- **Acciones:** crear sala, editar sala, activar o desactivar sala.

- **Estados:** cargando, con datos, error.

- **Datos desde:** GET /salas?incluirInactivas=true y PUT /salas/{id}.

### 8. Formulario de sala (solo coordinador)

- **Muestra:** los campos de Sala: vacíos si es nueva, rellenos si se edita.

- **Acciones:** guardar, cancelar.

- **Estados:** inicial, guardando, éxito (vuelve a Administrar salas), error de validación (por ejemplo, capacidad 0 o código vacío).

- **Datos desde:** POST /salas o PUT /salas/{id}.

### Recordatorios

- Cada reserva creada programa una notificación local **15 minutos antes** de su inicio, con sala, horario y actividad.

- Si la reserva se cancela, su recordatorio se elimina.

- Desde Android 13, la app debe pedir permiso para mostrar notificaciones la primera vez que se abre.

## 7. Dataset ficticio

Como el docente no entregó datos, usamos este dataset propio: 9 salas, 7 usuarios y 30 reservas sin cruces, más 17 casos de prueba para validar la API.

**Dónde vive.** En docs/dataset/ del repositorio, como salas.json, usuarios.json y reservas.json. Es la única fuente: Backend lo carga en MySQL y Datos lo usa para probar Room en la Fase 3. Si alguien cambia el dataset, cambia estos archivos y avisa al equipo.

**Privacidad.** Todos los nombres, correos y actividades son inventados. La contraseña de prueba es la misma para todos los usuarios. Como el repositorio es público, no se escribe en ningún archivo del repositorio: ms-usuarios la lee desde la variable de entorno PASSWORD_PRUEBA, la guarda como hash BCrypt al cargar el dataset, y el Líder la comparte con el equipo por otro medio. Nunca debe ser una contraseña real de alguien del equipo.

### Salas

| id  | codigo | nombre                    | tipo           | capacidad | proyector | computadores | climatización | activa |
|-----|--------|---------------------------|----------------|-----------|-----------|--------------|---------------|--------|
| 1   | LAB-01 | Laboratorio Computación 1 | LABORATORIO    | 24        | sí        | sí           | sí            | sí     |
| 2   | LAB-02 | Laboratorio Electrónica   | LABORATORIO    | 16        | sí        | sí           | no            | sí     |
| 3   | SC-101 | Sala 101                  | SALA_CLASES    | 30        | sí        | no           | sí            | sí     |
| 4   | SC-102 | Sala 102                  | SALA_CLASES    | 25        | sí        | no           | no            | sí     |
| 5   | SC-201 | Sala 201                  | SALA_CLASES    | 40        | sí        | no           | sí            | sí     |
| 6   | SC-202 | Sala 202                  | SALA_CLASES    | 20        | no        | no           | no            | sí     |
| 7   | MP-01  | Auditorio                 | MULTIPROPOSITO | 80        | sí        | no           | sí            | sí     |
| 8   | MP-02  | Sala de reuniones         | MULTIPROPOSITO | 10        | sí        | no           | no            | sí     |
| 9   | SC-301 | Sala 301                  | SALA_CLASES    | 30        | sí        | no           | no            | **no** |

La sala 9 está inactiva a propósito, para probar el filtro incluirInactivas y el error SALA_INACTIVA.

### Usuarios

| id  | nombre                   | email                    | rol         |
|-----|--------------------------|--------------------------|-------------|
| 1   | Coordinador Ficticio Uno | coordinador1@duocuc.test | COORDINADOR |
| 2   | Coordinador Ficticio Dos | coordinador2@duocuc.test | COORDINADOR |
| 3   | Relator Ficticio Uno     | relator1@duocuc.test     | RELATOR     |
| 4   | Relator Ficticio Dos     | relator2@duocuc.test     | RELATOR     |
| 5   | Relator Ficticio Tres    | relator3@duocuc.test     | RELATOR     |
| 6   | Relator Ficticio Cuatro  | relator4@duocuc.test     | RELATOR     |
| 7   | Relator Ficticio Cinco   | relator5@duocuc.test     | RELATOR     |

El dominio .test está reservado para pruebas: ningún correo real puede tenerlo.

### Reservas

Dos semanas hábiles, del lunes 2 al viernes 13 de noviembre de 2026. Incluyen a propósito reservas pegadas en la misma sala (terminan justo cuando empieza otra) y dos canceladas.

| id  | fecha      | salaId | bloques | horario       | usuarioId | actividad                         | estado    |
|-----|------------|--------|---------|---------------|-----------|-----------------------------------|-----------|
| 1   | 2026-11-02 | 1      | 1 a 3   | 08:00 – 11:00 | 3         | Excel intermedio                  | ACTIVA    |
| 2   | 2026-11-02 | 1      | 4 a 5   | 11:00 – 13:00 | 4         | Python para análisis de datos     | ACTIVA    |
| 3   | 2026-11-02 | 3      | 2 a 4   | 09:00 – 12:00 | 5         | Liderazgo de equipos              | ACTIVA    |
| 4   | 2026-11-02 | 7      | 7 a 9   | 14:00 – 17:00 | 6         | Seminario de seguridad laboral    | ACTIVA    |
| 5   | 2026-11-03 | 2      | 2 a 5   | 09:00 – 13:00 | 7         | Electrónica básica                | ACTIVA    |
| 6   | 2026-11-03 | 1      | 11 a 13 | 18:00 – 21:00 | 3         | Excel avanzado vespertino         | ACTIVA    |
| 7   | 2026-11-03 | 4      | 1 a 2   | 08:00 – 10:00 | 4         | Inducción de nuevos colaboradores | ACTIVA    |
| 8   | 2026-11-03 | 8      | 6 a 6   | 13:00 – 14:00 | 1         | Reunión de coordinación           | ACTIVA    |
| 9   | 2026-11-04 | 5      | 2 a 4   | 09:00 – 12:00 | 5         | Comunicación efectiva             | ACTIVA    |
| 10  | 2026-11-04 | 1      | 2 a 4   | 09:00 – 12:00 | 6         | Power BI inicial                  | CANCELADA |
| 11  | 2026-11-04 | 1      | 2 a 3   | 09:00 – 11:00 | 3         | Excel intermedio                  | ACTIVA    |
| 12  | 2026-11-04 | 6      | 11 a 12 | 18:00 – 20:00 | 7         | Gestión del tiempo                | ACTIVA    |
| 13  | 2026-11-05 | 7      | 1 a 4   | 08:00 – 12:00 | 4         | Charla de ciberseguridad          | ACTIVA    |
| 14  | 2026-11-05 | 2      | 7 a 9   | 14:00 – 17:00 | 7         | Electrónica básica                | ACTIVA    |
| 15  | 2026-11-05 | 3      | 10 a 12 | 17:00 – 20:00 | 5         | Liderazgo de equipos              | ACTIVA    |
| 16  | 2026-11-06 | 1      | 7 a 10  | 14:00 – 18:00 | 6         | Power BI inicial                  | ACTIVA    |
| 17  | 2026-11-06 | 4      | 2 a 3   | 09:00 – 11:00 | 3         | Excel básico                      | ACTIVA    |
| 18  | 2026-11-06 | 8      | 5 a 5   | 12:00 – 13:00 | 1         | Reunión de coordinación           | ACTIVA    |
| 19  | 2026-11-09 | 1      | 1 a 3   | 08:00 – 11:00 | 3         | Excel intermedio                  | ACTIVA    |
| 20  | 2026-11-09 | 1      | 4 a 6   | 11:00 – 14:00 | 4         | Python para análisis de datos     | ACTIVA    |
| 21  | 2026-11-09 | 7      | 11 a 13 | 18:00 – 21:00 | 6         | Seminario de seguridad laboral    | ACTIVA    |
| 22  | 2026-11-10 | 3      | 2 a 4   | 09:00 – 12:00 | 5         | Comunicación efectiva             | ACTIVA    |
| 23  | 2026-11-10 | 2      | 2 a 5   | 09:00 – 13:00 | 7         | Electrónica básica                | ACTIVA    |
| 24  | 2026-11-10 | 6      | 8 a 9   | 15:00 – 17:00 | 3         | Excel básico                      | CANCELADA |
| 25  | 2026-11-11 | 1      | 7 a 10  | 14:00 – 18:00 | 6         | Power BI inicial                  | ACTIVA    |
| 26  | 2026-11-11 | 5      | 1 a 2   | 08:00 – 10:00 | 4         | Inducción de nuevos colaboradores | ACTIVA    |
| 27  | 2026-11-12 | 7      | 3 a 6   | 10:00 – 14:00 | 5         | Jornada de liderazgo              | ACTIVA    |
| 28  | 2026-11-12 | 4      | 11 a 13 | 18:00 – 21:00 | 7         | Gestión del tiempo                | ACTIVA    |
| 29  | 2026-11-13 | 1      | 2 a 4   | 09:00 – 12:00 | 3         | Excel avanzado                    | ACTIVA    |
| 30  | 2026-11-13 | 8      | 5 a 5   | 12:00 – 13:00 | 1         | Reunión de coordinación           | ACTIVA    |

En los archivos JSON, la columna «bloques» se guarda como dos campos: bloqueInicio y bloqueFin. La columna «horario» es solo referencia para leer esta tabla; no se guarda.

**Fechas.** La API rechaza reservas en días pasados. Si las pruebas o la presentación son después del 2 de noviembre, se corren todas las fechas hacia adelante la misma cantidad de semanas, para que los días sigan siendo de lunes a viernes.

### Casos de prueba de la API

Backend los guarda en la colección de Postman (docs/postman/). Todos usan el dataset de arriba y se corren con fechas futuras.

| \#  | Quién         | Pedido                                                   | Resultado esperado                                                 |
|-----|---------------|----------------------------------------------------------|--------------------------------------------------------------------|
| 1   | Relator 1     | Login con la contraseña de prueba                        | 200 con token                                                      |
| 2   | Relator 1     | Login con contraseña incorrecta                          | 401 CREDENCIALES_INVALIDAS                                         |
| 3   | Nadie         | GET /salas sin token                                     | 401 TOKEN_INVALIDO                                                 |
| 4   | Relator 1     | GET /salas                                               | 200 con 8 salas (sin la SC-301)                                    |
| 5   | Relator 1     | GET /salas?incluirInactivas=true                         | 403 SIN_PERMISO                                                    |
| 6   | Coordinador 1 | GET /salas?incluirInactivas=true                         | 200 con 9 salas                                                    |
| 7   | Relator 2     | Reservar sala 1, 2026-11-02, bloques 2 a 3               | 409 CRUCE_HORARIO (choca con la reserva 1)                         |
| 8   | Relator 2     | Reservar sala 1, 2026-11-02, bloques 5 a 6               | 409 CRUCE_HORARIO (comparte el bloque 5 con la reserva 2)          |
| 9   | Relator 2     | Reservar sala 1, 2026-11-02, bloques 6 a 7               | 201: empieza justo cuando termina la reserva 2                     |
| 10  | Relator 2     | Reservar sala 2, 2026-11-02, bloques 1 a 3               | 201: mismos bloques que la reserva 1, pero otra sala               |
| 11  | Relator 2     | Reservar sala 1, 2026-11-04, bloque 4 a 4                | 201: el bloque 4 solo lo ocupaba la reserva 10, que está cancelada |
| 12  | Relator 2     | Reservar con bloqueInicio 5 y bloqueFin 3                | 400 BLOQUE_INVALIDO                                                |
| 13  | Relator 2     | Reservar con bloqueFin 14                                | 400 BLOQUE_INVALIDO                                                |
| 14  | Relator 2     | Reservar la sala 9                                       | 400 SALA_INACTIVA                                                  |
| 15  | Relator 2     | Reservar la sala 99                                      | 404 SALA_NO_ENCONTRADA                                             |
| 16  | Relator 1     | PUT /reservas/3/cancelar (la reserva 3 es del Relator 3) | 403 SIN_PERMISO                                                    |
| 17  | Coordinador 1 | PUT /reservas/3/cancelar                                 | 200 con la reserva en estado CANCELADA                             |

Los casos 7 a 11 y 16 a 17 modifican datos: después de correrlos, se recarga el dataset original.

## 8. Fases del proyecto, paso a paso

El proyecto avanza en 8 fases; la Fase 4 (backend) corre en paralelo con las fases 2 y 3 (app), y todo se junta en la Fase 5.

Cada fase termina solo cuando se cumple su criterio «Terminada cuando». Las tareas de cada fase se crean como tarjetas en Trello antes de empezarla.

### Fase 0 · Preparación · Lidera: Líder

Objetivo: que los tres tengan las herramientas instaladas y acceso al repositorio y a Trello.

**Todos**

1.  Crear cuenta en GitHub y en Trello, si no tienen.

2.  Instalar Git y configurar su identidad (con su propio nombre y correo):

> git config --global user.name "Tu Nombre"  
> git config --global user.email "tu-correo@ejemplo.com"

3.  Instalar JDK 17.

4.  Aceptar la invitación al repositorio y al tablero.

5.  Clonar el repositorio: git clone \<url-del-repositorio\>.

Los comandos se escriben en **Git Bash**, la terminal que trae Git en Windows. En Windows 11, la opción «Open Git Bash here» del clic derecho está dentro de **Mostrar más opciones**, o se abre directo con Shift + clic derecho.

**Líder (Datos)**

1.  Crear el repositorio reserva-salas con la estructura de carpetas de la sección 3, un README.md inicial y un .gitignore para Android y Java.

2.  Agregar a los otros dos como colaboradores y crear la rama develop.

3.  Crear el tablero de Trello con columnas, etiquetas y tarjetas de las fases 0 y 1.

4.  En el teléfono de pruebas: revisar la versión de Android (Ajustes → Acerca del teléfono), activar el modo desarrollador (tocar 7 veces «Número de compilación») y activar la depuración por USB (Ajustes → Opciones de desarrollador).

5.  Instalar Android Studio.

**Backend**

1.  Instalar IntelliJ IDEA Community, MySQL Server con MySQL Workbench y Postman.

2.  El backend se escribe en Java con Spring Boot y Maven.

**Interfaz**

1.  Instalar Android Studio y crear un emulador (Device Manager → crear dispositivo virtual) con una versión reciente de Android. Crear un segundo emulador con Android 8.0 (API 26), la versión mínima que soporta la app.

2.  Opcional: crear cuenta en Figma para los bocetos.

**Terminada cuando:** cada integrante agregó su nombre al README en una rama feature/ fusionada en develop. Así los tres practican el flujo de la sección 3 antes de programar.

### Fase 1 · Diseño · Lidera: todos

Objetivo: cerrar el diseño. La mayor parte ya está en este documento.

**Líder (Datos)**

1.  Resolver los pendientes de la sección 1.

2.  Confirmar con el equipo el minSdk 26 (ya definido en la sección 1).

3.  Crear en Trello las tarjetas de las fases 2, 3 y 4.

**Backend**

1.  Revisar las secciones 4, 5 y 7 y confirmar puertos, validaciones y códigos de error.

2.  Escribir los archivos salas.json, usuarios.json y reservas.json en docs/dataset/, copiando la sección 7.

**Interfaz**

1.  Revisar la sección 6.

2.  Hacer bocetos de las 8 pantallas, en Figma o en papel fotografiado, y subirlos a docs/bocetos/.

**Terminada cuando:** los tres aprobaron este documento y los bocetos y el dataset están en el repositorio.

### Fase 2 · App base con datos falsos · Lidera: Interfaz

Objetivo: una app que se puede recorrer completa, con datos escritos en el código. En paralelo, Backend comienza la Fase 4.

**Líder (Datos)**

1.  Crear el proyecto en app-android/ con Android Studio: plantilla «Empty Activity» (Compose), nombre ReservaSalas, paquete cl.duoc.reservasalas.app, minSdk 26 (Android 8.0), y targetSdk y compileSdk en la versión más reciente que ofrezca Android Studio.

2.  Crear la estructura de paquetes MVVM:

> cl.duoc.reservasalas.app/  
> ├── model/ ← Sala, Usuario, Bloque, Reserva  
> ├── data/  
> │ ├── local/ ← Room (Fase 3)  
> │ ├── remote/ ← Retrofit (Fase 5)  
> │ └── repository/ ← repositorios  
> ├── viewmodel/ ← un ViewModel por pantalla  
> ├── ui/  
> │ ├── screens/ ← las 8 pantallas  
> │ ├── components/ ← piezas reutilizables  
> │ ├── navigation/ ← navegación y barra inferior  
> │ └── theme/ ← colores y tipografía  
> └── notifications/ ← recordatorios (Fase 6)

3.  Crear las clases del modelo (Sala, Usuario, Bloque, Reserva) con los campos exactos de la sección 4.

4.  Crear un repositorio falso que entrega el dataset de la sección 7 desde la memoria.

5.  Crear los ViewModels de las 8 pantallas con los estados de la sección 6, usando el repositorio falso.

**Interfaz**

1.  Definir el tema Material Design 3: colores, tipografía y formas.

2.  Programar la navegación y la barra inferior.

3.  Programar las 8 pantallas según los bocetos, conectadas a los ViewModels del líder.

4.  Diseñar cómo se ve cada estado (cargando, vacío, error, guardando).

**Terminada cuando:** se pueden recorrer las 8 pantallas en el teléfono antiguo, con datos falsos y sin cierres inesperados.

### Fase 3 · Persistencia local con Room · Lidera: Líder

Objetivo: que la app guarde datos en el teléfono y detecte cruces por sí misma.

**Líder (Datos)**

1.  Crear las entidades Room, los DAO (consultas) y la base de datos local.

2.  Reemplazar el repositorio falso por uno que usa Room, cargando el dataset la primera vez que se abre la app.

3.  Programar la regla de cruce de la sección 4 como validación local antes de guardar.

4.  Guardar la sesión (token y usuario) en el teléfono con DataStore.

**Interfaz**

1.  Agregar el selector de fecha y el diálogo de confirmación de cancelación.

2.  Revisar que cada estado se vea bien con datos reales de Room.

**Terminada cuando:** la app crea y cancela reservas, estas siguen ahí después de cerrar y abrir la app, y un cruce se rechaza con mensaje.

### Fase 4 · Backend · Lidera: Backend (en paralelo desde la Fase 2)

Objetivo: los 11 endpoints del Contrato 1 funcionando y probados con Postman.

**Backend**

1.  Crear en Spring Initializr (start.spring.io), dentro de backend/, los proyectos ms-salas y ms-reservas (Backend) y ms-usuarios (Líder): Java, Maven, paquete cl.duoc.reservasalas.\<nombre\> y las dependencias Spring Web, Spring Data JPA, MySQL Driver, Validation y Spring Security.

2.  Crear en MySQL una base de datos por microservicio: bd_usuarios, bd_salas y bd_reservas.

3.  **ms-usuarios (Líder):** entidad Usuario, contraseñas guardadas con hash BCrypt, POST /auth/login que entrega el JWT. El token incluye el id, nombre y rol del usuario.

4.  **ms-salas:** los endpoints de salas con sus filtros y permisos por rol.

5.  **ms-reservas:** los 13 bloques, los endpoints de reservas y las 7 validaciones en el orden de la sección 5. Consulta a ms-salas para verificar la sala. Al crear una reserva, guarda nombreRelator tomado del token.

6.  En ms-salas y ms-reservas, validar el JWT con la misma clave secreta de ms-usuarios. La clave se lee de la variable de entorno JWT_SECRET y nunca se sube al repositorio.

7.  Un manejador global de errores en cada microservicio, que responda siempre con el formato de la sección 5.

8.  Cargar el dataset de docs/dataset/ al arrancar.

9.  Crear la colección de Postman con los 17 casos de la sección 7 y guardarla en docs/postman/.

**Líder (Datos):** construye ms-usuarios (paso 3) y revisa los cambios del backend antes de que se fusionen en develop, para verificar que respeten el Contrato 1.

**Terminada cuando:** los 17 casos de prueba dan el resultado esperado.

### Fase 5 · Integración · Lidera: Líder

Objetivo: la app usa el backend real; Room queda como copia local para cuando no hay conexión.

**Líder (Datos)**

1.  Configurar Retrofit con una conexión por microservicio.

2.  Agregar el token a todos los pedidos (encabezado Authorization), de forma automática.

3.  Convertir las respuestas de error en el mensaje que muestra la interfaz.

4.  Cambiar los repositorios: piden al servidor, guardan la respuesta en Room y, sin conexión, leen de Room.

5.  Reemplazar el login falso por POST /auth/login y manejar el 401 (volver a Login).

**Backend:** dejar los microservicios accesibles desde la red local y apoyar la resolución de errores.

**Interfaz:** mostrar los mensajes de error del servidor y el aviso «Sin conexión».

**Errores típicos de esta fase**

- **El teléfono no encuentra localhost.** En el teléfono, localhost es el propio teléfono. Hay que usar la IP del computador en la red WiFi (por ejemplo 192.168.1.20), con ambos conectados a la misma red. El emulador usa la dirección especial 10.0.2.2.

- **Android bloquea http sin cifrar.** En desarrollo se permite solo para la IP local, mediante la configuración de seguridad de red de la app.

- **El firewall del computador bloquea los puertos.** Hay que permitir las conexiones entrantes a los puertos 8081, 8082 y 8083.

**Terminada cuando:** dos usuarios en dos dispositivos distintos (el teléfono y el emulador) ven la misma agenda, y cuando ambos intentan reservar la misma sala y bloque, uno lo logra y el otro recibe el mensaje de cruce.

### Fase 6 · Funciones completas · Lidera: Interfaz

Objetivo: completar todo lo que pide el caso.

**Interfaz**

1.  Grilla de la Agenda general.

2.  Filtros de Buscar salas.

3.  Selección de bloques consecutivos en Detalle de sala.

4.  Pantallas del coordinador: Administrar salas y Formulario de sala.

5.  Pulido visual general.

**Líder (Datos)**

1.  Recordatorios locales 15 minutos antes de cada reserva, con WorkManager o AlarmManager. Se eliminan si la reserva se cancela. Desde Android 12, las alarmas exactas de AlarmManager requieren un permiso especial; WorkManager no lo necesita, a cambio de que el aviso pueda llegar con unos minutos de diferencia.

2.  Pedir el permiso de notificaciones (Android 13 en adelante).

3.  ViewModels y repositorios de las pantallas del coordinador.

**Backend:** ajustes pedidos por el equipo durante la integración.

**Requisitos del mandante:** el check-in con QR, el estado no presentado, el aviso al coordinador y la edición de horarios y equipamiento se agregan a esta fase como tarjetas en cuanto su diseño esté en las secciones 4, 5 y 6.

**Al final, si alcanza el tiempo: notificaciones push.** Se usan con Firebase Cloud Messaging: Backend envía el aviso desde el servidor y el Líder lo recibe en la app. Es lo último porque los recordatorios locales ya cubren el objetivo principal.

**Terminada cuando:** las 8 pantallas están completas y el recordatorio llega al teléfono 15 minutos antes de una reserva de prueba.

### Fase 7 · Calidad y entrega · Lidera: todos

Objetivo: una versión estable, documentada y lista para presentar.

1.  **Pruebas:** correr los 17 casos de Postman y recorrer cada pantalla en el teléfono de pruebas y en el emulador con Android 8.0, con ambos roles. Cada error encontrado se anota como tarjeta y se corrige en una rama feature/.

2.  **Rendimiento en el teléfono antiguo:** la app abre sin demoras largas y la grilla de la Agenda general se desplaza con fluidez.

3.  **README completo:** qué es el proyecto, cómo levantar el backend, cómo instalar el APK, usuarios de prueba y cómo definir la variable PASSWORD_PRUEBA (sin escribir su valor).

4.  **APK firmado:** el Líder crea la llave de firma (Build → Generate Signed App Bundle or APK), la guarda **fuera** del repositorio con un respaldo privado, y genera el APK en modo release.

5.  **Probar el APK release** instalándolo en el teléfono: la versión release puede fallar en cosas que la versión de desarrollo no muestra.

6.  **Trello al día** y ensayo de la presentación, donde cada integrante explica su parte y sabe responder sobre las demás.

**Terminada cuando:** la checklist de la sección 9 está completa.

## 9. Checklist de entrega y glosario

El proyecto está listo para entregar cuando todos estos puntos están marcados.

### Checklist de entrega

**Funcionalidad**

- [ ] Login con email y contraseña, y cierre de sesión.

- [ ] Mi agenda muestra las reservas propias por fecha y permite cancelarlas.

- [ ] Agenda general muestra la ocupación de todas las salas por bloque.

- [ ] Buscar salas filtra por capacidad y equipamiento.

- [ ] Detalle de sala permite reservar bloques consecutivos libres.

- [ ] Un cruce de horario se rechaza con mensaje claro, incluso entre dos dispositivos.

- [ ] El coordinador crea, edita, activa y desactiva salas, y cancela cualquier reserva.

- [ ] El recordatorio llega 15 minutos antes de cada reserva.

- [ ] Sin conexión, la app muestra los datos guardados y el aviso correspondiente.

- [ ] El relator hace check-in escaneando el QR de la sala y el coordinador recibe el aviso.

- [ ] Sin check-in, la reserva queda como no presentado y la sala no se libera.

- [ ] El coordinador edita el horario general, el horario de cada sala y el equipamiento, y deja comentarios sobre el equipamiento.

**Calidad**

- [ ] Los 17 casos de Postman dan el resultado esperado.

- [ ] La app funciona con fluidez en el teléfono de pruebas.

- [ ] La app se instala y funciona en el emulador con Android 8.0 (versión mínima).

- [ ] Solo datos ficticios en el repositorio, la base de datos y las pruebas.

**Entregables**

- [ ] Repositorio en GitHub con todo el trabajo integrado en main desde develop.

- [ ] README completo.

- [ ] Tablero de Trello con todas las tarjetas en Terminado o justificadas.

- [ ] APK firmado en modo release, probado en el teléfono.

- [ ] Llave de firma respaldada fuera del repositorio.

- [ ] Presentación ensayada por los tres integrantes.

### Glosario

| Término          | Significado                                                                                                             |
|------------------|-------------------------------------------------------------------------------------------------------------------------|
| AGENTS.md        | Archivo con las instrucciones que leen los agentes de IA que programan en el repositorio                                |
| API REST         | Conjunto de direcciones del servidor a las que la app pide o envía datos                                                |
| APK              | Archivo instalador de una app Android                                                                                   |
| Backend          | La parte del sistema que corre en el servidor: guarda los datos y aplica las reglas                                     |
| Backlog          | Lista de todas las tareas del proyecto que aún no entran a un sprint                                                    |
| Bloque           | Cada una de las 13 horas reservables del día, de 08:00 a 21:00                                                          |
| Commit           | Registro guardado de un conjunto de cambios en Git, con un mensaje                                                      |
| compileSdk       | Versión de las herramientas de Android con que se compila la app. Va en la más reciente                                 |
| Contrato         | Acuerdo escrito sobre qué datos se intercambian entre dos partes del sistema y con qué forma                            |
| Cruce de horario | Dos reservas activas de la misma sala, el mismo día, con al menos un bloque en común                                    |
| DAO              | Clase de Room donde se escriben las consultas a la base de datos local                                                  |
| DataStore        | Almacenamiento simple de Android para datos pequeños, como la sesión                                                    |
| develop          | Rama de integración y pruebas generales. Recibe las ramas feature/ terminadas y pasa a main cuando el proyecto funciona |
| Endpoint         | Una dirección concreta de la API, con su método. Ejemplo: POST /reservas                                                |
| .gitignore       | Archivo con la lista de lo que Git nunca debe subir al repositorio                                                      |
| .gitkeep         | Archivo vacío que se pone en una carpeta para que Git la registre                                                       |
| Hash             | Transformación irreversible de una contraseña, para no guardarla tal cual                                               |
| JSON             | Formato de texto para enviar datos entre programas, con la forma "campo": valor                                         |
| JWT / token      | Pase de acceso temporal que entrega el servidor al iniciar sesión                                                       |
| Kanban           | Forma de trabajo donde las tareas avanzan por las columnas de un tablero, con un límite de tareas en progreso           |
| Keystore         | Archivo con la llave que firma el APK. Nunca se sube al repositorio                                                     |
| Microservicio    | Programa pequeño del backend con una sola responsabilidad                                                               |
| minSdk           | Versión más antigua de Android en la que la app se puede instalar. La nuestra es 26 (Android 8.0)                       |
| MVP              | Producto mínimo funcional: la versión más simple que resuelve el problema                                               |
| MVVM             | Forma de ordenar la app en capas: Vista (pantallas), ViewModel (estado y decisiones) y Modelo (datos)                   |
| Nivel de API     | Número interno de cada versión de Android. Ejemplo: Android 8.0 = API 26, Android 12 = API 31                           |
| Product Owner    | Rol de Scrum que prioriza las tareas y decide qué entra en cada sprint. En este equipo, el líder                        |
| Rama (branch)    | Copia paralela del código para trabajar sin afectar develop ni main                                                     |
| Release          | Versión publicada en GitHub al cerrar un sprint, con el APK adjunto                                                     |
| Retrofit         | Librería de Android para conectarse a la API REST                                                                       |
| Room             | Librería de Android para la base de datos local del teléfono                                                            |
| Scrum            | Forma de trabajo en periodos fijos (sprints) con planificación, revisión y retrospectiva                                |
| Sprint           | Periodo fijo de trabajo, de 2 semanas en este proyecto, con una meta concreta                                           |
| targetSdk        | Versión de Android para la que la app está diseñada y probada. Va en la más reciente y no excluye teléfonos antiguos    |
| ViewModel        | Clase que guarda el estado de una pantalla y ejecuta sus acciones                                                       |
