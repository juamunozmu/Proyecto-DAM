package com.unal.nachoquest.domain.model

data class UserProfile(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val xp: Int = 0,
    val nivel: Int = 1,
    val estrellas: Int = 0,
    val retosCompletados: List<String> = emptyList()
)