package com.unal.nachoquest.ui.screens.quiz.minijuegos

import kotlin.random.Random

internal data class Animal(val emoji: String, val nombre: String)

/** Reglas de las parejas del potrero: 8 animales, cada uno repetido dos veces. */
internal object ReglasParejas {

    val ANIMALES = listOf(
        Animal("🐄", "Vaca"),
        Animal("🐎", "Caballo"),
        Animal("🐑", "Oveja"),
        Animal("🐖", "Cerdo"),
        Animal("🐔", "Gallina"),
        Animal("🐕", "Perro"),
        Animal("🐈", "Gato"),
        Animal("🐐", "Cabra")
    )

    /** Mazo mezclado: cada posición guarda el índice del animal en [ANIMALES]. */
    fun mazo(random: Random = Random.Default): List<Int> =
        (ANIMALES.indices + ANIMALES.indices).shuffled(random)

    /** Estrellas según los intentos fallidos (dos cartas volteadas que no eran pareja). */
    fun estrellas(fallos: Int): Int = when {
        fallos <= 4 -> 3
        fallos <= 9 -> 2
        else -> 1
    }
}
