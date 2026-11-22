package cl.duoc.reservasalas.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.reservasalas.app.notificaciones.puedeProgramarAlarmasExactas
import cl.duoc.reservasalas.app.ui.navegacion.NavegacionApp
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme

class MainActivity : ComponentActivity() {

    // Si la app podía usar alarmas exactas la última vez que se mostró (null = aún no se sabe).
    private var podiaAlarmaExacta: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barra de estado transparente con íconos claros: la BarraSuperior negra se dibuja detrás.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        val contenedor = (application as ReservaSalasApp).container
        setContent {
            ReservaSalasTheme {
                NavegacionApp(sesion = contenedor.sesionDataStore.sesion)
            }
        }
    }

    /**
     * Se llama cada vez que la app vuelve a primer plano (ON_RESUME), por ejemplo al volver de
     * los ajustes. Si el usuario acaba de dar el permiso de alarmas exactas, se reprograman
     * los recordatorios para que dejen de ser inexactos.
     */
    override fun onResume() {
        super.onResume()
        val puede = puedeProgramarAlarmasExactas(this)
        if (podiaAlarmaExacta == false && puede) {
            (application as ReservaSalasApp).container.sincronizadorRecordatorios.reprogramarTodo()
        }
        podiaAlarmaExacta = puede
    }
}
