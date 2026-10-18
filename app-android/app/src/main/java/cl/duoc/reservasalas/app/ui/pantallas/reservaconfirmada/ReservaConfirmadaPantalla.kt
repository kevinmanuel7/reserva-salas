package cl.duoc.reservasalas.app.ui.pantallas.reservaconfirmada

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Reserva confirmada (ruta reservaConfirmada/{idReserva}). Dueño: Simón.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/simon/05-Reserva-confirmada.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - idReserva: número de la reserva que llega en la ruta.
 * - navegar: ir a otra pantalla, siempre con Rutas (ej. navegar(Rutas.sala("S-201"))).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun ReservaConfirmadaPantalla(
    idReserva: Int,
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Simón): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Reserva confirmada", dueno = "Simón", modifier = modifier)
}
