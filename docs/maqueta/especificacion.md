# Maqueta app Reserva de Salas — Pantallas por integrante

Oct 15, 2026 · @kevin

## Resumen

El miércoles 28 de octubre presentamos una maqueta funcional del front: login funcional, interfaz principal, home y funcionalidades adicionales, sin backend: toda la app, incluido el login, funciona con datos falsos. En la próxima evaluación se conectan los repositorios reales sin tocar las pantallas. Cada integrante es dueño de un flujo completo de pantallas.

| Integrante | Flujo | Pantallas |
| --- | --- | --- |
| Kevin | Entrada y estructura | Login, estructura (barras), Perfil, Check-in con cámara, tema y componentes compartidos, datos falsos |
| Simón | Relator (y reservas del coordinador) | Inicio relator, Salas, Detalle de sala, Reservar, Reserva confirmada, Mi agenda, Detalle de reserva |
| Bastián | Coordinador | Inicio coordinador, Editar sala, Reservas del día, Supervisar reserva, Horario general |

El coordinador también puede reservar: usa las mismas pantallas de Salas, Reservar y Mi agenda que el relator. Si el mandante pide cambios, se aplican para la evaluación siguiente.

## Reglas comunes

Todas las pantallas usan solo los colores y componentes de esta sección. Nadie crea botones ni colores propios: si falta algo, se le pide a Kevin para agregarlo a la base.

### Colores

| Nombre | Hex | Uso |
| --- | --- | --- |
| Negro Duoc | #2D2926 | Barra superior, barra inferior, títulos, cabeceras de tarjetas |
| Gris 85% | #4D4845 | Textos secundarios, íconos inactivos |
| Amarillo Duoc | #FFB81C | Botón principal, ítem activo de la barra inferior, línea bajo la barra superior |
| Amarillo suave | #FFF3CC | Elemento seleccionado, chip "Reservada" |
| Fondo | #F2F3F5 | Fondo de todas las pantallas |
| Superficie | #FFFFFF | Tarjetas y formularios |
| Borde | #DDDDDD | Bordes de tarjetas y campos |
| Error | #D32F2F | Botón de peligro, mensajes de error, chip "No presentado" |
| Éxito | #2E7D32 | Chips "En uso" y "Verificada" |

Los hex de Pantone son aproximados. El amarillo nunca va como texto sobre blanco: va como relleno con texto negro, o como texto sobre negro. Logo: variante blanca de Duoc UC Sede Viña del Mar sobre la barra negra (pedir el archivo PNG o SVG al mandante).

### Generales

- Solo modo claro, solo orientación vertical, todo en español.
- Tipografía del sistema (Roboto). Íconos de Material Icons (dependencia material-icons-extended).
- Todos los datos vienen de repositorios falsos, incluido el login. La app no hace llamadas de red en esta etapa.

### Componentes compartidos (los hace Kevin)

| Componente | Descripción |
| --- | --- |
| BarraSuperior | Fondo negro, logo blanco a la izquierda, título de la pantalla, línea amarilla de 4 dp abajo. Flecha atrás en pantallas secundarias. |
| BarraInferior | Fondo negro, ítem activo en amarillo, inactivos en gris claro. 4 ítems para relator, 5 para coordinador. |
| BotonPrincipal | Relleno amarillo, texto negro en negrita, esquinas redondeadas. |
| BotonSecundario | Fondo blanco, contorno y texto negro. |
| BotonPeligro | Fondo blanco, contorno y texto rojo. |
| TarjetaSala | Código en negrita, nombre, capacidad con ícono de personas, chips de equipamiento, chip rojo "Con falla" si algún equipo falla. |
| ChipEstado | Muestra uno de los 5 estados de reserva con su color. |
| ChipEquipo | Chip gris claro con el nombre del equipo. |
| TarjetaMetrica | Número grande en negro y texto descriptivo pequeño debajo. |
| AvisoDestacado | Tarjeta con borde izquierdo amarillo, ícono de campana y texto. Se usa para el recordatorio. |

### Estados de reserva

| Estado | Chip | Cuándo |
| --- | --- | --- |
| Reservada | Amarillo suave, texto negro | Creada y aún no empieza |
| En uso | Verde, texto blanco | El relator hizo check-in |
| Verificada | Contorno verde, texto verde | El coordinador confirmó en persona |
| No presentado | Rojo, texto blanco | El coordinador lo marcó |
| Cancelada | Gris #9E9E9E, texto blanco | El dueño la canceló |

