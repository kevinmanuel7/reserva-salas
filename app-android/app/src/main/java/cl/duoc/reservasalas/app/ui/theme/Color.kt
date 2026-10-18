package cl.duoc.reservasalas.app.ui.theme

import androidx.compose.ui.graphics.Color

// Colores de Duoc UC Sede Viña del Mar (PDF del mandante). Los hex de Pantone son aproximados.
// Etapa maqueta: las pantallas usan SOLO estos colores. Prohibido escribir Color(0xFF...) en una pantalla.

/** Negro Duoc (Pantone Process Black C): barras, títulos y cabeceras de tarjetas. */
val NegroDuoc = Color(0xFF2D2926)

/** Negro al 85 %: textos secundarios e íconos inactivos sobre fondo claro. */
val Gris85 = Color(0xFF4D4845)

/** Amarillo Duoc (Pantone 1235 C): botón principal, ítem activo y línea bajo la barra superior. */
val AmarilloDuoc = Color(0xFFFFB81C)

/** Elemento seleccionado y chip "Reservada". */
val AmarilloSuave = Color(0xFFFFF3CC)

/** Fondo de todas las pantallas. */
val Fondo = Color(0xFFF2F3F5)

/** Tarjetas y formularios. */
val Superficie = Color(0xFFFFFFFF)

/** Bordes de tarjetas y campos. */
val Borde = Color(0xFFDDDDDD)

/** Botón de peligro, mensajes de error, chip "No presentado" y equipos con falla. */
val RojoError = Color(0xFFD32F2F)

/** Chips "En uso" y "Verificada", y equipos operativos. */
val VerdeExito = Color(0xFF2E7D32)

/** Chip "Cancelada". */
val GrisCancelada = Color(0xFF757575)

/** Bloques ocupados en las grillas de horario. */
val GrisOcupado = Color(0xFFE3E1E0)

/** Íconos y textos inactivos sobre fondo negro (barra inferior). */
val GrisSobreNegro = Color(0xFFBDB8B5)

/** Texto sobre fondos oscuros o de color. */
val Blanco = Color(0xFFFFFFFF)
