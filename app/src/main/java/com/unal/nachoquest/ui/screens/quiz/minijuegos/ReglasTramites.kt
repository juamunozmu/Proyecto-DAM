package com.unal.nachoquest.ui.screens.quiz.minijuegos

import kotlin.random.Random

/** Los papeles que llegan a la ventanilla, cada uno con la bandeja que le corresponde. */
internal enum class TipoTramite(val emoji: String, val nombre: String, val bandeja: String) {
    CARNE("🪪", "Carné", "Carnés"),
    RECIBO("🧾", "Recibo de matrícula", "Recibos"),
    CERTIFICADO("📜", "Certificado de notas", "Certificados"),
    SOLICITUD("📝", "Solicitud", "Solicitudes")
}

/**
 * Cada papel a su bandeja (capítulo 14): los trámites del Edificio Uriel Gutiérrez llegan uno a
 * uno y hay que dejarlos en su bandeja antes de que el Sello Pendiente les estampe
 * «PENDIENTE». Cada papel da un poco menos de tiempo que el anterior. Equivocarse de bandeja o
 * dejar que se acabe el tiempo cuenta como error.
 */
internal object ReglasTramites {

    const val PAPELES = 20
    const val SEGUNDOS_PRIMERO = 3.2f
    const val SEGUNDOS_ULTIMO = 1.4f

    /** Los papeles de la partida: la misma cantidad de cada tipo, revueltos. */
    fun papeles(cantidad: Int = PAPELES, random: Random = Random.Default): List<TipoTramite> =
        List(cantidad) { TipoTramite.entries[it % TipoTramite.entries.size] }.shuffled(random)

    /** Segundos para el papel número [indice] (desde 0): bajan de forma pareja hasta el último. */
    fun segundosPara(indice: Int, cantidad: Int = PAPELES): Float {
        if (cantidad <= 1) return SEGUNDOS_PRIMERO
        val avance = indice.coerceIn(0, cantidad - 1) / (cantidad - 1).toFloat()
        return SEGUNDOS_PRIMERO + (SEGUNDOS_ULTIMO - SEGUNDOS_PRIMERO) * avance
    }

    fun esCorrecta(papel: TipoTramite, bandeja: TipoTramite): Boolean = papel == bandeja

    fun estrellas(errores: Int): Int = when {
        errores <= 1 -> 3
        errores <= 4 -> 2
        else -> 1
    }
}