### Reglas de reserva

- Bloques entre 08:00 y 21:00 con 5 minutos de pausa entre uno y otro, para que un grupo salga antes de que entre el siguiente: 08:00–09:00, 09:05–10:00, 10:05–11:00 … 20:05–21:00 (13 bloques, igual que en la versión web). Una reserva de varios bloques se muestra de corrido, del inicio del primero al fin del último (ej. 10:05–12:00).
- De 1 a 4 bloques consecutivos del mismo día por reserva.
- Hasta 14 días hacia adelante.
- El dueño puede cancelar mientras la reserva no haya empezado.
- Check-in habilitado desde 10 minutos antes hasta 15 minutos después del inicio.
- Sin check-in, la reserva se muestra como "Sin check-in" y el coordinador la marca como "No presentado" (no es automático).
- Recordatorio: aviso dentro de la app 15 minutos antes (simulado, no es notificación del sistema).

### Rutas de navegación

| Ruta | Pantalla | Dueño |
| --- | --- | --- |
| login | Login | Kevin |
| inicioRelator | Inicio del relator | Simón |
| inicioCoordinador | Inicio del coordinador | Bastián |
| salas | Lista de salas | Simón |
| sala/{codigo} | Detalle de sala | Simón |
| reservar/{codigo} | Reservar | Simón |
| reservaConfirmada/{idReserva} | Reserva confirmada | Simón |
| agenda | Mi agenda | Simón |
| reserva/{idReserva} | Detalle de reserva | Simón |
| checkin/{idReserva} | Check-in | Kevin |
| editarSala/{codigo} | Editar sala | Bastián |
| reservasDia | Reservas del día | Bastián |
| supervisar/{idReserva} | Supervisar reserva | Bastián |
| horarioGeneral | Horario general | Bastián |
| perfil | Perfil | Kevin |

{codigo} es el código de la sala (ej. S-201) e {idReserva} el número de la reserva. Estos nombres son definitivos.

## Pantallas de Kevin

Kevin entrega la base (tema, componentes, estructura, rutas y datos falsos) fusionada en develop a más tardar el domingo 18. Los demás construyen encima de ella. Kevin también hace el Check-in con cámara: está detallado como pantalla 8 en la sección de Simón, porque es parte del flujo del relator.

### 1. Login (ruta: login)

Se rescata la pantalla y el ViewModel que ya existen y se adaptan al nuevo diseño. En esta etapa el login valida contra los 7 usuarios de prueba en un repositorio falso (sin ms-usuarios): rechaza contraseñas incorrectas, muestra errores y guarda la sesión.

- Fondo negro en la mitad superior con el logo blanco de Duoc UC Sede Viña del Mar centrado.
- Debajo del logo, el título "Reserva de Salas" en blanco y una línea amarilla.
- Mitad inferior: tarjeta blanca con:
  - Campo "Correo institucional" con ícono de sobre.
  - Campo "Contraseña" con ícono de candado y botón de ojo para mostrar u ocultar.
  - Mensaje de error en rojo bajo los campos, con el texto que entrega el repositorio (ej. "Correo o contraseña incorrectos").
  - BotonPrincipal "Ingresar" a todo el ancho; muestra un indicador de carga mientras espera.
- Pie de pantalla en gris: "Datos ficticios con fines académicos".
- Al ingresar: si el rol es RELATOR va a inicioRelator; si es COORDINADOR va a inicioCoordinador.
- Si hay sesión guardada, la app salta el login y entra directo al inicio según el rol.

### 2. Estructura principal (no es una ruta, envuelve las pantallas)

- BarraSuperior en todas las pantallas excepto Login.
- BarraInferior visible solo en las pantallas principales (las de cada ítem); se oculta en pantallas secundarias como Detalle o Reservar.
- Ítems de la barra inferior del relator: Inicio (ícono casa) · Salas (ícono sala) · Mi agenda (ícono calendario) · Perfil (ícono persona).
- Ítems de la barra inferior del coordinador: Inicio · Salas · Reservas (ícono lista con check) · Mi agenda · Perfil.
- El ítem activo se pinta amarillo; tocar el ítem activo vuelve al inicio de esa sección.

