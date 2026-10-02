package com.unal.nachoquest.domain.model

data class RetoQuiz(
    val id: String,
    val puntoInteresId: String,
    val titulo: String,
    val descripcion: String,
    val preguntas: List<Pregunta>,
    val recompensaXp: Int = 50,
    val recompensaMonedas: Int = 10
)

data class Pregunta(
    val id: String,
    val enunciado: String,
    val opciones: List<String>,
    val indiceCorrecta: Int
)