package cl.duoc.reservasalas.app.notificaciones

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi

/**
 * true si la app puede programar alarmas exactas.
 * Hasta Android 11 no piden permiso; desde Android 12, el usuario puede negarlo.
 * La usan el programador, el diálogo de Inicio del relator y MainActivity.
 */
fun puedeProgramarAlarmasExactas(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

/** Pantalla de ajustes "Alarmas y recordatorios" de esta app (solo existe desde Android 12). */
@RequiresApi(Build.VERSION_CODES.S)
fun intentPermisoAlarmasExactas(context: Context): Intent =
    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
