package cl.duoc.reservasalas.app.ui.pantallas.reservar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Reservar (ruta reservar/{codigo}). Dueño: Simón.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/simon/04-Reservar.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - codigo: código de la sala que llega en la ruta, ej. "S-201".
 * - navegar: ir a otra pantalla, siempre con Rutas (ej. navegar(Rutas.sala("S-201"))).
 * - volver: vuelve a la pantalla anterior (flecha atrás de la BarraSuperior).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun ReservarPantalla(
    codigo: String,
    navegar: (String) -> Unit,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Simón): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Reservar $codigo", dueno = "Simón", modifier = modifier, alVolver = volver)
}
