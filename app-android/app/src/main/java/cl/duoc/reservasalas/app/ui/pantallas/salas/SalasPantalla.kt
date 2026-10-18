package cl.duoc.reservasalas.app.ui.pantallas.salas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Salas (ruta salas). Dueño: Simón.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/simon/02-Salas.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - navegar: ir a otra pantalla, siempre con Rutas (ej. navegar(Rutas.sala("S-201"))).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun SalasPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Simón): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Salas", dueno = "Simón", modifier = modifier)
}
