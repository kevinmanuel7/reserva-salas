# AGENTS.md — reserva-salas

Instrucciones para agentes de IA que trabajen en este repositorio.
Este archivo contiene las reglas generales. Cada carpeta principal tiene su propio
`AGENTS.md` con reglas específicas:

- `app-android/AGENTS.md` → reglas de la app Android (Kotlin).
- `backend/AGENTS.md` → reglas del backend (Java + Spring Boot).

Si una regla de una carpeta contradice este archivo, manda la regla de la carpeta.

---

## 1. Qué es el proyecto

Aplicación Android para reservar espacios de Duoc UC (salas, sala de graduaciones,
capilla, entre otros). Es el proyecto semestral del ramo Desarrollo de Aplicaciones
Móviles y tiene un cliente real (el mandante es el propio Duoc UC).

El proyecto compite contra otros equipos, por lo que la calidad, la estabilidad y la
documentación importan tanto como las funcionalidades.

## 2. Estructura del repositorio

| Carpeta        | Contenido                                                        |
|----------------|------------------------------------------------------------------|
| `app-android/` | App Android en Kotlin.                                           |
| `backend/`     | Microservicios en Java con Spring Boot.                          |
| `docs/`        | `plan-proyecto.md` (plan completo), `dataset/`, `postman/` y `bocetos/`. |

## 3. Tecnologías

- App: Kotlin, Android, con Jetpack Compose.
- Backend: Java, Spring Boot.
- Base de datos del backend: MySQL.
- La app debe soportar una versión mínima de Android baja, para funcionar también en
  teléfonos antiguos. El número exacto está en `app-android/AGENTS.md`.

## 4. Modelo de datos y contratos

El detalle está en `docs/plan-proyecto.md`: modelo de datos (sección 4),
Contrato 1 de la API (sección 5), contrato de pantallas (sección 6),
dataset y casos de prueba (sección 7) y fases (sección 8). Leerlo antes de
crear clases, tablas o endpoints.

- Entidades: Sala, Usuario, Bloque y Reserva.
- Los nombres de campo son idénticos en MySQL, en el JSON de la API y en Kotlin,
  respetando mayúsculas y minúsculas (`bloqueInicio`, nunca `bloque_inicio`).
- Fechas: texto `AAAA-MM-DD`. Horas: texto `HH:MM` de 24 horas, siempre con dos dígitos.
- Valores de listas cerradas (`tipo`, `rol`, `estado`): MAYÚSCULAS y sin tildes.
- El `id` lo asigna la base de datos.
- Si este archivo y el plan se contradicen, preguntar antes de programar.

## 5. Reglas de negocio (decididas con el mandante — no cambiarlas sin aviso)

### Perfiles
- Solo existen dos perfiles: **relator** y **coordinador**.
- El **coordinador** es el administrador. Puede:
  - editar el horario general de bloques;
  - editar el horario propio de cada sala;
  - editar el equipamiento de cada sala;
  - dejar comentarios sobre el equipamiento (ejemplo: "el proyector del teatro tiene fallas").
- El **relator** reserva espacios y hace check-in.

### Inicio de sesión
- Login real con correo/usuario y contraseña. No es simulado.
- No hay inicio de sesión con Google (descartado).

### Reservas
- Se reserva por **bloques fijos de 1 hora**. No existe horario libre.
- Horario predeterminado: de **08:00 a 21:00**.
- El horario se define al comenzar cada semestre y puede cambiar, por eso debe ser
  configurable por el coordinador y no estar escrito fijo en el código.
- Se envía un **recordatorio 15 minutos antes** del inicio de la reserva.

### Check-in
- El relator confirma el uso del espacio escaneando el **código QR de la sala**.
- El check-in **notifica al coordinador**, para que pueda ir a verificar el uso real.
- Si el relator no hace check-in, la reserva se marca como **no presentado**.
  La sala **no** se libera.

### Espacios
- Por ahora el catálogo de espacios y equipamiento es provisional (inventado por el
  equipo). Se reemplazará por la lista real que entregue el mandante.

### Fuera de alcance (NO implementar todavía)
- Sistema de calificación de relatores.

## 6. Equipo y responsabilidades

| Integrante | Responsabilidad                                                        |
|------------|------------------------------------------------------------------------|
| Kevin      | Líder, documentación, microservicio ms-usuarios e integración app ↔ backend. |
| Bastián    | Backend: microservicios ms-salas y ms-reservas.                        |
| Simón      | Interfaz de la app.                                                    |

Antes de modificar código de un área que no corresponde a quien está trabajando,
el agente debe avisarlo y pedir confirmación.

## 7. Cómo debe trabajar el agente

1. **Plan primero.** Antes de escribir código, proponer un plan: qué archivos se crean
   o modifican y por qué. Esperar aprobación.
2. **Cambios pequeños.** Implementar una parte a la vez (por ejemplo, una entidad, un
   endpoint o una pantalla), no una funcionalidad completa de una sola vez.
3. **Explicar.** Al terminar cada parte, explicar en español y de forma simple qué se
   hizo, en qué archivo está y por qué. Los integrantes están aprendiendo; ninguna
   sintaxis debe darse por conocida.
4. **Verificar.** Compilar y ejecutar las pruebas de la carpeta modificada antes de dar
   la tarea por terminada.
5. **No inventar reglas de negocio.** Si algo no está definido en este archivo ni en
   `docs/plan-proyecto.md`, preguntar en vez de suponer.

## 8. Seguridad

- **El repositorio es público.** Nunca escribir contraseñas, claves de base de datos,
  tokens ni claves de API en el código, en la configuración ni en estos archivos.
  Usar variables de entorno y dejar solo archivos de ejemplo sin valores reales.
- Nunca guardar contraseñas de usuarios en texto plano; siempre guardarlas cifradas
  con un algoritmo de hash seguro.

## 9. Git

No se usan Pull Requests. Se trabaja con tres niveles de ramas:

| Rama                      | Para qué                                                          |
|---------------------------|-------------------------------------------------------------------|
| `main`                    | Versión estable. Recibe `develop` solo cuando el proyecto esté funcionando y el equipo lo decida. |
| `develop`                 | Integración y pruebas generales de todo el equipo.                |
| `feature/descripcion-corta` | Una rama por tarea (ejemplo: `feature/login-backend`). Se crea desde `develop`. |

- Al terminar una tarea y comprobar que compila, la rama `feature/...` se fusiona
  en `develop`.
- Nunca se trabaja directamente sobre `develop` ni sobre `main`.
- El agente **nunca** hace `merge` ni `push` a `develop` o a `main` sin pedir
  confirmación.
- Mensajes de commit en español, en presente y describiendo el cambio
  (ejemplo: `Agrega entidad Usuario y su repositorio`).

## 10. Documentación

- Toda decisión nueva del mandante o del equipo se registra en `docs/`.
- Si una decisión cambia una regla de la sección 5, actualizar también este archivo.
