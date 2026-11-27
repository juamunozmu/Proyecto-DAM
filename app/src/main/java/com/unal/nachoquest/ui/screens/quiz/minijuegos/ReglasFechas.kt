package com.unal.nachoquest.ui.screens.quiz.minijuegos

/** Un titular de periódico y el año en que salió la noticia. */
internal data class Titular(val texto: String, val anio: Int)

/**
 * Fechas en trámite (capítulo 16): en la Hemeroteca, el Sello tapó con «PENDIENTE» la fecha de
 * los periódicos. Hay que devolverle a cada titular su año. Son tres rondas de cuatro titulares
 * con años distintos. Todos los años ya están verificados en las preguntas de la historia.
 */
internal object ReglasFechas {

    val RONDAS: List<List<Titular>> = listOf(
        // El campus
        listOf(
            Titular("Se funda la Universidad Nacional de Colombia", 1867),
            Titular("El Estadio Alfonso López abre con los primeros Juegos Bolivarianos", 1938),
            Titular("Se inaugura la Capilla Cristo Maestro", 1953),
            Titular("La Escuela de Cine y Televisión estrena casa en el edificio de Artes", 2023)
        ),
        // Colombia
        listOf(
            Titular("El Bogotazo: asesinan a Jorge Eliécer Gaitán", 1948),
            Titular("Muere el estudiante Uriel Gutiérrez; su nombre queda en la sede administrativa", 1954),
            Titular("Carlos Lleras Restrepo llega a la presidencia", 1966),
            Titular("Colombia estrena una nueva Constitución", 1991)
        ),
        // La ciencia y el mundo
        listOf(
            Titular("Leopoldo Rother empieza a trazar el plano de la Ciudad Universitaria", 1936),
            Titular("Watson y Crick publican la doble hélice del ADN", 1953),
            Titular("Nace la carrera de Sociología en la Universidad Nacional", 1959),
            Titular("El ser humano llega a la Luna", 1969)
        )
    )

    fun esCorrecta(titular: Titular, anio: Int): Boolean = titular.anio == anio

    fun estrellas(errores: Int): Int = when {
        errores <= 1 -> 3
        errores <= 4 -> 2
        else -> 1
    }
}
