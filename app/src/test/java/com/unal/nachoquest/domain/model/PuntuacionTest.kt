package com.unal.nachoquest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PuntuacionTest {

    // Capítulo: 3 preguntas y 2 corazones después del minijuego
    private fun capitulo(
        correctas: Int,
        vidas: Int,
        estrellasMinijuego: Int = 3,
        bonoVelocidad: Int = 0,
        esRepeticion: Boolean = false
    ) = Puntuacion.calcular(
        totalPreguntas = 3,
        respuestasCorrectas = correctas,
        vidasRestantes = vidas,
        estrellasMinijuego = estrellasMinijuego,
        bonoVelocidad = bonoVelocidad,
        recompensaXp = 100,
        esRepeticion = esRepeticion
    )

    @Test
    fun `una partida perfecta da tres estrellas y todos los bonos`() {
        val resultado = capitulo(correctas = 3, vidas = 2, bonoVelocidad = 12)
        assertTrue(resultado.aprobado)
        assertEquals(3, resultado.estrellas)
        // 100 del reto + 12 de velocidad + 3 estrellas de minijuego x 10 + 25 por partida perfecta
        assertEquals(167, resultado.xp)
    }

    @Test
    fun `un error quita una estrella y el bono de partida perfecta`() {
        val resultado = capitulo(correctas = 2, vidas = 1)
        assertTrue(resultado.aprobado)
        assertEquals(2, resultado.estrellas)
        assertEquals(100 + 30, resultado.xp)
    }

    @Test
    fun `las estrellas no superan las del minijuego`() {
        val resultado = capitulo(correctas = 3, vidas = 2, estrellasMinijuego = 1)
        assertEquals(1, resultado.estrellas)
        assertEquals(100 + 10, resultado.xp)
    }

    @Test
    fun `sin corazones el reto se pierde y no da nada`() {
        val resultado = capitulo(correctas = 1, vidas = 0, bonoVelocidad = 8)
        assertFalse(resultado.aprobado)
        assertEquals(0, resultado.estrellas)
        assertEquals(0, resultado.xp)
    }

    @Test
    fun `repetir un reto mejora estrellas pero no da xp`() {
        val resultado = capitulo(correctas = 3, vidas = 2, esRepeticion = true)
        assertTrue(resultado.aprobado)
        assertEquals(3, resultado.estrellas)
        assertEquals(0, resultado.xp)
    }

    @Test
    fun `los retos sin minijuego cuentan solo las preguntas`() {
        val resultado = Puntuacion.calcular(
            totalPreguntas = 5,
            respuestasCorrectas = 3,
            vidasRestantes = 1,
            estrellasMinijuego = null,
            bonoVelocidad = 5,
            recompensaXp = 80,
            esRepeticion = false
        )
        assertTrue(resultado.aprobado)
        // Dos errores dejan una estrella (nunca menos de una si se aprueba)
        assertEquals(1, resultado.estrellas)
        assertEquals(85, resultado.xp)
    }
}
