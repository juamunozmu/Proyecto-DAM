package com.unal.nachoquest.ui.screens.quiz.minijuegos

internal enum class Pie { IZQUIERDO, DERECHO }

/** Lo que pasa al tocar «Pasar el testigo». */
internal sealed interface Entrega {
    /** Todavía no se llega a la zona: no se hace nada. */
    data object FueraDeZona : Entrega

    /** El testigo pasó: el siguiente corredor sale con [velocidadSalida]. */
    data class Valida(val velocidadSalida: Float) : Entrega
}

/**
 * Los relevos (capítulo 13): un 4×100 contra el Enredo. Se corre tocando los pies en orden
 * (izquierdo, derecho...); repetir el mismo pie es un tropiezo. Al final de cada tramo hay una
 * zona de entrega de 30 m, como en el atletismo de verdad: entre más tarde se pase el testigo
 * dentro de la zona, con más impulso sale el siguiente; si se acaba la zona sin entregarlo, el
 * testigo se cae y el siguiente arranca de cero.
 *
 * Las distancias van en metros y las velocidades en metros por segundo de juego (más rápidas
 * que las reales, para que la carrera dure unos 30 segundos).
 */
internal object ReglasRelevos {

    const val TRAMOS = 4
    const val LARGO_TRAMO = 100f
    const val LARGO_ZONA = 30f
    const val INICIO_ZONA = LARGO_TRAMO - LARGO_ZONA
    const val META = TRAMOS * LARGO_TRAMO

    const val VELOCIDAD_MAXIMA = 20f
    // Cuánta velocidad suma cada paso bien dado
    const val IMPULSO = 2.5f
    // Frenado proporcional a la velocidad (por segundo): sin pasos, el corredor se detiene
    const val FRENADO = 0.8f
    // Un tropiezo deja la velocidad en esta fracción
    const val FACTOR_TROPIEZO = 0.5f
    // Segundos que se pierden al recoger el testigo caído
    const val CASTIGO_CAIDA = 1.5f

    // El Enredo corre siempre igual: termina los 400 m en 30 s
    const val VELOCIDAD_ENREDO = META / 30f
    // Segundos que pierde el Enredo cuando Nacho le amarra los cordones (la pista)
    const val CORDONES_AMARRADOS = 1.5f

    /** Velocidad tras un paso: suma si es el pie que tocaba, y frena a la mitad si repite pie. */
    fun pisar(velocidad: Float, pie: Pie, piePrevio: Pie?): Float =
        if (pie == piePrevio) velocidad * FACTOR_TROPIEZO
        else (velocidad + IMPULSO).coerceAtMost(VELOCIDAD_MAXIMA)

    /** La velocidad después de [dt] segundos sin dar pasos. */
    fun frenar(velocidad: Float, dt: Float): Float = (velocidad * (1f - FRENADO * dt)).coerceAtLeast(0f)

    /** Dónde empieza la zona en la que el [corredor] (0 a 3) entrega el testigo. */
    fun inicioZona(corredor: Int): Float = corredor * LARGO_TRAMO + INICIO_ZONA

    /** Dónde termina el tramo del [corredor]: si llega ahí con el testigo, se le cae. */
    fun finTramo(corredor: Int): Float = (corredor + 1) * LARGO_TRAMO

    /** El último corredor no entrega: corre hasta la meta. */
    fun enZona(posicion: Float, corredor: Int): Boolean =
        corredor < TRAMOS - 1 && posicion >= inicioZona(corredor) && posicion < finTramo(corredor)

    /** El testigo se cae si el corredor termina su tramo sin entregarlo. */
    fun seCayo(posicion: Float, corredor: Int): Boolean = corredor < TRAMOS - 1 && posicion >= finTramo(corredor)

    /**
     * Pasar el testigo en [posicion]. El que recibe arranca a correr cuando ve venir al
     * compañero, así que si la entrega es al final de la zona sale casi a la velocidad del que
     * llega; al principio de la zona, casi parado. El testigo sigue desde donde se entregó.
     */
    fun entregar(posicion: Float, corredor: Int, velocidad: Float): Entrega {
        if (!enZona(posicion, corredor)) return Entrega.FueraDeZona
        val dentro = (posicion - inicioZona(corredor)) / LARGO_ZONA
        return Entrega.Valida(velocidad * dentro.coerceIn(0f, 1f))
    }

    /** Metros que lleva el Enredo a los [segundos] de carrera (se le restan los segundos perdidos). */
    fun posicionEnredo(segundos: Float, segundosPerdidos: Float = 0f): Float =
        ((segundos - segundosPerdidos).coerceAtLeast(0f) * VELOCIDAD_ENREDO).coerceAtMost(META)

    /** Estrellas según las carreras que hicieron falta para ganarle al Enredo. */
    fun estrellas(intentos: Int): Int = when {
        intentos <= 1 -> 3
        intentos == 2 -> 2
        else -> 1
    }
}
