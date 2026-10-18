package cl.duoc.reservasalas.app.ui.navegacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.reservasalas.app.R
import cl.duoc.reservasalas.app.model.Rol
import cl.duoc.reservasalas.app.model.Sesion
import cl.duoc.reservasalas.app.ui.componentes.BarraInferior
import cl.duoc.reservasalas.app.ui.componentes.ItemBarraInferior
import cl.duoc.reservasalas.app.ui.pantallas.agenda.AgendaPantalla
import cl.duoc.reservasalas.app.ui.pantallas.checkin.CheckInPantalla
import cl.duoc.reservasalas.app.ui.pantallas.detallereserva.DetalleReservaPantalla
import cl.duoc.reservasalas.app.ui.pantallas.detallesala.DetalleSalaPantalla
import cl.duoc.reservasalas.app.ui.pantallas.editarsala.EditarSalaPantalla
import cl.duoc.reservasalas.app.ui.pantallas.horariogeneral.HorarioGeneralPantalla
import cl.duoc.reservasalas.app.ui.pantallas.iniciocoordinador.InicioCoordinadorPantalla
import cl.duoc.reservasalas.app.ui.pantallas.iniciorelator.InicioRelatorPantalla
import cl.duoc.reservasalas.app.ui.pantallas.perfil.PerfilPantalla
import cl.duoc.reservasalas.app.ui.pantallas.reservaconfirmada.ReservaConfirmadaPantalla
import cl.duoc.reservasalas.app.ui.pantallas.reservar.ReservarPantalla
import cl.duoc.reservasalas.app.ui.pantallas.reservasdia.ReservasDiaPantalla
import cl.duoc.reservasalas.app.ui.pantallas.salas.SalasPantalla
import cl.duoc.reservasalas.app.ui.pantallas.supervisar.SupervisarPantalla
import cl.duoc.reservasalas.app.ui.screens.LoginScreen
import cl.duoc.reservasalas.app.ui.theme.Fondo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Mientras se lee DataStore no se sabe si hay sesión: se distingue "leyendo" de "no hay sesión". */
private sealed interface EstadoSesion {
    data object Leyendo : EstadoSesion
    data class Lista(val sesion: Sesion?) : EstadoSesion
}

/** Ítems de la barra inferior según el rol: 4 para el relator y 5 para el coordinador. */
fun itemsBarraInferior(rol: Rol?): List<ItemBarraInferior> = when (rol) {
    Rol.RELATOR -> listOf(
        ItemBarraInferior(Rutas.INICIO_RELATOR, "Inicio", R.drawable.ic_inicio),
        ItemBarraInferior(Rutas.SALAS, "Salas", R.drawable.ic_salas),
        ItemBarraInferior(Rutas.AGENDA, "Mi agenda", R.drawable.ic_agenda),
        ItemBarraInferior(Rutas.PERFIL, "Perfil", R.drawable.ic_perfil)
    )
    Rol.COORDINADOR -> listOf(
        ItemBarraInferior(Rutas.INICIO_COORDINADOR, "Inicio", R.drawable.ic_inicio),
        ItemBarraInferior(Rutas.SALAS, "Salas", R.drawable.ic_salas),
        ItemBarraInferior(Rutas.RESERVAS_DIA, "Reservas", R.drawable.ic_reservas),
        ItemBarraInferior(Rutas.AGENDA, "Mi agenda", R.drawable.ic_agenda),
        ItemBarraInferior(Rutas.PERFIL, "Perfil", R.drawable.ic_perfil)
    )
    null -> emptyList()
}

/**
 * Estructura principal de la app: decide la pantalla inicial, dibuja la barra inferior
 * en las pantallas principales y conecta cada ruta con su pantalla.
 *
 * Las pantallas NO reciben el NavController: reciben `navegar(ruta)` y `volver()`.
 * Así se pueden ver en @Preview y no dependen de la navegación.
 */
