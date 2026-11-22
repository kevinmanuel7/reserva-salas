package cl.duoc.reservasalas.app.notificaciones

import cl.duoc.reservasalas.app.data.falso.Reloj
import cl.duoc.reservasalas.app.data.modelo.EstadoReserva
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.model.Sesion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * Mantiene los recordatorios al día con las reservas y la sesión.
 * Cada vez que cambia la lista de reservas o la sesión, decide qué programar y qué borrar.
 *
 * No usa nada de Android: así se prueba con un ProgramadorRecordatorios falso.
 */
class SincronizadorRecordatorios(
    private val reservas: Flow<List<Reserva>>,
    private val sesion: Flow<Sesion?>,
    private val programador: ProgramadorRecordatorios,
    private val reloj: Reloj
) {

    // Lo que ya se programó en esta ejecución de la app (id → reserva), para no repetirlo
    // en cada cambio de la lista. Si la reserva cambia (ej. otro horario), se reprograma.
    private val programadas = mutableMapOf<Int, Reserva>()

    // Reservas cuyo recordatorio ya se borró, para no repetir el borrado.
    private val canceladas = mutableSetOf<Int>()

    // Lo último que se recibió, para poder reprogramar todo sin esperar un cambio.
    // null = todavía no llega ninguna lista.
    private var ultimaLista: List<Reserva>? = null
    private var ultimaSesion: Sesion? = null

    /** Empieza a observar. Se llama una sola vez, al abrir la app (ReservaSalasApp). */
    fun iniciar(scope: CoroutineScope) {
        combine(reservas, sesion) { lista, sesionActual -> sincronizar(lista, sesionActual) }
            .launchIn(scope)
    }

    /**
     * Vuelve a programar todas las reservas que necesitan aviso, aunque ya estuvieran programadas.
     * Se usa al volver de los ajustes con el permiso de alarmas exactas: así las alarmas que se
     * pidieron inexactas se reemplazan por exactas.
     */
    @Synchronized
    fun reprogramarTodo() {
        val lista = ultimaLista ?: return
        programadas.clear()
        sincronizar(lista, ultimaSesion)
    }

    /**
     * Programa las reservas que necesitan aviso y borra las que ya no.
     * @Synchronized: el flujo la llama desde un hilo de fondo y reprogramarTodo desde el hilo
     * principal; así nunca se ejecutan las dos a la vez sobre los mismos mapas.
     */
    @Synchronized
    fun sincronizar(lista: List<Reserva>, sesionActual: Sesion?) {
        ultimaLista = lista
        ultimaSesion = sesionActual

        // Sin sesión (cerró sesión o el token venció): no debe quedar ningún aviso.
        if (sesionActual == null) {
            programador.cancelarTodos()
            programadas.clear()
            canceladas.clear()
            return
        }

        val ahora = reloj.ahora()
        val hoy = ahora.toLocalDate()
        // La lista trae reservas de todos los usuarios: solo interesan las del conectado.
        val mias = lista.filter { it.idUsuario == sesionActual.usuario.id }
        val validas = mias.filter { it.necesitaRecordatorio(ahora) }

        for (reserva in validas) {
            if (programadas[reserva.id] != reserva) {
                programador.programar(reserva)
                programadas[reserva.id] = reserva
                canceladas.remove(reserva.id)
            }
        }

        // Se borra el aviso de las reservas que dejaron de estar Reservadas (cancelada, check-in,
        // no presentado):
        // - las que se programaron en esta ejecución, y
        // - las propias de hoy en adelante, por si cambiaron con la app cerrada (ej. cancelada
        //   desde la web) y su alarma quedó programada en AlarmManager.
        // Una reserva que solo "no aparece" en la lista (ej. lista vacía antes de refrescar) no se
        // toca. Tampoco una Reservada cuyo aviso ya pasó: Android puede estar por mostrarlo.
        val aBorrar = mias.filter { reserva ->
            reserva.estado != EstadoReserva.RESERVADA &&
                (reserva.id in programadas || !reserva.fecha.isBefore(hoy))
        }

        for (reserva in aBorrar) {
            if (reserva.id !in canceladas) {
                programador.cancelar(reserva.id)
                canceladas.add(reserva.id)
            }
            programadas.remove(reserva.id)
        }
    }
}
