
package cl.duoc.reservasalas.app.ui.pantallas.salas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ui.componentes.TarjetaSala
import cl.duoc.reservasalas.app.ui.navegacion.Rutas

@Composable
fun SalasPantalla(
    navegar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SalasViewModel = viewModel(
        factory = SalasViewModel.Factory
    )

    val salas = viewModel.salas.collectAsState().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Salas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Selecciona una sala para ver sus detalles",
            style = MaterialTheme.typography.bodyMedium
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(salas) { sala ->
                TarjetaSala(
                    sala = sala,
                    alPresionar = {
                        navegar(Rutas.sala(sala.codigo))
                    }
                )
            }
        }
    }
}
