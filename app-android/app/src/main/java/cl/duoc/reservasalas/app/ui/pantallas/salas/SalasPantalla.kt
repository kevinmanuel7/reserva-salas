package cl.duoc.reservasalas.app.ui.pantallas.salas

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.reservasalas.app.ui.componentes.EncabezadoMaqueta
import cl.duoc.reservasalas.app.ui.componentes.TarjetaSala
import cl.duoc.reservasalas.app.ui.navegacion.Rutas
import cl.duoc.reservasalas.app.ui.theme.*

@Composable
fun SalasPantalla(navegar: (String) -> Unit, modifier: Modifier = Modifier) {
    val vm: SalasViewModel = viewModel(factory = SalasViewModel.Factory)
    val salas by vm.salas.collectAsState()
    var busqueda by remember { mutableStateOf("") }
    var filtro by remember { mutableStateOf<String?>(null) }
    val filtradas = salas.filter { sala ->
        (sala.codigo.contains(busqueda, true) || sala.nombre.contains(busqueda, true)) &&
            (filtro == null || sala.equipos.any { it.nombre.contains(filtro!!, true) })
    }
    Column(modifier.fillMaxSize().background(Fondo)) {
        EncabezadoMaqueta("Salas")
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("Buscar por nombre o código") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Superficie, unfocusedContainerColor = Superficie),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Proyector", "Computadores", "Climatización").forEach { nombre ->
                FilterChip(
                    selected = filtro == nombre,
                    onClick = { filtro = if (filtro == nombre) null else nombre },
                    label = { Text(nombre) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NegroDuoc, selectedLabelColor = Blanco, containerColor = Superficie)
                )
            }
        }
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (filtradas.isEmpty()) item { Text("No se encontraron salas con esos criterios", modifier = Modifier.padding(12.dp)) }
            items(filtradas, key = { it.codigo }) { sala ->
                TarjetaSala(sala = sala, alPresionar = { navegar(Rutas.sala(sala.codigo)) })
            }
        }
    }
}
