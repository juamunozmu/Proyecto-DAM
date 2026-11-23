package com.unal.nachoquest.ui.screens.quiz

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unal.nachoquest.domain.model.PagoAyuda
import com.unal.nachoquest.domain.model.Tienda
import com.unal.nachoquest.domain.model.TipoAyuda
import com.unal.nachoquest.domain.model.TriviaDiaria
import com.unal.nachoquest.domain.model.UserProfile
import com.unal.nachoquest.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * El comodín 50:50 y el reloj de arena (+10 s) en las preguntas, con la pantalla y el ViewModel
 * reales. Se usa la trivia del día porque empieza directo en las preguntas, sin escena ni minijuego.
 */
@RunWith(AndroidJUnit4::class)
class AyudasPreguntaUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val repositorio = mockk<UserRepository>(relaxed = true) {
        every { userProfileFlow } returns MutableStateFlow(UserProfile(monedas = 50))
        every { perfilCargado } returns MutableStateFlow(true)
    }
    private val viewModel = QuizViewModel(repositorio)

    private fun abrirTrivia() {
        compose.setContent {
            QuizScreen(puntoId = TriviaDiaria.PUNTO_ID, onTerminar = {}, viewModel = viewModel)
        }
        compose.waitUntil(5_000) { viewModel.uiState.value.preguntaActual != null }
        compose.waitForIdle()
    }

    // ---------- Comodín 50:50 ----------

    @Test
    fun comodin_elPrimeroEsGratisYDescartaDosOpcionesIncorrectas() {
        abrirTrivia()
        compose.onNodeWithText("GRATIS").assertExists()

        compose.onNodeWithText("50:50", substring = true).performClick()
        compose.waitForIdle()

        val state = viewModel.uiState.value
        val pregunta = state.preguntaActual!!
        assertEquals(2, state.opcionesDescartadas.size)
        assertFalse(pregunta.indiceCorrecta in state.opcionesDescartadas)
        pregunta.opciones.forEachIndexed { i, opcion ->
            val nodo = compose.onNodeWithText(opcion)
            if (i in state.opcionesDescartadas) nodo.assertIsNotEnabled() else nodo.assertIsEnabled()
        }
        // Era el gratis: no se gastó ninguna ayuda ni moneda
        verify(exactly = 0) { repositorio.usarAyuda(any()) }
    }

    @Test
    fun comodin_noSePuedeUsarDosVecesEnLaMismaPregunta() {
        abrirTrivia()

        compose.onNodeWithText("50:50", substring = true).performClick()
        compose.waitForIdle()
        val descartadas = viewModel.uiState.value.opcionesDescartadas

        compose.onNodeWithText("50:50", substring = true).assertIsNotEnabled()
        compose.onNodeWithText("50:50", substring = true).performClick()
        compose.waitForIdle()

        assertEquals(descartadas, viewModel.uiState.value.opcionesDescartadas)
        verify(exactly = 0) { repositorio.usarAyuda(any()) }
    }

    @Test
    fun comodin_unaOpcionDescartadaNoSePuedeResponder() {
        abrirTrivia()
        compose.onNodeWithText("50:50", substring = true).performClick()
        compose.waitForIdle()

        val state = viewModel.uiState.value
        val descartada = state.opcionesDescartadas.first()
        compose.onNodeWithText(state.preguntaActual!!.opciones[descartada]).performClick()
        compose.waitForIdle()

        assertFalse(viewModel.uiState.value.mostrarResultadoPregunta)
    }

    // ---------- Reloj de arena (+10 s) ----------

    @Test
    fun reloj_sumaDiezSegundosYSeCobra() {
        every { repositorio.usarAyuda(TipoAyuda.TIEMPO) } returns PagoAyuda.CON_MONEDAS
        abrirTrivia()
        val antes = viewModel.uiState.value.tiempoRestanteMs

        compose.onNodeWithText("+${Tienda.SEGUNDOS_TIEMPO_EXTRA} s", substring = true).performClick()
        compose.waitForIdle()

        val state = viewModel.uiState.value
        assertTrue(state.tiempoExtraUsado)
        // El reloj sigue corriendo, así que se deja un margen de un segundo
        assertTrue(
            "Quedaban $antes ms y ahora ${state.tiempoRestanteMs} ms",
            state.tiempoRestanteMs >= antes + Tienda.SEGUNDOS_TIEMPO_EXTRA * 1000L - 1_000L
        )
        verify(exactly = 1) { repositorio.usarAyuda(TipoAyuda.TIEMPO) }
        // Solo se usa una vez por pregunta
        compose.onNodeWithText("+${Tienda.SEGUNDOS_TIEMPO_EXTRA} s", substring = true).assertIsNotEnabled()
        compose.onNodeWithText("USADO").assertExists()
    }

    @Test
    fun reloj_sinMonedasNoSumaTiempoYAvisa() {
        every { repositorio.usarAyuda(TipoAyuda.TIEMPO) } returns PagoAyuda.SIN_SALDO
        abrirTrivia()
        val antes = viewModel.uiState.value.tiempoRestanteMs

        compose.onNodeWithText("+${Tienda.SEGUNDOS_TIEMPO_EXTRA} s", substring = true).performClick()
        compose.waitForIdle()

        val state = viewModel.uiState.value
        assertFalse(state.tiempoExtraUsado)
        assertTrue(state.tiempoRestanteMs <= antes)
        // La pantalla muestra el aviso de «No te alcanzan las monedas» cuando sube este contador
        assertEquals(1, state.sinSaldoAnimado)
        compose.onNodeWithText("+${Tienda.SEGUNDOS_TIEMPO_EXTRA} s", substring = true).assertIsEnabled()
    }
}
