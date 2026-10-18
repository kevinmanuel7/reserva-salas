package cl.duoc.reservasalas.app.ui.pantallas.editarsala

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Editar sala (ruta editarSala/{codigo}). Dueño: Bastián.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/bastian/04-Editar-sala.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - codigo: código de la sala que llega en la ruta, ej. "S-201".
 * - volver: vuelve a la pantalla anterior (flecha atrás de la BarraSuperior).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun EditarSalaPantalla(
    codigo: String,
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Bastián): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Editar sala $codigo", dueno = "Bastián", modifier = modifier, alVolver = volver)
}
