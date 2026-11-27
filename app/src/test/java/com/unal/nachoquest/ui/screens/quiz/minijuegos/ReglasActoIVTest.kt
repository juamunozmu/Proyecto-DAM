package com.unal.nachoquest.ui.screens.quiz.minijuegos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Reglas de los minijuegos del Acto IV. */
class ReglasActoIVTest {

    // ---------- Trámites (capítulo 14) ----------

    @Test
    fun `llegan veinte papeles con la misma cantidad de cada tipo`() {
        repeat(50) { semilla ->
            val papeles = ReglasTramites.papeles(random = Random(semilla))
            assertEquals(ReglasTramites.PAPELES, papeles.size)
            TipoTramite.entries.forEach { tipo ->
                assertEquals(ReglasTramites.PAPELES / TipoTramite.entries.size, papeles.count { it == tipo })
            }
        }
    }

    @Test
    fun `cada papel da menos tiempo que el anterior`() {
        val tiempos = (0 until ReglasTramites.PAPELES).map { ReglasTramites.segundosPara(it) }
        assertEquals(ReglasTramites.SEGUNDOS_PRIMERO, tiempos.first(), 0.001f)
        assertEquals(ReglasTramites.SEGUNDOS_ULTIMO, tiempos.last(), 0.001f)
        tiempos.zipWithNext().forEach { (antes, despues) -> assertTrue(despues < antes) }
    }

    @Test
    fun `solo vale la bandeja del mismo tipo y menos errores dan más estrellas`() {
        assertTrue(ReglasTramites.esCorrecta(TipoTramite.CARNE, TipoTramite.CARNE))
        assertFalse(ReglasTramites.esCorrecta(TipoTramite.CARNE, TipoTramite.RECIBO))
        assertEquals(3, ReglasTramites.estrellas(0))
        assertEquals(3, ReglasTramites.estrellas(1))
        assertEquals(2, ReglasTramites.estrellas(4))
        assertEquals(1, ReglasTramites.estrellas(8))
    }

    // ---------- Archivo (capítulo 15) ----------

    @Test
    fun `los estantes están en orden y el folio está en una de sus cajas`() {
        ReglasArchivo.CAJAS_POR_ESTANTE.forEach { cajas ->
            repeat(30) { semilla ->
                val estante = ReglasArchivo.estante(cajas, Random(semilla))
                assertEquals(cajas, estante.numeros.size)
                assertEquals(estante.numeros.sorted(), estante.numeros)
                assertEquals(cajas, estante.numeros.toSet().size)
                assertTrue(estante.objetivo in estante.numeros.indices)
            }
        }
    }

    @Test
    fun `abrir una caja dice si el folio está antes, después o ahí`() {
        val estante = Estante(listOf(110, 220, 330, 440, 550), objetivo = 3)
        assertEquals(Comparacion.DESPUES, ReglasArchivo.comparar(estante, 1))
        assertEquals(Comparacion.ANTES, ReglasArchivo.comparar(estante, 4))
        assertEquals(Comparacion.AQUI, ReglasArchivo.comparar(estante, 3))
        assertEquals(2..4, ReglasArchivo.rangoPosible(estante, listOf(1)))
        assertEquals(2..3, ReglasArchivo.rangoPosible(estante, listOf(1, 4)))
        assertEquals(3..3, ReglasArchivo.rangoPosible(estante, listOf(1, 3)))
    }

    @Test
    fun `abriendo siempre la del medio cualquier folio se encuentra en el óptimo`() {
        assertEquals(listOf(3, 4, 5), ReglasArchivo.CAJAS_POR_ESTANTE.map { ReglasArchivo.optimo(it) })
        ReglasArchivo.CAJAS_POR_ESTANTE.forEach { cajas ->
            for (objetivo in 0 until cajas) {
                val estante = Estante((1..cajas).map { it * 10 }, objetivo)
                val abiertas = mutableListOf<Int>()
                while (abiertas.lastOrNull() != objetivo) {
                    abiertas.add(ReglasArchivo.delMedio(ReglasArchivo.rangoPosible(estante, abiertas)))
                }
                assertTrue("$cajas cajas, folio $objetivo: ${abiertas.size} aperturas", abiertas.size <= ReglasArchivo.optimo(cajas))
            }
        }
    }

