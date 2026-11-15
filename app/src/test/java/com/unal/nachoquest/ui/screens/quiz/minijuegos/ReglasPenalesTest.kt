package com.unal.nachoquest.ui.screens.quiz.minijuegos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class ReglasPenalesTest {

    @Test
    fun `la mira y el arquero nunca se salen del arco`() {
        val fases = List(200) { it * 0.1f }
        fases.forEach { fase ->
            assertTrue(ReglasPenales.posicion(fase, ReglasPenales.AMPLITUD_MIRA) in 0f..1f)
            assertTrue(ReglasPenales.posicion(fase, ReglasPenales.AMPLITUD_ARQUERO) in 0f..1f)
        }
        assertEquals(0.5f, ReglasPenales.posicion(0f, ReglasPenales.AMPLITUD_MIRA), 0.0001f)
    }

    @Test
    fun `es gol solo si el arquero no alcanza el balón`() {
        assertFalse("Al cuerpo del arquero", ReglasPenales.esGol(xBalon = 0.5f, xArquero = 0.5f))
        assertFalse("Rozando sus manos", ReglasPenales.esGol(xBalon = 0.5f, xArquero = 0.68f))
        assertTrue("Al otro palo", ReglasPenales.esGol(xBalon = 0.1f, xArquero = 0.8f))
        assertTrue("Justo fuera de su alcance", ReglasPenales.esGol(xBalon = 0.5f, xArquero = 0.72f))
    }

    @Test
    fun `siempre queda un hueco para meter gol`() {
        // Con el arquero en cualquier punto de su recorrido, alguna posición de la mira es gol
        List(100) { it * (2 * PI.toFloat() / 100) }.forEach { fase ->
            val arquero = ReglasPenales.posicion(fase, ReglasPenales.AMPLITUD_ARQUERO)
            val izquierda = ReglasPenales.posicion(-PI.toFloat() / 2, ReglasPenales.AMPLITUD_MIRA)
            val derecha = ReglasPenales.posicion(PI.toFloat() / 2, ReglasPenales.AMPLITUD_MIRA)
            assertTrue(ReglasPenales.esGol(izquierda, arquero) || ReglasPenales.esGol(derecha, arquero))
        }
    }

    @Test
    fun `el arquero se acelera con cada gol`() {
        assertTrue(ReglasPenales.velocidadArquero(1) > ReglasPenales.velocidadArquero(0))
        assertTrue(ReglasPenales.velocidadArquero(2) > ReglasPenales.velocidadArquero(1))
        // Con la tanda ya ganada no hay un nivel más rápido
        assertEquals(ReglasPenales.velocidadArquero(2), ReglasPenales.velocidadArquero(5), 0f)
    }

    @Test
    fun `menos tiros dan más estrellas`() {
        assertEquals(3, ReglasPenales.estrellas(3))
        assertEquals(3, ReglasPenales.estrellas(4))
        assertEquals(2, ReglasPenales.estrellas(5))
        assertEquals(2, ReglasPenales.estrellas(6))
        assertEquals(1, ReglasPenales.estrellas(7))
    }
}
