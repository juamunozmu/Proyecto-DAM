package com.unal.nachoquest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogrosTest {

    private fun logro(perfil: UserProfile, id: String) = Logros.calcular(perfil).single { it.id == id }

    @Test
    fun `un perfil nuevo no tiene logros`() {
        assertTrue(Logros.calcular(UserProfile()).none { it.desbloqueado })
    }

    @Test
    fun `el primer fragmento desbloquea la primera exploración`() {
        val perfil = UserProfile(retosCompletados = listOf("reto_plaza_che"))
        assertTrue(logro(perfil, "primera_exploracion").desbloqueado)
        assertFalse(logro(perfil, "maestro_biblioteca").desbloqueado)
    }

    @Test
    fun `los retos de lugar tienen sus logros pero no cuentan como fragmento`() {
        val cinco = RetosLugares.retos.take(5).map { it.retoId }
        val perfil = UserProfile(retosCompletados = cinco)
        assertTrue(logro(perfil, "guia_campus").desbloqueado)
        assertFalse(logro(perfil, "enciclopedia").desbloqueado)
        assertEquals(5, logro(perfil, "enciclopedia").progreso)
        assertFalse(logro(perfil, "primera_exploracion").desbloqueado)

        val todos = UserProfile(retosCompletados = RetosLugares.retos.map { it.retoId })
        assertTrue(logro(todos, "enciclopedia").desbloqueado)
    }

    @Test
    fun `los logros incrementales muestran el progreso`() {
        val perfil = UserProfile(lugaresDescubiertos = List(4) { "lugar_$it" })
        val caminante = logro(perfil, "caminante")
        assertFalse(caminante.desbloqueado)
        assertEquals(4, caminante.progreso)
        assertEquals(10, caminante.meta)
    }

    @Test
    fun `solo se anuncian los logros que el jugador no tenía`() {
        val antes = UserProfile(retosCompletados = listOf("reto_plaza_che"))
        val despues = antes.copy(
            retosCompletados = listOf("reto_plaza_che", "reto_biblio"),
            estrellasPorReto = mapOf("reto_biblio" to 3)
        )
        val nuevos = Logros.nuevos(antes, despues).map { it.id }
        assertEquals(listOf("maestro_biblioteca", "mente_brillante"), nuevos)
        assertTrue(Logros.nuevos(despues, despues).isEmpty())
    }

    @Test
    fun `la racha de trivia y la exploración tienen sus logros`() {
        val perfil = UserProfile(
            mejorRachaDias = 3,
            triviasPerfectas = 1,
            lugaresDescubiertos = List(40) { "lugar_$it" }
        )
        assertTrue(logro(perfil, "en_racha").desbloqueado)
        assertTrue(logro(perfil, "sabelotodo").desbloqueado)
        assertTrue(logro(perfil, "cartografo").desbloqueado)
        assertFalse(logro(perfil, "constancia").desbloqueado)
        assertEquals(3, logro(perfil, "constancia").progreso)
    }

    @Test
    fun `terminar la historia convierte al jugador en leyenda`() {
        val perfil = UserProfile(
            retosCompletados = Historia.capitulos.map { it.retoId },
            estrellasPorReto = mapOf("reto_biblio" to 3)
        )
        assertTrue(logro(perfil, "leyenda").desbloqueado)
        assertTrue(logro(perfil, "mente_brillante").desbloqueado)
        assertEquals(Historia.capitulos.size, logro(perfil, "leyenda").progreso)
    }
}
