package cl.duoc.reservasalas.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Esquema de colores de Material 3 armado con los colores de Duoc.
 * Así los componentes de Material (campos de texto, diálogos, etc.) toman estos colores solos.
 * Solo modo claro: la maqueta no tiene modo oscuro ni colores dinámicos del teléfono.
 */
private val EsquemaDuoc = lightColorScheme(
    primary = AmarilloDuoc,
    onPrimary = NegroDuoc,
    primaryContainer = AmarilloSuave,
    onPrimaryContainer = NegroDuoc,
    secondary = NegroDuoc,
    onSecondary = Blanco,
    background = Fondo,
    onBackground = NegroDuoc,
    surface = Superficie,
    onSurface = NegroDuoc,
    surfaceVariant = Fondo,
    onSurfaceVariant = Gris85,
    outline = Borde,
    error = RojoError,
    onError = Blanco
)

@Composable
fun ReservaSalasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaDuoc,
        typography = Typography,
        content = content
    )
}
