package com.unal.nachoquest.ui.screens.quiz.minijuegos

import kotlin.random.Random

/**
 * Las películas que el Enredo cortó. Cada una avanza de un fotograma al siguiente con una pista
 * visual clara (el sol sube, la planta crece, el balón se acerca al arco), así que el orden se
 * puede deducir mirando con cuidado.
 */
internal enum class PeliculaMontaje(val titulo: String, val fotogramas: Int) {
    AMANECER("Amanece en el campus", 4),
    SEMILLA("La semilla del jardín", 5),
    GOL("El gol del estadio", 6)
}

/**
 * Reglas del montaje (capítulo 9): los fotogramas de cada película aparecen revueltos y hay que
 * tocarlos en orden. Tocar uno que no sigue cuenta como error.
 */
internal object ReglasMontaje {

    val PELICULAS: List<PeliculaMontaje> = PeliculaMontaje.entries

    /**
     * Orden en que se muestran los fotogramas por colocar. Nunca sale ya ordenado: si el azar
     * lo deja en orden, se vuelve a mezclar.
     */
    fun revolver(fotogramas: Int, random: Random = Random.Default): List<Int> {
        require(fotogramas >= 2) { "Una película necesita al menos dos fotogramas" }
        val ordenado = (0 until fotogramas).toList()
        var revuelto: List<Int>
        do {
            revuelto = ordenado.shuffled(random)
        } while (revuelto == ordenado)
        return revuelto
    }

    /** El fotograma tocado es el que sigue en la línea de tiempo. */
    fun esElSiguiente(fotograma: Int, colocados: Int): Boolean = fotograma == colocados

    /** Estrellas según los errores en las tres películas juntas. */
    fun estrellas(errores: Int): Int = when {
        errores <= 1 -> 3
        errores <= 4 -> 2
        else -> 1
    }
}
