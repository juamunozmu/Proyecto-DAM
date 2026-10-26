package com.unal.nachoquest.domain.model

data class UserProfile(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val xp: Int = 0,
    val nivel: Int = 1,
    val estrellas: Int = 0,
    val retosCompletados: List<String> = emptyList(),
    val itemsDesbloqueados: List<String> = emptyList(),
    val insignias: List<String> = emptyList(),
    // Mejor puntuación (1-3 estrellas) obtenida en cada reto aprobado
    val estrellasPorReto: Map<String, Int> = emptyMap(),
    // Escenas de historia ya vistas (prólogo, intro/outro de capítulos, epílogo)
    val escenasVistas: List<String> = emptyList(),
    // Puntos de interés cuya ficha abrió el jugador en el mapa
    val lugaresDescubiertos: List<String> = emptyList()
)

// Extensión (y no propiedad) para que Firestore no la serialice como campo
fun UserProfile.primerNombre(): String =
    nombre.trim().substringBefore(" ").ifBlank { "primíparo" }