    // ---------- Fechas (capítulo 16) ----------

    @Test
    fun `cada periódico tiene cuatro titulares con años distintos`() {
        assertEquals(3, ReglasFechas.RONDAS.size)
        ReglasFechas.RONDAS.forEach { ronda ->
            assertEquals(4, ronda.size)
            assertEquals(ronda.size, ronda.map { it.anio }.toSet().size)
            assertEquals(ronda.size, ronda.map { it.texto }.toSet().size)
        }
        val titular = ReglasFechas.RONDAS.first().first()
        assertTrue(ReglasFechas.esCorrecta(titular, titular.anio))
        assertFalse(ReglasFechas.esCorrecta(titular, titular.anio + 1))
        assertEquals(3, ReglasFechas.estrellas(1))
        assertEquals(1, ReglasFechas.estrellas(9))
    }

    // ---------- Pila (capítulo 17) ----------

    @Test
    fun `una carpeta grande nunca va sobre una pequeña`() {
        val inicio = ReglasPila.inicial(3)
        assertEquals(listOf(listOf(3, 2, 1), emptyList(), emptyList()), inicio)
        val uno = ReglasPila.mover(inicio, Movimiento(0, 2))
        assertEquals(listOf(listOf(3, 2), emptyList(), listOf(1)), uno)
        // La 2 no puede ir sobre la 1
        assertFalse(ReglasPila.puedeMover(uno, Movimiento(0, 2)))
        assertEquals(uno, ReglasPila.mover(uno, Movimiento(0, 2)))
        // Desde una ventanilla vacía no sale nada
        assertFalse(ReglasPila.puedeMover(uno, Movimiento(1, 0)))
    }

    @Test
    fun `desde el inicio la solución es el mínimo y resuelve la pila`() {
        ReglasPila.CARPETAS_POR_NIVEL.forEach { carpetas ->
            val solucion = ReglasPila.solucion(ReglasPila.inicial(carpetas))
            assertEquals(ReglasPila.minimo(carpetas), solucion.size)
            val final = solucion.fold(ReglasPila.inicial(carpetas)) { v, mov ->
                assertTrue(ReglasPila.puedeMover(v, mov))
                ReglasPila.mover(v, mov)
            }
            assertTrue(ReglasPila.resuelto(final, carpetas))
        }
    }

    @Test
    fun `la pista resuelve desde cualquier punto sin pasarse del mínimo inicial`() {
        val carpetas = 4
        var v = ReglasPila.inicial(carpetas)
        // Unos movimientos cualquiera (válidos) antes de pedir ayuda
        listOf(Movimiento(0, 1), Movimiento(0, 2), Movimiento(1, 2), Movimiento(0, 1)).forEach { v = ReglasPila.mover(v, it) }
        val solucion = ReglasPila.solucion(v)
        val final = solucion.fold(v) { estado, mov ->
            assertTrue(ReglasPila.puedeMover(estado, mov))
            ReglasPila.mover(estado, mov)
        }
        assertTrue(ReglasPila.resuelto(final, carpetas))
        assertTrue(solucion.size <= ReglasPila.minimo(carpetas))
        assertTrue(ReglasPila.solucion(final).isEmpty())
    }

    @Test
    fun `menos aperturas de más dan más estrellas en el archivo`() {
        assertEquals(3, ReglasArchivo.estrellas(0))
        assertEquals(2, ReglasArchivo.estrellas(3))
        assertEquals(1, ReglasArchivo.estrellas(10))
    }
}
