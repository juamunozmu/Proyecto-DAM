package com.unal.nachoquest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoriaTest {

    private val retosEnOrden = Historia.capitulos.map { it.retoId }

    @Test
    fun `la historia avanza capítulo por capítulo`() {
        assertEquals(1, Historia.capituloActual(emptyList())?.numero)
        assertEquals(2, Historia.capituloActual(retosEnOrden.take(1))?.numero)
        assertEquals(4, Historia.capituloActual(retosEnOrden.take(3))?.numero)
        assertFalse(Historia.historiaCompletada(retosEnOrden.take(3)))
    }

    @Test
    fun `la historia termina al completar todos los capítulos`() {
        assertNull(Historia.capituloActual(retosEnOrden))
        assertTrue(Historia.historiaCompletada(retosEnOrden))
    }

    @Test
    fun `el acto I termina en el estadio con la tanda de penales y su epílogo`() {
        val ultimo = Historia.capitulosDelActo(1).last()
        assertEquals(5, ultimo.numero)
        assertEquals("poi_estadio", ultimo.puntoId)
        assertEquals(TipoMinijuego.PENALES, ultimo.minijuego)
        assertEquals(Historia.ESCENA_EPILOGO, ultimo.escenaFinal)
        // La brújula que entrega el auditorio es la que abre el estadio
        assertEquals("brujula_unal", ultimo.itemRequeridoId)
        assertEquals(ultimo.puntoId, CatalogoItems.obtenerPorId("brujula_unal")?.desbloqueaPuntoId)
    }

    @Test
    fun `el acto II empieza con la Copa y termina en la Concha Acústica`() {
        val acto2 = Historia.capitulosDelActo(2)
        assertEquals(listOf(6, 7, 8), acto2.map { it.numero })
        assertEquals("copa_memoria", acto2.first().itemRequeridoId)
        assertEquals(acto2.first().puntoId, CatalogoItems.obtenerPorId("copa_memoria")?.desbloqueaPuntoId)
        assertEquals("edif_761", acto2.last().puntoId)
        assertEquals(Historia.ESCENA_EPILOGO_FINAL, acto2.last().escenaFinal)
        // Solo el último capítulo de cada acto tiene epílogo
        assertEquals(2, Historia.capitulos.count { it.escenaFinal != null })
    }

    @Test
    fun `terminar el acto I no termina la historia`() {
        val acto1 = Historia.capitulosDelActo(1).map { it.retoId }
        assertTrue(Historia.actoCompletado(1, acto1))
        assertFalse(Historia.actoCompletado(2, acto1))
        assertFalse(Historia.historiaCompletada(acto1))
        assertEquals(6, Historia.capituloActual(acto1)?.numero)
    }

    @Test
    fun `cada minijuego se usa en un solo capítulo`() {
        val usados = Historia.capitulos.map { it.minijuego }
        assertEquals(usados.size, usados.toSet().size)
        assertEquals(TipoMinijuego.entries.toSet(), usados.toSet())
    }

    @Test
    fun `los retos de lugares que no son capítulos usan un id genérico`() {
        assertEquals("reto_biblio", Historia.retoIdParaPunto("edif_102"))
        assertEquals("reto_campus_edif_999", Historia.retoIdParaPunto("edif_999"))
    }

    @Test
    fun `las escenas usan el nombre del jugador`() {
        val prologo = Historia.escena(Historia.ESCENA_PROLOGO, "Ana")
        assertTrue(prologo.any { "Ana" in it.texto })
        assertTrue(prologo.none { "{jugador}" in it.texto })
    }

    @Test
    fun `el primer nombre sale del nombre completo del perfil`() {
        assertEquals("Juan", UserProfile(nombre = "  Juan Esteban Muñoz").primerNombre())
        assertEquals("primíparo", UserProfile(nombre = " ").primerNombre())
    }
}
