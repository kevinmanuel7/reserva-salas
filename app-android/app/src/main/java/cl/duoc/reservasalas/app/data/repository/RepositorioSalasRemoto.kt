package cl.duoc.reservasalas.app.data.repository

import cl.duoc.reservasalas.app.data.modelo.Sala
import cl.duoc.reservasalas.app.data.remote.SalasApi
import cl.duoc.reservasalas.app.data.remote.dto.SalaDto
import cl.duoc.reservasalas.app.data.remote.dto.aModelo
import cl.duoc.reservasalas.app.data.remote.dto.aPeticion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Salas reales desde ms-salas. Cumple el mismo contrato que RepositorioSalasFalso,
 * así que las pantallas no notan la diferencia.
 */
class RepositorioSalasRemoto(
    private val api: SalasApi
) : RepositorioSalas {

    // Copia en memoria que observan las pantallas. Parte vacía hasta que responde el servidor.
    private val _salas = MutableStateFlow<List<Sala>>(emptyList())
    override val salas: StateFlow<List<Sala>> = _salas.asStateFlow()

    // Lo que respondió el servidor, ordenado por id. Se usa al guardar.
    private var dtosPorId: Map<Long, SalaDto> = emptyMap()

    // Alcance propio para la primera carga, que no depende de ninguna pantalla.
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        alcance.launch { refrescar() }
    }

    override fun buscar(codigo: String): Sala? =
        _salas.value.find { it.codigo == codigo }

    override suspend fun refrescar() {
        try {
            val dtos = api.listar()
            dtosPorId = dtos.associateBy { it.id }
            _salas.value = dtos.map { it.aModelo() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sin conexión o error del servidor: se conserva la última lista que se tenía.
        }
    }

    override suspend fun guardar(sala: Sala) {
        val original = dtosPorId[sala.id]
            ?: throw IllegalStateException("La sala ${sala.codigo} no existe en el servidor")

        val actualizada = api.actualizar(sala.id, sala.aPeticion(original))

        dtosPorId = dtosPorId + (actualizada.id to actualizada)
        _salas.update { lista ->
            lista.map { if (it.id == actualizada.id) actualizada.aModelo() else it }
        }
    }
}