package cl.duoc.reservasalas.app.notificaciones

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import cl.duoc.reservasalas.app.data.local.RecordatoriosDataStore
import cl.duoc.reservasalas.app.data.modelo.Reserva
import cl.duoc.reservasalas.app.data.repository.RepositorioSalas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.ZoneId

/**
 * Programa y borra los recordatorios locales. Es una interfaz para que las pruebas usen
 * una versión falsa que solo anota las llamadas, sin Android.
 */
interface ProgramadorRecordatorios {
    /** Programa (o reprograma) el aviso de la reserva para 15 minutos antes de su inicio. */
    fun programar(reserva: Reserva)

    fun cancelar(idReserva: Int)

    /** Borra todos los recordatorios (al cerrar sesión). */
    fun cancelarTodos()
}

/**
 * Versión real: cada recordatorio es una alarma de AlarmManager que, a la hora del aviso,
 * despierta a [RecordatorioReceiver] aunque la app esté cerrada.
 *
 * [scope] debe ejecutar una tarea a la vez, para que los ids se guarden y borren en el
 * mismo orden en que se pidieron (ver AppContainer).
 */
class ProgramadorRecordatoriosAlarmas(
    private val context: Context,
    private val repositorioSalas: RepositorioSalas,
    private val almacenIds: RecordatoriosDataStore,
    private val scope: CoroutineScope
) : ProgramadorRecordatorios {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun programar(reserva: Reserva) {
        // Hora del aviso en milisegundos desde 1970, que es lo que pide AlarmManager.
        val momento = reserva.momentoRecordatorio
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val nombreSala = repositorioSalas.buscar(reserva.codigoSala)?.nombre ?: reserva.codigoSala

        val intent = intentReceiver().apply {
            putExtra(RecordatorioReceiver.CLAVE_ID_RESERVA, reserva.id)
            putExtra(RecordatorioReceiver.CLAVE_TEXTO, textoRecordatorio(reserva, nombreSala))
        }
        // requestCode = id de la reserva: un PendingIntent por reserva. Si ya existía, FLAG_UPDATE_CURRENT
        // le pone el texto nuevo y la alarma nueva reemplaza a la anterior.
        val alDisparar = PendingIntent.getBroadcast(
            context,
            reserva.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (puedeProgramarAlarmasExactas(context)) {
            try {
                // Alarma tipo despertador: siempre exacta, también en reposo (Doze). Se usa en vez de
                // setExactAndAllowWhileIdle porque algunos fabricantes (ColorOS de OPPO) convierten
                // esa en inexacta. Al tocar la "próxima alarma" del teléfono se abre la app.
                val info = AlarmManager.AlarmClockInfo(momento, intentAbrirApp(context, reserva.id))
                alarmManager.setAlarmClock(info, alDisparar)
            } catch (e: SecurityException) {
                // El permiso se quitó justo ahora: se usa el respaldo.
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, alDisparar)
            }
        } else {
            // Respaldo sin permiso de alarmas exactas: Android puede atrasarla unos minutos.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, momento, alDisparar)
        }

        scope.launch { almacenIds.agregar(reserva.id) }
    }

    override fun cancelar(idReserva: Int) {
        cancelarAlarma(idReserva)
        scope.launch { almacenIds.quitar(idReserva) }
    }

    override fun cancelarTodos() {
        scope.launch { almacenIds.leerYBorrarTodos().forEach { cancelarAlarma(it) } }
    }

    /** Borra la alarma de una reserva, si existe. */
    private fun cancelarAlarma(idReserva: Int) {
        // FLAG_NO_CREATE: devuelve el PendingIntent solo si ya existe (si no, null).
        val alDisparar = PendingIntent.getBroadcast(
            context,
            idReserva,
            intentReceiver(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        ) ?: return
        alarmManager.cancel(alDisparar)
        alDisparar.cancel()
    }

    // Intent explícito hacia nuestro receiver: Android compara la clase (no los extras)
    // para saber si dos PendingIntent son "el mismo".
    private fun intentReceiver() = Intent(context, RecordatorioReceiver::class.java)
}
