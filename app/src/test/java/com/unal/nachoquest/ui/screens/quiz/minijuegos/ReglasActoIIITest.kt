package com.unal.nachoquest.ui.screens.quiz.minijuegos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Reglas de los minijuegos del Acto III. */
class ReglasActoIIITest {

    // ---------- Montaje (capítulo 9) ----------

    @Test
    fun `el montaje revuelve todos los fotogramas y nunca los deja en orden`() {
        repeat(200) { semilla ->
            val revuelto = ReglasMontaje.revolver(4, Random(semilla))
            assertEquals((0 until 4).toSet(), revuelto.toSet())
            assertEquals(4, revuelto.size)
            assertNotEquals("Semilla $semilla: salió en orden", (0 until 4).toList(), revuelto)
        }
    }

    @Test
    fun `solo cuenta el fotograma que sigue en la línea de tiempo`() {
        assertTrue(ReglasMontaje.esElSiguiente(fotograma = 0, colocados = 0))
        assertTrue(ReglasMontaje.esElSiguiente(fotograma = 3, colocados = 3))
        assertFalse(ReglasMontaje.esElSiguiente(fotograma = 2, colocados = 0))
        assertFalse(ReglasMontaje.esElSiguiente(fotograma = 1, colocados = 2))
    }

    @Test
    fun `las películas crecen en dificultad`() {
        val fotogramas = ReglasMontaje.PELICULAS.map { it.fotogramas }
        assertEquals(fotogramas.sorted(), fotogramas)
        assertTrue(fotogramas.all { it >= 2 })
    }

    @Test
    fun `menos errores dan más estrellas`() {
        assertEquals(3, ReglasMontaje.estrellas(0))
        assertEquals(3, ReglasMontaje.estrellas(1))
        assertEquals(2, ReglasMontaje.estrellas(4))
        assertEquals(1, ReglasMontaje.estrellas(5))
        assertEquals(1, ReglasMontaje.estrellas(30))
    }
}
