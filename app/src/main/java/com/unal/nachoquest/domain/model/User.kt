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
    val lugaresDescubiertos: List<String> = emptyList(),
    // Trivia del día: racha de días seguidos y último día jugado (días desde 1970)
    val rachaDias: Int = 0,
    val mejorRachaDias: Int = 0,
    val ultimoDiaTrivia: Long = 0,
    val triviasSuperadas: Int = 0,
    val triviasPerfectas: Int = 0,
    // Tienda: monedas, ayudas guardadas (id de TipoAyuda -> cantidad) y aspectos de Nacho
    val monedas: Int = 0,
    val ayudas: Map<String, Int> = emptyMap(),
    val aspectos: List<String> = emptyList(),
    val aspectosEquipados: List<String> = emptyList()
)

/**
 * Estrellas por reto con los retos completados que no tienen puntuación guardada.
 *
 * Antes del 25 de octubre de 2026 la app solo contaba estrellas en total y no guardaba la
 * puntuación de cada reto, así que los perfiles de esa época tienen retos completados sin
 * estrellas. Un reto aprobado vale al menos 1 estrella; al repetirlo se guarda la real.
 */
fun UserProfile.estrellasPorRetoReparadas(): Map<String, Int> =
    retosCompletados.filter { it !in estrellasPorReto }.associateWith { 1 } + estrellasPorReto

// Extensión (y no propiedad) para que Firestore no la serialice como campo
fun UserProfile.primerNombre(): String =
    nombre.trim().substringBefore(" ").ifBlank { "primíparo" }
