package cl.duoc.reservasalas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import cl.duoc.reservasalas.app.ui.screens.LoginScreen
import cl.duoc.reservasalas.app.ui.theme.ReservaSalasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReservaSalasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Por ahora la app solo tiene la pantalla de login (sin navegación).
                    LoginScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
