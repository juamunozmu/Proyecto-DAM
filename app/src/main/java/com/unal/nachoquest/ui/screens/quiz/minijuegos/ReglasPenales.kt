package com.unal.nachoquest.ui.screens.quiz.minijuegos

import kotlin.math.abs
import kotlin.math.sin

/**
 * Reglas de la tanda de penales contra el Olvido.
 *
 * Las posiciones horizontales van de 0 (palo izquierdo) a 1 (palo derecho). La mira y el arquero
 * se mueven en vaivén; el balón va al punto donde estaba la mira al patear y es gol si, cuando
 * llega al arco, el arquero no lo alcanza.
 */
internal object ReglasPenales {

    const val GOLES_PARA_GANAR = 3
    const val DURACION_VUELO_MS = 450

    const val AMPLITUD_MIRA = 0.44f
    const val AMPLITUD_ARQUERO = 0.34f

    // Fracción del ancho del arco que cubre el arquero hacia cada lado, y radio del balón
    const val ALCANCE_ARQUERO = 0.16f
    const val RADIO_BALON = 0.04f

    // Velocidades del vaivén, en radianes por segundo; el arquero se acelera con cada gol
    private const val VELOCIDAD_MIRA = 2.9f
    private val VELOCIDADES_ARQUERO = floatArrayOf(1.8f, 2.5f, 3.3f)

    fun velocidadMira(goles: Int): Float = VELOCIDAD_MIRA + 0.35f * goles

    fun velocidadArquero(goles: Int): Float =
        VELOCIDADES_ARQUERO[goles.coerceIn(0, VELOCIDADES_ARQUERO.lastIndex)]

    /** Posición en el arco para una fase del vaivén (el centro es 0,5). */
    fun posicion(fase: Float, amplitud: Float): Float = 0.5f + amplitud * sin(fase)

    fun esGol(xBalon: Float, xArquero: Float): Boolean =
        abs(xBalon - xArquero) > ALCANCE_ARQUERO + RADIO_BALON

    /** Tres goles en cuatro tiros o menos dan las tres estrellas. */
    fun estrellas(tiros: Int): Int = when {
        tiros <= GOLES_PARA_GANAR + 1 -> 3
        tiros <= GOLES_PARA_GANAR + 3 -> 2
        else -> 1
    }
}
