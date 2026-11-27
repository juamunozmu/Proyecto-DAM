package com.unal.nachoquest.ui.screens.quiz.minijuegos

import kotlin.random.Random

/**
 * El último sello (capítulo 18): el Sello Pendiente salta sobre las nueve páginas del Expediente
 * de la Memoria y les estampa «PENDIENTE». Hay que tocar cada página sellada para aprobarla antes
 * de que el sello se seque. Cada sello llega más rápido y dura menos que el anterior. Los sellos
 * que se secan sin aprobar cuentan como errores.
 */
internal object ReglasExpediente {

    const val PAGINAS = 9
    const val SELLOS = 24

    // Segundos entre un sello y el siguiente, del primero al último
    const val INTERVALO_PRIMERO = 1.3f
    const val INTERVALO_ULTIMO = 0.55f

    // Segundos que tarda en secarse cada sello
    const val DURACION_PRIMERO = 2.6f
    const val DURACION_ULTIMO = 1.6f

    private fun avance(indice: Int): Float = indice.coerceIn(0, SELLOS - 1) / (SELLOS - 1).toFloat()

    fun intervalo(indice: Int): Float = INTERVALO_PRIMERO + (INTERVALO_ULTIMO - INTERVALO_PRIMERO) * avance(indice)

    fun duracion(indice: Int): Float = DURACION_PRIMERO + (DURACION_ULTIMO - DURACION_PRIMERO) * avance(indice)

    /** Una página sin sello al azar, o null si todas están selladas. */
    fun paginaLibre(selladas: Set<Int>, random: Random = Random.Default): Int? =
        (0 until PAGINAS).filter { it !in selladas }.randomOrNull(random)

    fun estrellas(errores: Int): Int = when {
        errores <= 2 -> 3
        errores <= 6 -> 2
        else -> 1
    }
}
