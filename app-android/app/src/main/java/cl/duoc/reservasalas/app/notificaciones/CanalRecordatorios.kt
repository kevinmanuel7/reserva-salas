package cl.duoc.reservasalas.app.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import cl.duoc.reservasalas.app.MainActivity
import cl.duoc.reservasalas.app.data.modelo.Reserva

/** Datos fijos del recordatorio, compartidos por el canal, el receiver y el programador. */
object Recordatorios {
    const val ID_CANAL = "recordatorios_reservas"
    const val NOMBRE_CANAL = "Recordatorios de reservas"

    val TITULO = "Tu reserva empieza en ${Reserva.MINUTOS_RECORDATORIO} minutos"
}

/**
 * Crea el canal "Recordatorios de reservas". Desde Android 8.0 toda notificación debe ir
 * en un canal; el usuario puede silenciarlo desde los ajustes del teléfono.
 * Crearlo de nuevo no hace nada si ya existe, por eso se llama en cada inicio de la app.
 */
fun crearCanalRecordatorios(context: Context) {
    val canal = NotificationChannel(
        Recordatorios.ID_CANAL,
        Recordatorios.NOMBRE_CANAL,
        NotificationManager.IMPORTANCE_HIGH
    )
    canal.description = "Aviso ${Reserva.MINUTOS_RECORDATORIO} minutos antes de cada reserva"
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(canal)
}

/**
 * PendingIntent que abre la app. Lo usan la notificación (al tocarla) y la alarma tipo
 * despertador (al tocar la "próxima alarma" en el teléfono). requestCode = id de la reserva.
 * FLAG_IMMUTABLE: nadie más puede modificar el Intent.
 */
fun intentAbrirApp(context: Context, idReserva: Int): PendingIntent {
    val abrirApp = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    return PendingIntent.getActivity(
        context,
        idReserva,
        abrirApp,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

/** Texto del recordatorio, ej. "Sala 101 · 10:05–11:00 · Excel intermedio". */
fun textoRecordatorio(reserva: Reserva, nombreSala: String): String =
    "$nombreSala · ${reserva.horario} · ${reserva.motivo}"
