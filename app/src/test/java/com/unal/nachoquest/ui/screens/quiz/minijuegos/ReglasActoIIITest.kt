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

    // ---------- Tanques (capítulo 10) ----------

    @Test
    fun `cada nivel de los tanques tiene solución y se resuelve con el camino mínimo`() {
        ReglasTanques.NIVELES.forEach { nivel ->
            val camino = ReglasTanques.caminoMinimo(nivel)
            assertTrue("$nivel no tiene solución", camino.isNotEmpty())
            val final = camino.fold(EstadoTanques()) { estado, accion -> ReglasTanques.aplicar(nivel, estado, accion) }
            assertTrue(ReglasTanques.resuelto(nivel, final))
            // El objetivo no puede ser la capacidad de un tanque: sería llenarlo y ya
            assertTrue(nivel.objetivo != nivel.capacidadA && nivel.objetivo != nivel.capacidadB)
        }
    }

    @Test
    fun `el acertijo clásico de 3 y 5 litros se resuelve en 6 movimientos`() {
        assertEquals(6, ReglasTanques.minimo(NivelTanques(3, 5, 4)))
    }

    @Test
    fun `pasar agua llena el otro tanque o vacía el primero`() {
        val nivel = NivelTanques(3, 5, 4)
        assertEquals(EstadoTanques(0, 3), ReglasTanques.aplicar(nivel, EstadoTanques(3, 0), AccionTanque.PASAR_A_B))
        assertEquals(EstadoTanques(1, 5), ReglasTanques.aplicar(nivel, EstadoTanques(3, 3), AccionTanque.PASAR_A_B))
        assertEquals(EstadoTanques(3, 2), ReglasTanques.aplicar(nivel, EstadoTanques(0, 5), AccionTanque.PASAR_B_A))
        assertEquals(EstadoTanques(0, 5), ReglasTanques.aplicar(nivel, EstadoTanques(3, 5), AccionTanque.VACIAR_A))
    }

    @Test
    fun `la pista sigue el camino mínimo desde donde vaya el jugador`() {
        val nivel = NivelTanques(3, 5, 4)
        // Desde un estado intermedio, el camino restante es más corto que el total
        val intermedio = EstadoTanques(3, 3)
        val resto = ReglasTanques.caminoMinimo(nivel, intermedio)
        assertTrue(resto.isNotEmpty())
        val final = resto.fold(intermedio) { estado, accion -> ReglasTanques.aplicar(nivel, estado, accion) }
        assertTrue(ReglasTanques.resuelto(nivel, final))
        assertTrue(ReglasTanques.caminoMinimo(nivel, EstadoTanques(4, 0)).isEmpty())
    }

    @Test
    fun `menos movimientos de más dan más estrellas en los tanques`() {
        assertEquals(3, ReglasTanques.estrellas(0))
        assertEquals(3, ReglasTanques.estrellas(2))
        assertEquals(2, ReglasTanques.estrellas(8))
        assertEquals(1, ReglasTanques.estrellas(9))
    }

    // ---------- Hélice (capítulo 11) ----------

    @Test
    fun `cada base tiene su pareja y la pareja de la pareja es ella misma`() {
        assertEquals(Base.T, ReglasHelice.complemento(Base.A))
        assertEquals(Base.A, ReglasHelice.complemento(Base.T))
        assertEquals(Base.G, ReglasHelice.complemento(Base.C))
        assertEquals(Base.C, ReglasHelice.complemento(Base.G))
        Base.entries.forEach { assertEquals(it, ReglasHelice.complemento(ReglasHelice.complemento(it))) }
        assertTrue(ReglasHelice.esPareja(Base.C, Base.G))
        assertFalse(ReglasHelice.esPareja(Base.A, Base.G))
    }

    @Test
    fun `las cadenas tienen el largo pedido y crecen en cada ronda`() {
        assertEquals(ReglasHelice.LARGOS.size, ReglasHelice.SEGUNDOS.size)
        assertEquals(ReglasHelice.LARGOS.sorted(), ReglasHelice.LARGOS)
        ReglasHelice.LARGOS.forEach { largo -> assertEquals(largo, ReglasHelice.cadena(largo, Random(largo)).size) }
    }

    @Test
    fun `menos errores dan más estrellas en la hélice`() {
        assertEquals(3, ReglasHelice.estrellas(1))
        assertEquals(2, ReglasHelice.estrellas(ReglasHelice.ERRORES_POR_DESENROLLAR + 2))
        assertEquals(1, ReglasHelice.estrellas(5))
    }
}
