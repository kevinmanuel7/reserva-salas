package cl.duoc.reservasalas.app.ui.pantallas.supervisar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Supervisar reserva (ruta supervisar/{idReserva}). Dueño: Bastián.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/bastian/03-Supervisar-reserva.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - idReserva: número de la reserva que llega en la ruta.
 * - volver: vuelve a la pantalla anterior (flecha atrás de la BarraSuperior).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun SupervisarPantalla(
    idReserva: Int,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Bastián): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Supervisar reserva", dueno = "Bastián", modifier = modifier, alVolver = volver)
}
