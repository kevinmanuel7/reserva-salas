package cl.duoc.reservasalas.app.ui.navegacion

import cl.duoc.reservasalas.app.model.Rol

/**
 * Las 15 rutas definitivas de la app (AGENTS.md, "Rutas definitivas").
 * Prohibido inventar rutas nuevas: si falta una, se le pide a Kevin.
 *
 * Para navegar a una ruta con datos se usan las funciones de abajo, nunca texto armado a mano:
 *   navegar(Rutas.sala("S-201"))     en vez de   navegar("sala/S-201")
 */
object Rutas {
    const val LOGIN = "login"
    const val PERFIL = "perfil"
    const val CHECKIN = "checkin/{idReserva}"

    const val INICIO_RELATOR = "inicioRelator"
    const val SALAS = "salas"
    const val SALA = "sala/{codigo}"
    const val RESERVAR = "reservar/{codigo}"
    const val RESERVA_CONFIRMADA = "reservaConfirmada/{idReserva}"
    const val AGENDA = "agenda"
    const val RESERVA = "reserva/{idReserva}"

    const val INICIO_COORDINADOR = "inicioCoordinador"
    const val RESERVAS_DIA = "reservasDia"
    const val SUPERVISAR = "supervisar/{idReserva}"
    const val EDITAR_SALA = "editarSala/{codigo}"
    const val HORARIO_GENERAL = "horarioGeneral"

    /** Nombre del dato {codigo} de las rutas (código de la sala, ej. "S-201"). */
    const val ARG_CODIGO = "codigo"

    /** Nombre del dato {idReserva} de las rutas (número de la reserva). */
    const val ARG_ID_RESERVA = "idReserva"

    fun checkin(idReserva: Int) = "checkin/$idReserva"
    fun sala(codigo: String) = "sala/$codigo"
    fun reservar(codigo: String) = "reservar/$codigo"
    fun reservaConfirmada(idReserva: Int) = "reservaConfirmada/$idReserva"
    fun reserva(idReserva: Int) = "reserva/$idReserva"
    fun supervisar(idReserva: Int) = "supervisar/$idReserva"
    fun editarSala(codigo: String) = "editarSala/$codigo"

    /** Pantalla de inicio que le corresponde a cada rol. */
    fun inicioSegunRol(rol: Rol): String = when (rol) {
        Rol.RELATOR -> INICIO_RELATOR
        Rol.COORDINADOR -> INICIO_COORDINADOR
    }
}
