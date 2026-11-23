package com.unal.nachoquest.ui.screens.quiz.minijuegos

import kotlin.random.Random

/** Dirección de un paso en el laberinto, con el bit que la representa en cada celda. */
internal enum class Paso(val bit: Int, val df: Int, val dc: Int) {
    ARRIBA(1, -1, 0),
    DERECHA(2, 0, 1),
    ABAJO(4, 1, 0),
    IZQUIERDA(8, 0, -1);

    val opuesto: Paso
        get() = when (this) {
            ARRIBA -> ABAJO
            ABAJO -> ARRIBA
            DERECHA -> IZQUIERDA
            IZQUIERDA -> DERECHA
        }
}

/**
 * Laberinto de [lado]×[lado] celdas. `abiertas[i]` guarda en bits las direcciones por las que se
 * puede salir de la celda i (sin pared). La entrada es la esquina superior izquierda y la salida,
 * la inferior derecha.
 */
internal class Laberinto(val lado: Int, private val abiertas: IntArray) {

    val entrada: Int get() = 0
    val salida: Int get() = lado * lado - 1

    fun fila(celda: Int) = celda / lado
    fun columna(celda: Int) = celda % lado

    fun puedeIr(celda: Int, paso: Paso): Boolean = abiertas[celda] and paso.bit != 0

    /** Celda a la que se llega dando [paso], o null si hay pared. */
    fun mover(celda: Int, paso: Paso): Int? =
        if (puedeIr(celda, paso)) (fila(celda) + paso.df) * lado + columna(celda) + paso.dc else null

    /** Camino más corto (celdas, sin incluir [desde]) hasta la salida. */
    fun caminoASalida(desde: Int): List<Int> {
        val anterior = IntArray(lado * lado) { -1 }
        val visto = BooleanArray(lado * lado)
        val cola = ArrayDeque(listOf(desde))
        visto[desde] = true
        while (cola.isNotEmpty()) {
            val actual = cola.removeFirst()
            if (actual == salida) break
            for (paso in Paso.entries) {
                val siguiente = mover(actual, paso) ?: continue
                if (visto[siguiente]) continue
                visto[siguiente] = true
                anterior[siguiente] = actual
                cola.addLast(siguiente)
            }
        }
        val camino = mutableListOf<Int>()
        var celda = salida
        while (celda != desde && celda != -1) {
            camino.add(celda)
            celda = anterior[celda]
        }
        return camino.reversed()
    }
}

internal object ReglasLaberinto {

    const val LADO = 9

    /** Distancia (en celdas, contando diagonales) hasta la que Nacho ve a través de la bruma. */
    const val ALCANCE_VISTA = 1

    /** Pasos del camino que muestra cada pista (el sonido del cencerro). */
    const val PASOS_POR_PISTA = 6

    /**
     * Laberinto perfecto (todas las celdas conectadas y un único camino entre dos celdas),
     * generado con búsqueda en profundidad desde la entrada.
     */
    fun generar(lado: Int = LADO, random: Random = Random.Default): Laberinto {
        val abiertas = IntArray(lado * lado)
        val visitada = BooleanArray(lado * lado)
        val pila = ArrayDeque(listOf(0))
        visitada[0] = true
        while (pila.isNotEmpty()) {
            val actual = pila.last()
            val f = actual / lado
            val c = actual % lado
            val opciones = Paso.entries.filter { paso ->
                val nf = f + paso.df
                val nc = c + paso.dc
                nf in 0 until lado && nc in 0 until lado && !visitada[nf * lado + nc]
            }
            if (opciones.isEmpty()) {
                pila.removeLast()
                continue
            }
            val paso = opciones.random(random)
            val siguiente = (f + paso.df) * lado + c + paso.dc
            abiertas[actual] = abiertas[actual] or paso.bit
            abiertas[siguiente] = abiertas[siguiente] or paso.opuesto.bit
            visitada[siguiente] = true
            pila.addLast(siguiente)
        }
        return Laberinto(lado, abiertas)
    }

    /** Estrellas según los pasos que dio el jugador frente al camino más corto. */
    fun estrellas(pasos: Int, caminoMinimo: Int): Int = when {
        pasos <= caminoMinimo + 6 -> 3
        pasos <= caminoMinimo + 20 -> 2
        else -> 1
    }
}
