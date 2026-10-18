package cl.duoc.reservasalas.app.ui.pantallas.detallesala

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Sala (ruta sala/{codigo}). Dueño: Simón.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/simon/03-Detalle-de-sala.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - codigo: código de la sala que llega en la ruta, ej. "S-201".
 * - navegar: ir a otra pantalla, siempre con Rutas (ej. navegar(Rutas.sala("S-201"))).
 * - volver: vuelve a la pantalla anterior (flecha atrás de la BarraSuperior).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun DetalleSalaPantalla(
    codigo: String,
    navegar: (String) -> Unit,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Simón): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Sala $codigo", dueno = "Simón", modifier = modifier, alVolver = volver)
}
