package cl.duoc.reservasalas.app.ui.pantallas.checkin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Check-in (ruta checkin/{idReserva}). Dueño: Kevin.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/kevin/03-Check-in.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - idReserva: número de la reserva que llega en la ruta.
 * - volver: vuelve a la pantalla anterior (flecha atrás de la BarraSuperior).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun CheckInPantalla(
    idReserva: Int,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Kevin): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Check-in", dueno = "Kevin", modifier = modifier, alVolver = volver)
}
