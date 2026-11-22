package cl.duoc.reservasalas.app.notificaciones

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Lo despierta AlarmManager 15 minutos antes de una reserva: muestra la notificación.
 * Android lo ejecuta aunque la app esté cerrada.
 *
 * Los datos llegan como extras del Intent (ver ProgramadorRecordatoriosAlarmas).
 */
class RecordatorioReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val idReserva = intent.getIntExtra(CLAVE_ID_RESERVA, -1)
        val texto = intent.getStringExtra(CLAVE_TEXTO)

        // Sin datos o sin permiso no se muestra nada.
        if (idReserva == -1 || texto == null || !puedeNotificar(context)) return

        // Al tocar la notificación se abre la app.
        val alTocar = intentAbrirApp(context, idReserva)

        val notificacion = NotificationCompat.Builder(context, Recordatorios.ID_CANAL)
            // Ícono del sistema: los íconos de la app son adaptativos y fallan como ícono pequeño en Android 8.
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(Recordatorios.TITULO)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(alTocar)
            .setAutoCancel(true)
            .build()

        try {
            // El id de la notificación es el de la reserva: así no se repite ni se pisa con otra.
            NotificationManagerCompat.from(context).notify(idReserva, notificacion)
        } catch (e: SecurityException) {
            // El permiso se quitó justo ahora: se omite el aviso.
        }
    }

    /**
     * El permiso POST_NOTIFICATIONS existe solo desde Android 13: en Android 12 o menos
     * checkSelfPermission responde DENIED aunque se pueda notificar, por eso no se revisa ahí.
     * En todas las versiones, las notificaciones no deben estar apagadas.
     */
    private fun puedeNotificar(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permitido = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!permitido) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    companion object {
        const val CLAVE_ID_RESERVA = "idReserva"
        const val CLAVE_TEXTO = "texto"
    }
}