### 3. Perfil (ruta: perfil)

- Tarjeta con círculo de iniciales (fondo amarillo, letras negras), nombre completo, correo y chip con el rol ("Relator" o "Coordinador").
- Sección "Mi actividad" con dos TarjetaMetrica: reservas este mes y check-in realizados.
- Solo coordinador: botón de lista "Horario general de bloques" con flecha, que lleva a horarioGeneral.
- Texto informativo: "Sede Viña del Mar · Datos ficticios con fines académicos".
- BotonPeligro "Cerrar sesión" al final, con diálogo de confirmación ("¿Seguro que quieres cerrar sesión?" con Cancelar y Cerrar sesión). Al confirmar, borra la sesión y vuelve a login.

### 4. Base técnica

- Tema con los colores de Reglas comunes.
- Los 10 componentes compartidos.
- Las 15 rutas de navegación, con pantallas vacías de relleno para que cada uno solo reemplace la suya.
- Repositorios falsos con los datos de la sección Datos falsos.
- Repositorio de sesión falso para el login, con los 7 usuarios de prueba.

## Pantallas de Simón

Simón hace el flujo de reservar y de usar una reserva. Todas sus pantallas sirven para ambos roles: cuando algo cambia según el rol, se indica.

### 1. Inicio del relator (ruta: inicioRelator)

- Saludo: "Hola, {primer nombre}" en negrita y la fecha de hoy debajo (ej. "Jueves 15 de octubre").
- AvisoDestacado del recordatorio, solo si hay una reserva que empieza en 15 minutos o menos: "Tu reserva en {sala} empieza a las {hora}". Tocarlo lleva a reserva/{idReserva}.
- Tarjeta "Tu próxima reserva": nombre y código de la sala, fecha, Rango de horas (ej. 10:05–12:00), ChipEstado. Si el check-in está habilitado, BotonPrincipal "Hacer check-in" que lleva a checkin/{idReserva}. Si no hay reservas: "No tienes reservas próximas".
- BotonPrincipal grande "Reservar sala" que lleva a salas.
- Sección "Esta semana": lista corta (máximo 3) de las siguientes reservas, cada una con sala, día y hora; "Ver todas" lleva a agenda.

### 2. Salas (ruta: salas)

- Buscador arriba: "Buscar por nombre o código".
- Fila de chips de filtro por equipamiento: Proyector, Computadores, Climatización. Se pueden marcar varios.
- Lista de TarjetaSala, ordenada por código.
- Tocar una tarjeta lleva a sala/{codigo}.
- Si el filtro no deja resultados: "No hay salas con esos filtros".
- Igual para ambos roles.

### 3. Detalle de sala (ruta: sala/{codigo})

- BarraSuperior con el código de la sala y flecha atrás.
- Cabecera: nombre de la sala, código, capacidad ("25 personas") y ubicación (ej. "Edificio A, piso 2").
- Sección "Equipamiento": lista de cada equipo con ícono, nombre y estado ("Operativo" en verde o "Con falla" en rojo). Si tiene comentario del coordinador, se muestra debajo en gris.
- Sección "Disponibilidad de hoy": fila horizontal de los bloques del día (08:00 a 20:00), libres en blanco con borde y ocupados en gris.
- BotonPrincipal "Reservar" fijo abajo, lleva a reservar/{codigo}.
- Solo coordinador: BotonSecundario "Editar sala" junto a Reservar, lleva a editarSala/{codigo}.

### 4. Reservar (ruta: reservar/{codigo})

- BarraSuperior "Reservar {código}" con flecha atrás.
- Selector de día: fila horizontal de los próximos 14 días (día de la semana y número). El seleccionado va en amarillo.
- Grilla de bloques del día elegido (08:00, 09:05, 10:05 … 20:05; cada uno muestra solo su hora de inicio). Estados de cada bloque: libre (blanco con borde), ocupado (gris, no se puede tocar), seleccionado (amarillo).
- Se pueden seleccionar de 1 a 4 bloques consecutivos. Si intenta saltarse uno o pasar de 4, aparece un mensaje breve explicando la regla.
- Campo opcional "Motivo" (ej. "Clase de Programación Móvil").
- Resumen abajo: "S-201 · Jueves 15 · 10:05–12:00 (2 bloques)".
- BotonPrincipal "Confirmar reserva", deshabilitado si no hay bloques elegidos. Al tocarlo, guarda la reserva en el repositorio falso y va a reservaConfirmada/{idReserva}.

