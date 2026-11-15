package com.unal.nachoquest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TriviaDiariaTest {

    private val hoy = 20_000L

    @Test
    fun `las preguntas del banco tienen cuatro opciones distintas e ids únicos`() {
        TriviaDiaria.banco.forEach { pregunta ->
            assertEquals("${pregunta.id}: debe tener 4 opciones", 4, pregunta.opciones.toSet().size)
            assertTrue("${pregunta.id}: respuesta fuera de rango", pregunta.indiceCorrecta in pregunta.opciones.indices)
        }
        val ids = TriviaDiaria.banco.map { it.id }
        assertEquals("Hay preguntas con el mismo id", ids.size, ids.toSet().size)
    }

    @Test
    fun `todos los jugadores reciben las mismas preguntas el mismo día`() {
        val preguntas = TriviaDiaria.preguntasDelDia(hoy)
        assertEquals(TriviaDiaria.PREGUNTAS_POR_DIA, preguntas.size)
        assertEquals(preguntas, TriviaDiaria.preguntasDelDia(hoy))
        assertNotEquals(preguntas, TriviaDiaria.preguntasDelDia(hoy + 1))
    }

    @Test
    fun `jugar en días seguidos alarga la racha`() {
        val nuevo = UserProfile()
        assertEquals(1, TriviaDiaria.rachaTrasJugar(nuevo, hoy))

        val jugoAyer = UserProfile(rachaDias = 3, ultimoDiaTrivia = hoy - 1)
        assertEquals(3, TriviaDiaria.rachaVigente(jugoAyer, hoy))
        assertEquals(4, TriviaDiaria.rachaTrasJugar(jugoAyer, hoy))
        assertFalse(TriviaDiaria.yaJugoHoy(jugoAyer, hoy))
    }

    @Test
    fun `saltarse un día reinicia la racha`() {
        val jugoAnteayer = UserProfile(rachaDias = 5, ultimoDiaTrivia = hoy - 2)
        assertEquals(0, TriviaDiaria.rachaVigente(jugoAnteayer, hoy))
        assertEquals(1, TriviaDiaria.rachaTrasJugar(jugoAnteayer, hoy))
    }

    @Test
    fun `repetir la trivia el mismo día no cambia la racha`() {
        val jugoHoy = UserProfile(rachaDias = 2, ultimoDiaTrivia = hoy)
        assertTrue(TriviaDiaria.yaJugoHoy(jugoHoy, hoy))
        assertEquals(2, TriviaDiaria.rachaTrasJugar(jugoHoy, hoy))
    }

    @Test
    fun `el bono de racha crece con los días y tiene tope`() {
        assertEquals(0, TriviaDiaria.bonoRacha(1))
        assertEquals(TriviaDiaria.XP_POR_DIA_DE_RACHA, TriviaDiaria.bonoRacha(2))
        assertEquals(TriviaDiaria.BONO_RACHA_MAXIMO, TriviaDiaria.bonoRacha(50))
        assertEquals(
            TriviaDiaria.XP_BASE + TriviaDiaria.bonoRacha(3),
            TriviaDiaria.retoDelDia(hoy, rachaActual = 3).recompensaXp
        )
    }
}