@Composable
fun NavegacionApp(sesion: Flow<Sesion?>) {
    val estadoSesion by remember(sesion) {
        sesion.map<Sesion?, EstadoSesion> { EstadoSesion.Lista(it) }
    }.collectAsStateWithLifecycle(initialValue = EstadoSesion.Leyendo)

    val estado = estadoSesion
    if (estado !is EstadoSesion.Lista) {
        // Solo dura un instante, mientras se lee DataStore.
        Box(Modifier.fillMaxSize().background(Fondo))
        return
    }

    // La pantalla inicial se decide una sola vez: con sesión guardada, se salta el login.
    val rutaInicial = remember {
        estado.sesion?.let { Rutas.inicioSegunRol(it.usuario.rol) } ?: Rutas.LOGIN
    }
    val rol = estado.sesion?.usuario?.rol

    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val items = itemsBarraInferior(rol)
    val mostrarBarra = items.any { it.ruta == rutaActual }

    val navegar: (String) -> Unit = { ruta -> navController.navegarA(ruta, rol) }
    val volver: () -> Unit = { navController.popBackStack() }

    Scaffold(
        containerColor = Fondo,
        // Cada barra maneja sus propios márgenes del sistema (statusBarsPadding / navigationBarsPadding).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (mostrarBarra) {
                BarraInferior(items = items, rutaActual = rutaActual, alElegir = navegar)
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = rutaInicial,
            modifier = Modifier
                .padding(relleno)
                .then(if (mostrarBarra) Modifier else Modifier.navigationBarsPadding())
        ) {
            // --- Kevin ---
            composable(Rutas.LOGIN) {
                LoginScreen(
                    alIniciarSesion = { usuario ->
                        navController.navigate(Rutas.inicioSegunRol(usuario.rol)) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
            composable(Rutas.PERFIL) {
                PerfilPantalla(navegar = navegar)
            }
            composable(Rutas.CHECKIN, arguments = listOf(argumentoIdReserva())) { entrada ->
                CheckInPantalla(idReserva = entrada.idReserva(), volver = volver)
            }

            // --- Simón ---
            composable(Rutas.INICIO_RELATOR) {
                InicioRelatorPantalla(navegar = navegar)
            }
            composable(Rutas.SALAS) {
                SalasPantalla(navegar = navegar)
            }
            composable(Rutas.SALA, arguments = listOf(argumentoCodigo())) { entrada ->
                DetalleSalaPantalla(codigo = entrada.codigo(), navegar = navegar, volver = volver)
            }
            composable(Rutas.RESERVAR, arguments = listOf(argumentoCodigo())) { entrada ->
                ReservarPantalla(codigo = entrada.codigo(), navegar = navegar, volver = volver)
            }
            composable(Rutas.RESERVA_CONFIRMADA, arguments = listOf(argumentoIdReserva())) { entrada ->
                ReservaConfirmadaPantalla(idReserva = entrada.idReserva(), navegar = navegar)
            }
            composable(Rutas.AGENDA) {
                AgendaPantalla(navegar = navegar)
            }
            composable(Rutas.RESERVA, arguments = listOf(argumentoIdReserva())) { entrada ->
                DetalleReservaPantalla(idReserva = entrada.idReserva(), navegar = navegar, volver = volver)
            }

            // --- Bastián ---
            composable(Rutas.INICIO_COORDINADOR) {
                InicioCoordinadorPantalla(navegar = navegar)
            }
            composable(Rutas.RESERVAS_DIA) {
                ReservasDiaPantalla(navegar = navegar)
            }
            composable(Rutas.SUPERVISAR, arguments = listOf(argumentoIdReserva())) { entrada ->
                SupervisarPantalla(idReserva = entrada.idReserva(), volver = volver)
            }
            composable(Rutas.EDITAR_SALA, arguments = listOf(argumentoCodigo())) { entrada ->
                EditarSalaPantalla(codigo = entrada.codigo(), volver = volver)
            }
            composable(Rutas.HORARIO_GENERAL) {
                HorarioGeneralPantalla(volver = volver)
            }
        }
    }
}

private fun argumentoCodigo() = navArgument(Rutas.ARG_CODIGO) { type = NavType.StringType }

private fun argumentoIdReserva() = navArgument(Rutas.ARG_ID_RESERVA) { type = NavType.IntType }

private fun NavBackStackEntry.codigo(): String =
    arguments?.getString(Rutas.ARG_CODIGO).orEmpty()

private fun NavBackStackEntry.idReserva(): Int =
    arguments?.getInt(Rutas.ARG_ID_RESERVA) ?: 0

/**
 * Navega a [ruta] con las reglas de la app:
 * - login: borra todo el historial (después de cerrar sesión no se puede volver atrás).
 * - ítem de la barra inferior: cambia de pestaña sin apilar pantallas repetidas.
 * - reserva confirmada: saca "Reservar" del historial, para no volver a reservar con "atrás".
 * - cualquier otra: navegación normal.
 */
private fun NavHostController.navegarA(ruta: String, rol: Rol?) {
    val rutasBarra = itemsBarraInferior(rol).map { it.ruta }
    when {
        ruta == Rutas.LOGIN -> navigate(Rutas.LOGIN) {
            popUpTo(graph.id) { inclusive = true }
        }
        rol != null && ruta in rutasBarra -> navigate(ruta) {
            popUpTo(Rutas.inicioSegunRol(rol)) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
        ruta.startsWith("reservaConfirmada/") -> navigate(ruta) {
            popUpTo(Rutas.RESERVAR) { inclusive = true }
        }
        else -> navigate(ruta)
    }
}
