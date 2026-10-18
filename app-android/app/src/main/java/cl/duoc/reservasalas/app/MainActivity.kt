package cl.duoc.reservasalas.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.reservasalas.app.ui.navegacion.NavegacionApp
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme

class MainActivity : ComponentActivity() {
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
}
