package cl.duoc.reservasalas.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/*
 * Ayudas compartidas por las pruebas de ViewModel.
 *
 * Por qué existen: un ViewModel lanza corrutinas en el "hilo principal" (Dispatchers.Main), que en
 * las pruebas reemplaza Dispatchers.setMain. Si la prueba termina y llama Dispatchers.resetMain()
 * mientras una de esas corrutinas todavía espera algo (ej. leer el DataStore en otro hilo), al
 * despertar ya no encuentra el hilo principal y lanza un error. Ese error lo recibe la SIGUIENTE
 * prueba que use runTest, que falla sin tener la culpa (prueba inestable).
 * Solución: cerrar los ViewModel y esperar a que sus corrutinas terminen ANTES de resetMain().
 */

/**
 * Crea un ViewModel dentro de [this] (un ViewModelStore), como lo hace Android en una pantalla.
 * Así se puede cerrar al final con [cerrarYEsperar].
 * reified: permite usar VM::class dentro de la función.
 */
inline fun <reified VM : ViewModel> ViewModelStore.crear(noinline crear: () -> VM): VM =
    ViewModelProvider.create(this, viewModelFactory { initializer { crear() } })[VM::class]

/**
 * Cierra los ViewModel del almacén (se cancelan sus corrutinas) y espera, máximo 5 segundos,
 * a que terminen de verdad. Llamar en @After ANTES de Dispatchers.resetMain().
 */
fun ViewModelStore.cerrarYEsperar(vararg viewModels: ViewModel) {
    // Se guarda el Job de cada viewModelScope antes de cerrar: después ya no se puede pedir.
    val trabajos = viewModels.map { it.viewModelScope.coroutineContext.job }
    clear()
    runBlocking { withTimeout(5_000) { trabajos.joinAll() } }
}

/**
 * Cancela el alcance del DataStore temporal y espera a que termine, para que no siga escribiendo
 * cuando TemporaryFolder borre la carpeta.
 */
fun CoroutineScope.cancelarYEsperar() {
    // Se toma el Job afuera: dentro de runBlocking, "coroutineContext" sería el de runBlocking.
    val trabajo = coroutineContext.job
    runBlocking { trabajo.cancelAndJoin() }
}
