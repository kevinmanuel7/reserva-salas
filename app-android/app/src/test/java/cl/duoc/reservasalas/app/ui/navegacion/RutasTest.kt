package cl.duoc.reservasalas.app.ui.navegacion

import cl.duoc.reservasalas.app.model.Rol
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Las rutas con datos se arman con funciones (nunca a mano) y cada rol entra a su inicio.
 * No se simula nada: son funciones puras.
 */
class RutasTest {

    @Test
    fun rutasConDatos_reemplazanElParametro() {
        assertEquals("sala/S-201", Rutas.sala("S-201"))
        assertEquals("reservar/S-201", Rutas.reservar("S-201"))
        assertEquals("editarSala/S-201", Rutas.editarSala("S-201"))
        assertEquals("checkin/5", Rutas.checkin(5))
        assertEquals("reserva/5", Rutas.reserva(5))
        assertEquals("reservaConfirmada/5", Rutas.reservaConfirmada(5))
        assertEquals("reservaCancelada/5", Rutas.reservaCancelada(5))
        assertEquals("supervisar/5", Rutas.supervisar(5))
    }

    @Test
    fun cadaRolEntraASuInicio() {
        assertEquals(Rutas.INICIO_RELATOR, Rutas.inicioSegunRol(Rol.RELATOR))
        assertEquals(Rutas.INICIO_COORDINADOR, Rutas.inicioSegunRol(Rol.COORDINADOR))
    }
}