### 5. Reserva confirmada (ruta: reservaConfirmada/{idReserva})

- Ícono grande de check en círculo verde.
- Título "¡Reserva confirmada!".
- Tarjeta con sala, fecha, horario, motivo y ChipEstado "Reservada".
- Texto: "Te recordaremos 15 minutos antes. Recuerda hacer check-in al llegar escaneando el QR de la sala."
- BotonPrincipal "Ver mi agenda" (va a agenda) y BotonSecundario "Volver al inicio".
- Sin flecha atrás: no debe poder volver a la pantalla de Reservar.

### 6. Mi agenda (ruta: agenda)

- Pestañas arriba: "Próximas" y "Historial".
- Próximas: reservas agrupadas por día con encabezado ("Hoy", "Mañana", "Lunes 19 de octubre"). Cada una en tarjeta con sala, horario y ChipEstado.
- Historial: reservas pasadas y canceladas, con su estado final (Verificada, No presentado, Cancelada).
- Tocar una tarjeta lleva a reserva/{idReserva}.
- Si no hay reservas: "Aún no tienes reservas" y BotonPrincipal "Reservar sala".
- Igual para ambos roles: muestra solo las reservas del usuario conectado.

### 7. Detalle de reserva (ruta: reserva/{idReserva})

- Tarjeta con sala (tocar el nombre lleva a sala/{codigo}), fecha, horario, motivo, ChipEstado y fecha en que se creó.
- Si está habilitado el check-in: BotonPrincipal "Hacer check-in" que lleva a checkin/{idReserva}.
- Si aún no empieza: texto "Check-in disponible desde las {hora menos 10 min}".
- Si aún no empieza: BotonPeligro "Cancelar reserva" con diálogo de confirmación. Al confirmar, el estado pasa a Cancelada.
- Si ya terminó: solo información, sin botones.

### 8. Check-in (ruta: checkin/{idReserva}) · a cargo de Kevin

Requisito del profesor: usar la cámara como recurso nativo, pidiendo el permiso al sistema. Se implementa con el mismo método de la guía de clases "Cámara en el registro" más ML Kit para leer el QR de la foto. Es imprescindible.

- Tarjeta con la reserva: sala, horario y "Sala esperada: {código}".
- Texto: "Toma una foto del código QR pegado en la puerta de la sala. La primera vez, la app te pedirá permiso para usar la cámara."
- BotonPrincipal "Escanear QR de la sala" con ícono de cámara. Al pulsarlo:
  1. Si el teléfono no tiene cámara: aviso "Este teléfono no tiene cámara".
  2. Si el permiso CAMERA no está concedido: se abre el diálogo del sistema (RequestPermission). Si lo rechaza: aviso "Sin permiso de cámara no se puede hacer check-in" y queda disponible el ingreso manual.
  3. Con permiso: se crea el archivo en cache/fotos y se abre la cámara del sistema (TakePicture).
  4. Si cancela la foto: la pantalla queda como estaba.
  5. Con la foto: ML Kit lee el QR y se compara con la sala esperada.
- Resultados de la lectura:
  - QR correcto (ej. SALA:S-201): check verde y "Check-in realizado en {sala} a las {hora}". El estado pasa a En uso y se agrega un aviso para el coordinador.
  - QR de otra sala: error "Este QR es de la sala {otra}. Tu reserva es en {esperada}."
  - Sin QR en la foto: error "No se encontró un código QR. Intenta de nuevo con buena luz."
- Botón de texto "Ingresar código manualmente": campo para escribir el código de la sala. Sirve si el QR está dañado o se negó el permiso, y es el respaldo para la demo.
- BotonSecundario "Volver" que regresa a reserva/{idReserva}.
- Archivos que cambian: AndroidManifest.xml (permiso CAMERA, uses-feature con required="false", FileProvider con autoridad cl.duoc.reservasalas.app.fileprovider), res/xml/file\_paths.xml (carpeta fotos en caché), el ViewModel del check-in (crear el archivo y leer el QR) y la pantalla.
- Dependencia nueva: com.google.mlkit:barcode-scanning (versión incluida en la app, sin internet).
- Para la demo: imprimir un QR por sala con el texto SALA:{código}.

## Pantallas de Bastián

