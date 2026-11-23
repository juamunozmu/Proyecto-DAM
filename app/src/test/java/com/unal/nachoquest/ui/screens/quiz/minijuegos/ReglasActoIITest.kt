package com.unal.nachoquest.ui.screens.quiz.minijuegos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Reglas de los minijuegos del Acto II: laberinto, parejas y diferencias. */
class ReglasActoIITest {

    @Test
    fun `el laberinto conecta todas las celdas y siempre tiene salida`() {
        repeat(20) { semilla ->
            val laberinto = ReglasLaberinto.generar(random = Random(semilla))
            val total = laberinto.lado * laberinto.lado
            // Recorrido desde la entrada: debe alcanzar todas las celdas
            val visto = BooleanArray(total)
            val pila = ArrayDeque(listOf(laberinto.entrada))
            var aristas = 0
            while (pila.isNotEmpty()) {
                val celda = pila.removeLast()
                if (visto[celda]) continue
                visto[celda] = true
                Paso.entries.forEach { paso ->
                    laberinto.mover(celda, paso)?.let { aristas++; pila.addLast(it) }
                }
            }
            assertTrue("Semilla $semilla: hay celdas aisladas", visto.all { it })
            // Laberinto perfecto: un árbol tiene exactamente total - 1 pasillos (cada uno contado dos veces)
            assertEquals(2 * (total - 1), aristas)

            val camino = laberinto.caminoASalida(laberinto.entrada)
            assertEquals(laberinto.salida, camino.last())
            // Cada paso del camino es un movimiento válido
            (listOf(laberinto.entrada) + camino).zipWithNext().forEach { (a, b) ->
                assertTrue(Paso.entries.any { laberinto.mover(a, it) == b })
            }
        }
    }

    @Test
    fun `las paredes del laberinto no dejan salir del tablero`() {
        val laberinto = ReglasLaberinto.generar(random = Random(7))
        val lado = laberinto.lado
        for (i in 0 until lado) {
            assertFalse(laberinto.puedeIr(i, Paso.ARRIBA))
            assertFalse(laberinto.puedeIr(lado * (lado - 1) + i, Paso.ABAJO))
            assertFalse(laberinto.puedeIr(i * lado, Paso.IZQUIERDA))
            assertFalse(laberinto.puedeIr(i * lado + lado - 1, Paso.DERECHA))
        }
    }

    @Test
    fun `menos pasos de más dan más estrellas en el laberinto`() {
        assertEquals(3, ReglasLaberinto.estrellas(pasos = 20, caminoMinimo = 20))
        assertEquals(2, ReglasLaberinto.estrellas(pasos = 35, caminoMinimo = 20))
        assertEquals(1, ReglasLaberinto.estrellas(pasos = 60, caminoMinimo = 20))
    }

    @Test
    fun `el mazo de parejas tiene cada animal exactamente dos veces`() {
        val mazo = ReglasParejas.mazo(Random(3))
        assertEquals(ReglasParejas.ANIMALES.size * 2, mazo.size)
        mazo.groupingBy { it }.eachCount().values.forEach { assertEquals(2, it) }
        assertEquals(3, ReglasParejas.estrellas(0))
        assertEquals(2, ReglasParejas.estrellas(7))
        assertEquals(1, ReglasParejas.estrellas(15))
    }

    @Test
    fun `las zonas de las diferencias caben en el cuadro y no se pisan`() {
        val todas = ReglasDiferencias.TODAS
        assertEquals(todas.size, todas.map { it.id }.toSet().size)
        todas.forEach { d ->
            assertTrue("${d.id} se sale del cuadro", d.x >= 0f && d.y >= 0f && d.x + d.ancho <= 1f && d.y + d.alto <= 1f)
        }
        // Ningún centro cae dentro de la zona de otra diferencia
        todas.forEach { a ->
            todas.filter { it != a }.forEach { b -> assertFalse("${a.id} pisa a ${b.id}", b.contiene(a.centroX, a.centroY, 0f)) }
        }
        assertEquals(ReglasDiferencias.DIFERENCIAS_POR_PARTIDA, ReglasDiferencias.elegir(Random(1)).size)
    }

    @Test
    fun `tocar una diferencia la encuentra y tocar el cielo vacío no`() {
        val sol = ReglasDiferencias.TODAS.first { it.id == ReglasDiferencias.SOL }
        assertNotNull(ReglasDiferencias.tocar(sol.centroX, sol.centroY, listOf(sol)))
        assertNull(ReglasDiferencias.tocar(0.5f, 0.3f, ReglasDiferencias.TODAS))
        assertEquals(3, ReglasDiferencias.estrellas(1))
        assertEquals(1, ReglasDiferencias.estrellas(8))
    }
}
