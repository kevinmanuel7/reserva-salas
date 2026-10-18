package cl.duoc.reservasalas.app.ui.pantallas.detallereserva

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Reserva (ruta reserva/{idReserva}). Dueño: Simón.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/simon/07-Detalle-de-reserva.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - idReserva: número de la reserva que llega en la ruta.
 * - navegar: ir a otra pantalla, siempre con Rutas (ej. navegar(Rutas.sala("S-201"))).
 * - volver: vuelve a la pantalla anterior (flecha atrás de la BarraSuperior).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun DetalleReservaPantalla(
    idReserva: Int,
    navegar: (String) -> Unit,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Simón): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Reserva", dueno = "Simón", modifier = modifier, alVolver = volver)
}
