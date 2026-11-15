package com.unal.nachoquest.domain.model

/** Fila de la tabla de posiciones: solo los datos públicos de un jugador. */
data class JugadorRanking(
    val nombre: String,
    val xp: Int,
    val nivel: Int,
    val estrellas: Int,
    val esJugadorActual: Boolean = false
)
