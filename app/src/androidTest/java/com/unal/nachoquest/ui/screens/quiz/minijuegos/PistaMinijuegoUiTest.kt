package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.Tienda
import com.unal.nachoquest.domain.model.TipoMinijuego
import com.unal.nachoquest.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Botón de pista del minijuego, con el ahorcado del capítulo 2. El cobro se simula con
 * [puedePagar]: así se prueba tanto la pista pagada como la que no alcanza a pagarse.
 */
@RunWith(AndroidJUnit4::class)
class PistaMinijuegoUiTest {

    @get:Rule
    val compose = createComposeRule()

    private val capitulo = Historia.capitulos.first { it.minijuego == TipoMinijuego.LETRAS }
    private var cobros = 0
    private var puedePagar = true

    private fun abrirMinijuego() {
        compose.setContent {
            PantallaMinijuego(
                capitulo = capitulo,
                perfil = UserProfile(monedas = 100),
                onPedirPista = {
                    cobros++
                    puedePagar
                },
                onCompletado = {},
                onSalir = {}
            )
        }
    }

    private fun botonPista(usadas: Int) =
        compose.onNodeWithText("Pista $usadas/${Tienda.MAX_PISTAS_MINIJUEGO}", substring = true)

    @Test
    fun pistaPagada_subeElContador() {
        abrirMinijuego()

        botonPista(0).performClick()

        assertEquals(1, cobros)
        botonPista(1).assertExists()
    }

    @Test
    fun pistaSinPagar_noCuenta() {
        puedePagar = false
        abrirMinijuego()

        botonPista(0).performClick()

        assertEquals(1, cobros)
        botonPista(0).assertExists()
    }

    @Test
    fun alLlegarAlMaximo_elBotonSeDesactiva() {
        abrirMinijuego()

        repeat(Tienda.MAX_PISTAS_MINIJUEGO) { usadas -> botonPista(usadas).performClick() }

        val max = Tienda.MAX_PISTAS_MINIJUEGO
        botonPista(max).assertIsNotEnabled()
        compose.onNodeWithText("SIN PISTAS").assertExists()
        // Tocar otra vez no cobra una pista de más
        botonPista(max).performClick()
        assertEquals(max, cobros)
    }
}
