package com.unal.nachoquest.ui.screens.quiz.minijuegos

import kotlin.random.Random

/**
 * Una diferencia entre el cuadro original y la copia del Eco. La zona donde se puede tocar va en
 * fracciones del ancho y del alto del cuadro (0 a 1).
 */
internal data class Diferencia(
    val id: Int,
    val descripcion: String,
    val x: Float,
    val y: Float,
    val ancho: Float,
    val alto: Float
) {
    val centroX: Float get() = x + ancho / 2
    val centroY: Float get() = y + alto / 2

    fun contiene(px: Float, py: Float, margen: Float): Boolean =
        px in (x - margen)..(x + ancho + margen) && py in (y - margen)..(y + alto + margen)
}

internal object ReglasDiferencias {

    const val DIFERENCIAS_POR_PARTIDA = 5

    // Margen extra alrededor de cada zona, para que no haga falta tocar con precisión de píxel
    private const val MARGEN = 0.03f

    // Ids que usa el dibujo del cuadro para saber qué cambiar en la copia
    const val SOL = 0
    const val TECHO = 1
    const val VENTANA = 2
    const val FRUTAS = 3
    const val NUBE = 4
    const val PAJARO = 5
    const val FLOR = 6
    const val PUERTA = 7

    val TODAS = listOf(
        Diferencia(SOL, "El sol desapareció", 0.06f, 0.05f, 0.2f, 0.22f),
        Diferencia(TECHO, "El techo cambió de color", 0.5f, 0.36f, 0.34f, 0.15f),
        Diferencia(VENTANA, "Falta la ventana", 0.57f, 0.53f, 0.1f, 0.1f),
        Diferencia(FRUTAS, "El árbol perdió sus frutas", 0.08f, 0.34f, 0.26f, 0.24f),
        Diferencia(NUBE, "Apareció una nube de más", 0.54f, 0.06f, 0.24f, 0.13f),
        Diferencia(PAJARO, "Se fue el pájaro", 0.36f, 0.13f, 0.13f, 0.09f),
        Diferencia(FLOR, "Una flor cambió de color", 0.84f, 0.82f, 0.1f, 0.12f),
        Diferencia(PUERTA, "La puerta cambió de color", 0.7f, 0.58f, 0.09f, 0.16f)
    )

    fun elegir(random: Random = Random.Default): List<Diferencia> =
        TODAS.shuffled(random).take(DIFERENCIAS_POR_PARTIDA)

    /** Diferencia pendiente que contiene el toque (px, py), o null si el jugador falló. */
    fun tocar(px: Float, py: Float, pendientes: Collection<Diferencia>): Diferencia? =
        pendientes.firstOrNull { it.contiene(px, py, MARGEN) }

    /** Estrellas según los toques en lugares donde no había nada distinto. */
    fun estrellas(errores: Int): Int = when {
        errores <= 1 -> 3
        errores <= 4 -> 2
        else -> 1
    }
}