Bastián hace el flujo de supervisión del coordinador. Imprescindibles: Inicio, Editar sala y Reservas del día. Deseables: Supervisar reserva y Horario general (si el miércoles 21 va atrasado, quedan para después de la presentación).

### 1. Inicio del coordinador (ruta: inicioCoordinador) · imprescindible

- Cabecera negra con línea amarilla (como en la web): "Panel del coordinador" y debajo "Sede Viña del Mar · {fecha de hoy}".
- Cuadrícula 2×2 de TarjetaMetrica:
  - Reservas para hoy.
  - En uso ahora (estado En uso).
  - Sin check-in (empezaron hace más de 15 minutos sin check-in).
  - Salas con fallas (al menos un equipo Con falla).
- Tocar "Sin check-in" o "Reservas para hoy" lleva a reservasDia. Tocar "Salas con fallas" lleva a salas.
- Sección "Avisos recientes": lista de check-in de las últimas horas. Cada aviso: ícono QR, "{relator} hizo check-in en {sala}", hora, y punto amarillo si no se ha revisado. Tocar un aviso lleva a supervisar/{idReserva}.
- Si no hay avisos: "Sin avisos por ahora".

### 2. Editar sala (ruta: editarSala/{codigo}) · imprescindible

Se llega desde el botón "Editar sala" del Detalle de sala.

- BarraSuperior "Editar {código}" con flecha atrás.
- Sección "Datos" (solo lectura en esta versión): código, nombre y capacidad.
- Sección "Horario de la sala": interruptor "Usar horario general" (activo por defecto). Al desactivarlo aparecen dos selectores de hora: apertura y cierre (ej. 09:00 a 18:00), solo en horas exactas entre 08:00 y 21:00.
- Sección "Equipamiento": una tarjeta por equipo con:
  - Nombre e ícono del equipo.
  - Selector de estado: Operativo / Con falla.
  - Campo de texto "Comentario" (ej. "La imagen parpadea"), obligatorio si el estado es Con falla.
- Botón de texto "+ Agregar equipo" que abre un diálogo para elegir del catálogo (Proyector, Computadores, Climatización, Pizarra, Parlantes).
- Abajo: BotonSecundario "Cancelar" y BotonPrincipal "Guardar cambios" (como en la web). Al guardar, muestra "Cambios guardados" y vuelve al Detalle de sala con los datos actualizados.

### 3. Reservas del día (ruta: reservasDia) · imprescindible

Es el ítem "Reservas" de la barra inferior del coordinador.

- Selector de fecha arriba: "Hoy" por defecto, con flechas para día anterior y siguiente.
- Chips de filtro por estado: Todas, Reservada, En uso, Sin check-in, Verificada, No presentado.
- Lista ordenada por hora de inicio. Cada tarjeta: horario, sala, nombre del relator, ChipEstado.
- Las reservas sin check-in se resaltan con borde izquierdo rojo.
- Tocar una tarjeta lleva a supervisar/{idReserva}.
- Contador arriba de la lista: "{n} reservas".

### 4. Supervisar reserva (ruta: supervisar/{idReserva}) · deseable

- Tarjeta con sala, horario, relator (nombre y correo), motivo y ChipEstado.
- Línea de tiempo simple: Reservada → Check-in (con hora, o "pendiente") → Verificada.
- Si está En uso: BotonPrincipal "Marcar como verificada" (fui a la sala y se está usando). El estado pasa a Verificada.
- Si está sin check-in: BotonPeligro "Marcar no presentado" con diálogo de confirmación. El estado pasa a No presentado. La sala no se libera.
- Si ya está Verificada, No presentado o Cancelada: solo información.

### 5. Horario general (ruta: horarioGeneral) · deseable

Se llega desde Perfil del coordinador.

- Texto explicativo: "Este horario aplica a todas las salas que usan el horario general. Se define al comenzar cada semestre."
- Selector de hora de apertura (por defecto 08:00) y de cierre (por defecto 21:00).
- Fila de días con chips marcables: Lun a Sáb (Lun–Vie marcados por defecto).
- Campo "Pausa entre bloques" en minutos (por defecto 5). Vista previa: "13 bloques por día: 08:00–09:00, 09:05–10:00 …" (se recalcula al cambiar las horas).
- BotonSecundario "Cancelar" y BotonPrincipal "Guardar".

