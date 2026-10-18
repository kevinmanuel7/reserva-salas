package cl.duoc.reservasalas.app.ui.pantallas.horariogeneral

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Horario general (ruta horarioGeneral). Dueño: Bastián.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/bastian/05-Horario-general.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - volver: vuelve a la pantalla anterior (flecha atrás de la BarraSuperior).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun HorarioGeneralPantalla(
    volver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Bastián): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Horario general", dueno = "Bastián", modifier = modifier, alVolver = volver)
}
