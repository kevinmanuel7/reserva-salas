package cl.duoc.reservasalas.app.ui.pantallas.iniciocoordinador

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.componentes.PantallaEnConstruccion

/**
 * Inicio (ruta inicioCoordinador). Dueño: Bastián.
 * Especificación: docs/maqueta/especificacion.md
 * Imagen: docs/maqueta/pantallas/bastian/01-Inicio-coordinador.png
 *
 * La navegación (ui/navegacion/NavegacionApp.kt) llama a esta función con estos parámetros:
 * NO cambies el nombre ni los parámetros sin avisar a Kevin.
 * - navegar: ir a otra pantalla, siempre con Rutas (ej. navegar(Rutas.sala("S-201"))).
 * - modifier: márgenes que entrega la navegación (ej. el espacio de la barra inferior).
 */
@Composable
fun InicioCoordinadorPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO (Bastián): reemplazar este relleno por la pantalla real, con su ViewModel en esta misma carpeta.
    PantallaEnConstruccion(titulo = "Inicio", dueno = "Bastián", modifier = modifier)
}