## Datos falsos

Todos usan los mismos datos, creados por Kevin en los repositorios falsos. Las fechas se calculan desde el día actual, así la demo del 28 siempre muestra reservas "de hoy".

### Usuarios

Los 7 usuarios de prueba de ms-usuarios (IDs 1 a 7), con los mismos nombres y correos @duocuc.test. Las reservas falsas apuntan a esos IDs.

### Salas

| Código | Nombre | Capacidad | Equipamiento | Horario |
| --- | --- | --- | --- | --- |
| S-101 | Sala 101 | 20 | Proyector (con falla: "la imagen parpadea"), Climatización | General |
| S-102 | Sala 102 | 30 | Proyector | General |
| S-201 | Sala 201 | 15 | Climatización | General |
| LAB-01 | Laboratorio de Computación 1 | 25 | Proyector, Computadores, Climatización | General |
| LAB-02 | Laboratorio de Computación 2 | 18 | Computadores | Propio: 09:00 a 18:00 |
| MUL-01 | Espacio Multipropósito | 50 | Proyector, Climatización | General |

Cuando el mandante entregue la lista real de espacios, se reemplaza esta tabla.

### Reservas de ejemplo (deben existir para que cada pantalla tenga algo que mostrar)

| N.º | Sala | Cuándo | Bloques | Estado | Para mostrar |
| --- | --- | --- | --- | --- | --- |
| 1 | LAB-01 | Hoy, empieza 10 minutos después de abrir la app | 2 | Reservada | Recordatorio y check-in habilitado |
| 2 | S-201 | Hoy, empezó hace 30 minutos | 2 | En uso | Aviso de check-in al coordinador, Marcar verificada |
| 3 | S-102 | Hoy, empezó hace 40 minutos | 1 | Reservada (sin check-in) | Métrica "Sin check-in", Marcar no presentado |
| 4 | MUL-01 | Hoy, temprano | 2 | Verificada | Historial y estado final |
| 5 | S-101 | Mañana 10:05–12:00 | 2 | Reservada | Próximas en Mi agenda, Cancelar |
| 6 | LAB-02 | En 3 días 15:05–16:00 | 1 | Reservada | Agenda de la semana |
| 7 | S-201 | Ayer 09:00–10:00 | 1 | No presentado | Historial |
| 8 | LAB-01 | Hace 2 días | 1 | Cancelada | Historial |

Asignen las reservas 1, 5 y 7 al relator con el que harán la demo, y al menos una (ej. la 6) al coordinador, para mostrar que también reserva.

## Calendario

La ruta crítica es la base de Kevin: debe estar en develop el domingo 18.

| Fecha | Kevin | Simón | Bastián |
| --- | --- | --- | --- |
| Jue 15 – Vie 16 | Bocetos con el equipo; tema y componentes | Bocetos con el equipo; bocetos detallados de sus 8 pantallas | Bocetos con el equipo; bocetos detallados de sus 5 pantallas |
| Sáb 17 – Dom 18 | Estructura, rutas, Perfil, datos falsos → develop | Inicio relator y Salas sobre componentes provisorios | Inicio coordinador sobre componentes provisorios |
| Lun 19 – Mié 21 | Apoyo y revisión | Detalle, Reservar, Confirmación, Mi agenda | Editar sala, Reservas del día |
| Mié 21 | Revisión de avance de todos |  |  |
| Jue 22 – Dom 25 | Check-in con cámara y QR impresos; apoyo a quien vaya atrasado | Detalle de reserva | Supervisar reserva, Horario general |
| Lun 26 | Congelamiento: todo fusionado en develop | Solo correcciones | Solo correcciones |
| Mar 27 | Ensayo de la demo con guion | Ensayo | Ensayo |
| Mié 28 | Presentación | Presentación | Presentación |

### Una pantalla está terminada cuando

- [ ] Usa solo colores y componentes de Reglas comunes.
- [ ] Muestra datos de los repositorios falsos, no datos escritos dentro de la pantalla.
- [ ] Navega a las rutas indicadas en este documento.
- [ ] Tiene su mensaje para el caso vacío (sin datos).
- [ ] Se probó en el teléfono, no solo en la vista previa.
- [ ] Está subida a la rama personal (feature/simon, feature/bastian o feature/kevin) y se le avisó a Kevin para fusionarla en develop.
