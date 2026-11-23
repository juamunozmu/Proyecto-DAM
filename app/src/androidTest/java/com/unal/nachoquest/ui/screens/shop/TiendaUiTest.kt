package com.unal.nachoquest.ui.screens.shop

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unal.nachoquest.domain.model.Tienda
import com.unal.nachoquest.domain.model.TipoAyuda
import com.unal.nachoquest.domain.model.UserProfile
import com.unal.nachoquest.domain.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compras en la tienda, con y sin saldo. El repositorio está simulado: guarda el perfil en
 * memoria y cobra igual que el real, sin tocar Firestore.
 */
@RunWith(AndroidJUnit4::class)
class TiendaUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val perfil = MutableStateFlow(UserProfile())

    private val repositorio = mockk<UserRepository>(relaxed = true) {
        every { userProfileFlow } returns perfil
        every { comprarAyuda(any()) } answers {
            val tipo = firstArg<TipoAyuda>()
            val actual = perfil.value
            if (actual.monedas < tipo.precio) {
                false
            } else {
                perfil.value = actual.copy(
                    monedas = actual.monedas - tipo.precio,
                    ayudas = actual.ayudas + (tipo.id to Tienda.reserva(actual, tipo) + 1)
                )
                true
            }
        }
        every { comprarAspecto(any()) } answers {
            val aspecto = Tienda.aspecto(firstArg())!!
            val actual = perfil.value
            if (actual.monedas < aspecto.precio) {
                false
            } else {
                perfil.value = actual.copy(
                    monedas = actual.monedas - aspecto.precio,
                    aspectos = actual.aspectos + aspecto.id,
                    aspectosEquipados = Tienda.alternarEquipado(actual.aspectosEquipados, aspecto.id)
                )
                true
            }
        }
    }

    private lateinit var viewModel: TiendaViewModel

    private fun abrirTienda(monedas: Int) {
        perfil.value = UserProfile(monedas = monedas)
        viewModel = TiendaViewModel(repositorio)
        compose.setContent { TiendaScreen(onVolver = {}, viewModel = viewModel) }
    }

    private fun tocar(texto: String) {
        compose.onNodeWithText(texto).performScrollTo().performClick()
    }

    @Test
    fun conSaldo_comprarUnaAyudaDescuentaMonedasYLaGuarda() {
        abrirTienda(monedas = 25)

        tocar("🪙 ${TipoAyuda.TIEMPO.precio}")

        verify(exactly = 1) { repositorio.comprarAyuda(TipoAyuda.TIEMPO) }
        compose.onNodeWithText("15").assertExists()
        compose.onNodeWithText("Tienes 1").assertExists()
        assertEquals(0, viewModel.sinSaldo.value)
    }

    @Test
    fun conSaldo_dosComprasSeguidasSeAcumulan() {
        abrirTienda(monedas = 25)

        tocar("🪙 ${TipoAyuda.TIEMPO.precio}")
        tocar("🪙 ${TipoAyuda.TIEMPO.precio}")

        compose.onNodeWithText("5").assertExists()
        compose.onNodeWithText("Tienes 2").assertExists()
    }

    @Test
    fun sinSaldo_laCompraNoCambiaNadaYAvisa() {
        abrirTienda(monedas = 5)

        tocar("🪙 ${TipoAyuda.COMODIN.precio}")

        verify(exactly = 1) { repositorio.comprarAyuda(TipoAyuda.COMODIN) }
        compose.onNodeWithText("5").assertExists()
        compose.onNodeWithText("Tienes 1").assertDoesNotExist()
        assertEquals(TipoAyuda.entries.size, compose.onAllNodesWithText("No tienes").fetchSemanticsNodes().size)
        // El contador hace que la pantalla muestre el aviso de «No te alcanzan las monedas»
        assertEquals(1, viewModel.sinSaldo.value)
    }

    @Test
    fun conSaldo_comprarUnAspectoSeLoPoneANacho() {
        val gafas = Tienda.aspecto("gafas")!!
        abrirTienda(monedas = 100)

        tocar("🪙 ${gafas.precio}")

        assertEquals(listOf("gafas"), perfil.value.aspectosEquipados)
        compose.onNodeWithText("${100 - gafas.precio}").assertExists()
        compose.onNodeWithText("Quitar").assertExists()
    }

    @Test
    fun sinSaldo_noSePuedeComprarUnAspecto() {
        val birrete = Tienda.aspecto("birrete")!!
        abrirTienda(monedas = 100)

        tocar("🪙 ${birrete.precio}")

        assertEquals(emptyList<String>(), perfil.value.aspectos)
        compose.onNodeWithText("Quitar").assertDoesNotExist()
        assertEquals(1, viewModel.sinSaldo.value)
    }
}
