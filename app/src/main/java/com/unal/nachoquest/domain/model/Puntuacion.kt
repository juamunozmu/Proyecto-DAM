package com.unal.nachoquest.domain.model

/** Resultado final de un reto: si se aprobó, las estrellas (0-3) y la XP ganada. */
data class ResultadoReto(
    val aprobado: Boolean,
    val estrellas: Int,
    val xp: Int
)

/**
 * Reglas de puntuación de los retos (minijuego del capítulo + preguntas del guardián).
 */
object Puntuacion {

    const val XP_POR_ESTRELLA_MINIJUEGO = 10
    const val BONO_PERFECTO = 25

    /**
     * @param estrellasMinijuego estrellas del minijuego, o null en los retos que no tienen
     * @param bonoVelocidad XP extra acumulada por responder rápido
     */
    fun calcular(
        totalPreguntas: Int,
        respuestasCorrectas: Int,
        vidasRestantes: Int,
        estrellasMinijuego: Int?,
        bonoVelocidad: Int,
        recompensaXp: Int,
        esRepeticion: Boolean
    ): ResultadoReto {
        // Se aprueba si se termina con al menos un corazón
        val aprobado = vidasRestantes > 0
        val errores = totalPreguntas - respuestasCorrectas
        val estrellasPreguntas = (3 - errores).coerceIn(1, 3)
        // En los capítulos cuenta lo peor entre el minijuego y las preguntas
        val estrellas = if (!aprobado) 0 else minOf(estrellasPreguntas, estrellasMinijuego ?: 3)
        val perfecto = errores == 0 && (estrellasMinijuego ?: 3) == 3
        val bonoMinijuego = (estrellasMinijuego ?: 0) * XP_POR_ESTRELLA_MINIJUEGO

        // La XP solo se gana la primera vez que se aprueba; repetir sirve para mejorar estrellas
        val xp = when {
            !aprobado || esRepeticion -> 0
            else -> recompensaXp + bonoVelocidad + bonoMinijuego + if (perfecto) BONO_PERFECTO else 0
        }
        return ResultadoReto(aprobado = aprobado, estrellas = estrellas, xp = xp)
    }
}
