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
        assertEquals(3, Historia.capitulos.count { it.escenaFinal != null })
    }

    @Test
    fun `el acto III empieza con la Copa Restaurada en la antigua sede de Cine y Televisión`() {
        val primero = Historia.capitulosDelActo(3).first()
        assertEquals(9, primero.numero)
        assertEquals("edif_701", primero.puntoId)
        assertEquals("copa_restaurada", primero.itemRequeridoId)
        assertEquals(primero.puntoId, CatalogoItems.obtenerPorId("copa_restaurada")?.desbloqueaPuntoId)
        assertEquals(TipoMinijuego.MONTAJE, primero.minijuego)
        assertEquals("Los Hilos de la Memoria", Historia.titulosActos[3])
    }

    @Test
    fun `el acto III termina con los relevos en el Polideportivo y su epílogo`() {
        val ultimo = Historia.capitulosDelActo(3).last()
        assertEquals(13, ultimo.numero)
        assertEquals(TipoMinijuego.RELEVOS, ultimo.minijuego)
        assertEquals(Historia.ESCENA_EPILOGO_ACTO3, ultimo.escenaFinal)
        assertTrue(Historia.escena(Historia.ESCENA_EPILOGO_ACTO3, "Ana").isNotEmpty())
        assertEquals("Los Hilos de la Memoria", Historia.titulosActos[ultimo.acto])
    }

    @Test
    fun `quien terminó el acto III sigue con el acto IV en el Uriel Gutiérrez`() {
        val primero = Historia.capitulosDelActo(4).first()
        assertEquals(14, primero.numero)
        assertEquals("edif_861", primero.puntoId)
        assertEquals("madeja_memoria", primero.itemRequeridoId)
        assertEquals(primero.puntoId, CatalogoItems.obtenerPorId("madeja_memoria")?.desbloqueaPuntoId)
        assertEquals("El Sello Pendiente", Historia.titulosActos[4])
        val retosHastaActo3 = (1..3).flatMap { Historia.capitulosDelActo(it) }.map { it.retoId }
        assertEquals(primero, Historia.capituloActual(retosHastaActo3))
    }

    @Test
    fun `los folios del acto IV abren cada capítulo en orden`() {
        val acto4 = Historia.capitulosDelActo(4)
        assertEquals(listOf("edif_861", "edif_500B", "edif_571", "edif_408A"), acto4.take(4).map { it.puntoId })
        acto4.zipWithNext().forEach { (anterior, siguiente) ->
            assertEquals(siguiente.puntoId, CatalogoItems.obtenerPorId(anterior.itemRecompensaId)?.desbloqueaPuntoId)
        }
    }

    @Test
    fun `el Sello y el Búho aparecen en el acto IV`() {
        val escenas = Historia.capitulosDelActo(4).flatMap {
            Historia.escena(it.escenaIntro, "Ana") + Historia.escena(it.escenaOutro, "Ana")
        }
        assertTrue(escenas.any { it.hablante == Hablante.SELLO })
        assertTrue(escenas.any { it.hablante == Hablante.BUHO })
        // El Búho llega en el Archivo Central e Histórico
        val archivo = Historia.capitulosDelActo(4).first { it.puntoId == "edif_500B" }
        assertTrue(Historia.escena(archivo.escenaIntro, "Ana").any { it.hablante == Hablante.BUHO })
    }

    @Test
    fun `los hilos del acto III abren cada capítulo en orden`() {
        val acto3 = Historia.capitulosDelActo(3)
        assertEquals(listOf("edif_701", "edif_408", "edif_426", "edif_251", "edif_103"), acto3.map { it.puntoId })
        acto3.zipWithNext().forEach { (anterior, siguiente) ->
            assertEquals(siguiente.puntoId, CatalogoItems.obtenerPorId(anterior.itemRecompensaId)?.desbloqueaPuntoId)
        }
    }

    @Test
    fun `quien terminó el acto II sigue con el acto III`() {
        val actos1y2 = (Historia.capitulosDelActo(1) + Historia.capitulosDelActo(2)).map { it.retoId }
        assertTrue(Historia.actoCompletado(2, actos1y2))
        assertFalse(Historia.historiaCompletada(actos1y2))
        assertEquals(9, Historia.capituloActual(actos1y2)?.numero)
    }

    @Test
    fun `el Enredo y el Recuerdo hablan en el acto III`() {
        val escena = Historia.escena("cap9_intro", "Ana")
        assertTrue(escena.any { it.hablante == Hablante.ENREDO })
        assertTrue(escena.any { it.hablante == Hablante.RECUERDO })
    }

    @Test
    fun `los actos se numeran en romanos`() {
        assertEquals(listOf("I", "II", "III", "IV"), (1..4).map(Historia::romano))
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
