package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/** Las cuatro bases del ADN, con el color con que se dibujan. */
internal enum class Base(val color: Color) {
    A(Color(0xFFE53935)),
    T(Color(0xFFFDD835)),
    C(Color(0xFF1E88E5)),
    G(Color(0xFF43A047))
}

/**
 * Completa la hélice (capítulo 11): el Enredo dejó solo un lado del ADN. Hay que ponerle a cada
 * base su pareja (A con T, C con G) antes de que se acabe el tiempo y la hélice se desenrolle.
 */
internal object ReglasHelice {

    /** Bases de cada cadena, y segundos que hay para completarla. */
    val LARGOS = listOf(6, 8, 10)
    val SEGUNDOS = listOf(25, 28, 32)

    /** Errores que suma que una cadena se desenrolle por tiempo (y se vuelve a empezar). */
    const val ERRORES_POR_DESENROLLAR = 2

    fun complemento(base: Base): Base = when (base) {
        Base.A -> Base.T
        Base.T -> Base.A
        Base.C -> Base.G
        Base.G -> Base.C
    }

    fun cadena(largo: Int, random: Random = Random.Default): List<Base> =
        List(largo) { Base.entries.random(random) }

    fun esPareja(base: Base, elegida: Base): Boolean = complemento(base) == elegida

    fun estrellas(errores: Int): Int = when {
        errores <= 1 -> 3
        errores <= 4 -> 2
        else -> 1
    }
}
