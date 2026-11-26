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

    // ---------- Vitral (capítulo 12) ----------

    @Test
    fun `tocar un vidrio cambia él y sus vecinos, y tocarlo otra vez lo deja igual`() {
        val lado = 3
        val encendido = ReglasVitral.encendido(lado)
        // El del centro cambia cinco vidrios: él y los cuatro de al lado
        val centro = ReglasVitral.tocar(encendido, lado, 4)
        assertEquals(listOf(1, 3, 4, 5, 7), centro.indices.filter { !centro[it] })
        // La esquina solo tiene dos vecinos
        val esquina = ReglasVitral.tocar(encendido, lado, 0)
        assertEquals(listOf(0, 1, 3), esquina.indices.filter { !esquina[it] })
        // El borde no da la vuelta a la fila siguiente
        val borde = ReglasVitral.tocar(encendido, lado, 2)
        assertEquals(listOf(1, 2, 5), borde.indices.filter { !borde[it] })
        assertEquals(encendido, ReglasVitral.tocar(centro, lado, 4))
    }

    @Test
    fun `cada vitral empieza roto y su solución mínima lo enciende`() {
        ReglasVitral.NIVELES.forEach { nivel ->
            val inicial = ReglasVitral.inicial(nivel)
            assertFalse("El vitral de ${nivel.lado} empieza encendido", ReglasVitral.resuelto(inicial))
            val solucion = ReglasVitral.solucionMinima(inicial, nivel.lado)
            val final = solucion.fold(inicial) { vitral, vidrio -> ReglasVitral.tocar(vitral, nivel.lado, vidrio) }
            assertTrue("La solución del vitral de ${nivel.lado} no lo enciende", ReglasVitral.resuelto(final))
            // Nunca hace falta más toques que los que lo rompieron
            assertTrue(solucion.size <= nivel.toquesQueLoRompen.size)
        }
    }

    @Test
    fun `los vitrales crecen y el primero se arregla con dos toques`() {
        val lados = ReglasVitral.NIVELES.map { it.lado }
        assertEquals(lados.sorted(), lados)
        // En 3×3 la solución es única: hay que tocar las dos esquinas que lo rompieron
        assertEquals(2, ReglasVitral.minimo(ReglasVitral.NIVELES.first()))
        assertEquals(listOf(0, 8), ReglasVitral.solucionMinima(ReglasVitral.inicial(ReglasVitral.NIVELES.first()), 3).sorted())
    }

    @Test
    fun `un vitral encendido no necesita toques`() {
        assertTrue(ReglasVitral.solucionMinima(ReglasVitral.encendido(4), 4).isEmpty())
    }

    // ---------- Relevos (capítulo 13) ----------

    /**
     * Corre los 400 m dando [pasosPorSegundo] pasos alternados y entregando el testigo cuando
     * va [entregaEn] metros dentro de la zona (null: nunca entrega). Devuelve los segundos.
     */
    private fun simularCarrera(pasosPorSegundo: Float, entregaEn: Float?): Float {
        val dt = 0.01f
        var t = 0f
        var posicion = 0f
        var velocidad = 0f
        var corredor = 0
        var pie: Pie? = null
        var hastaElPaso = 0f
        var recogiendo = 0f
        while (posicion < ReglasRelevos.META && t < 120f) {
            t += dt
            if (recogiendo > 0f) {
                recogiendo -= dt
                continue
            }
            hastaElPaso -= dt
            if (hastaElPaso <= 0f) {
                val siguiente = if (pie == Pie.IZQUIERDO) Pie.DERECHO else Pie.IZQUIERDO
                velocidad = ReglasRelevos.pisar(velocidad, siguiente, pie)
                pie = siguiente
                hastaElPaso += 1f / pasosPorSegundo
            }
            velocidad = ReglasRelevos.frenar(velocidad, dt)
            posicion += velocidad * dt
            if (entregaEn != null && posicion >= ReglasRelevos.inicioZona(corredor) + entregaEn) {
                val entrega = ReglasRelevos.entregar(posicion, corredor, velocidad)
                if (entrega is Entrega.Valida) {
                    corredor++
                    velocidad = entrega.velocidadSalida
                    pie = null
                }
            }
            if (ReglasRelevos.seCayo(posicion, corredor)) {
                corredor++
                velocidad = 0f
                pie = null
                recogiendo = ReglasRelevos.CASTIGO_CAIDA
            }
        }
        return t
    }

    private val tiempoEnredo = ReglasRelevos.META / ReglasRelevos.VELOCIDAD_ENREDO

    @Test
    fun `alternar los pies acelera y repetir pie es un tropiezo`() {
        val v = ReglasRelevos.pisar(0f, Pie.IZQUIERDO, null)
        assertEquals(ReglasRelevos.IMPULSO, v, 0.001f)
        assertEquals(2 * ReglasRelevos.IMPULSO, ReglasRelevos.pisar(v, Pie.DERECHO, Pie.IZQUIERDO), 0.001f)
        assertEquals(v * ReglasRelevos.FACTOR_TROPIEZO, ReglasRelevos.pisar(v, Pie.IZQUIERDO, Pie.IZQUIERDO), 0.001f)
        assertEquals(ReglasRelevos.VELOCIDAD_MAXIMA, ReglasRelevos.pisar(ReglasRelevos.VELOCIDAD_MAXIMA, Pie.DERECHO, Pie.IZQUIERDO), 0.001f)
        assertTrue(ReglasRelevos.frenar(10f, 0.1f) < 10f)
        assertEquals(0f, ReglasRelevos.frenar(0f, 0.1f), 0.001f)
    }

    @Test
    fun `el testigo solo se entrega en la zona y al final sale con más impulso`() {
        assertEquals(Entrega.FueraDeZona, ReglasRelevos.entregar(50f, 0, 15f))
        // La zona del segundo corredor no sirve para el primero
        assertFalse(ReglasRelevos.enZona(175f, 0))
        val temprano = ReglasRelevos.entregar(72f, 0, 15f) as Entrega.Valida
        val tarde = ReglasRelevos.entregar(98f, 0, 15f) as Entrega.Valida
        assertTrue(tarde.velocidadSalida > temprano.velocidadSalida)
        assertTrue(tarde.velocidadSalida <= 15f)
        // El último corredor no entrega: corre hasta la meta
        assertEquals(Entrega.FueraDeZona, ReglasRelevos.entregar(380f, 3, 15f))
        assertFalse(ReglasRelevos.seCayo(399f, 3))
        assertTrue(ReglasRelevos.seCayo(100f, 0))
        assertFalse(ReglasRelevos.seCayo(100f, 1))
    }

    @Test
    fun `quien corre bien le gana al Enredo y quien corre lento no`() {
        // Seis pasos por segundo y entregas al final de la zona: gana
        assertTrue(simularCarrera(6f, entregaEn = 27f) < tiempoEnredo)
        // Tres pasos por segundo no alcanzan
        assertTrue(simularCarrera(3f, entregaEn = 27f) > tiempoEnredo)
        // Dejar caer el testigo cuesta más que entregarlo al final de la zona
        assertTrue(simularCarrera(6f, entregaEn = null) > simularCarrera(6f, entregaEn = 27f))
    }

    @Test
    fun `la pista le resta tiempo al Enredo y menos carreras dan más estrellas`() {
        assertTrue(ReglasRelevos.posicionEnredo(10f, ReglasRelevos.CORDONES_AMARRADOS) < ReglasRelevos.posicionEnredo(10f))
        assertEquals(0f, ReglasRelevos.posicionEnredo(1f, ReglasRelevos.CORDONES_AMARRADOS), 0.001f)
        assertEquals(ReglasRelevos.META, ReglasRelevos.posicionEnredo(1000f), 0.001f)
        assertEquals(3, ReglasRelevos.estrellas(1))
        assertEquals(2, ReglasRelevos.estrellas(2))
        assertEquals(1, ReglasRelevos.estrellas(5))
    }

    @Test
    fun `menos toques de más dan más estrellas en el vitral`() {
        assertEquals(3, ReglasVitral.estrellas(0))
        assertEquals(2, ReglasVitral.estrellas(5))
        assertEquals(1, ReglasVitral.estrellas(20))
    }
}
