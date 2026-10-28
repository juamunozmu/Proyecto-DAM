package com.unal.nachoquest.domain.model

/**
 * Tipo de lugar del campus. Define el icono del pin y si aparece en el mapa
 * (los edificios internos solo se encuentran con el buscador).
 */
enum class CategoriaLugar(
    val etiqueta: String,
    val emoji: String,
    val colorArgb: Int,
    val visibleEnMapa: Boolean = true
) {
    ACADEMICO("Académico", "🎓", 0xFF546E7A.toInt()),
    LABORATORIO("Laboratorio", "🧪", 0xFF00897B.toInt()),
    BIBLIOTECA("Biblioteca", "📚", 0xFF6D4C41.toInt()),
    CULTURA("Cultura", "🎭", 0xFF8E24AA.toInt()),
    DEPORTE("Deporte", "⚽", 0xFF1E88E5.toInt()),
    COMIDA("Comida", "☕", 0xFFEF6C00.toInt()),
    SERVICIOS("Servicios", "🏦", 0xFF3949AB.toInt()),
    PLAZA("Plaza", "🌳", 0xFF2E7D32.toInt()),
    INTERNO("Interno", "🏗️", 0xFF9E9E9E.toInt(), visibleEnMapa = false);

    companion object {
        fun desde(valor: String?): CategoriaLugar =
            entries.find { it.name == valor } ?: ACADEMICO

        /** Categorías que se ofrecen como filtro rápido en el mapa. */
        val filtros = listOf(COMIDA, BIBLIOTECA, CULTURA, DEPORTE, SERVICIOS, LABORATORIO)
    }
}

/**
 * Modelo de dominio que representa un punto de interés en el campus UNAL.
 */
data class PointOfInterest(
    val id: String,
    val nombre: String,
    val edificio: String,
    val campus: String = "Campus Bogotá",
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val categoria: CategoriaLugar = CategoriaLugar.ACADEMICO,
    // Lista de coordenadas (Lat, Lon) que forman el polígono de la zona
    val zonaPoligono: List<Pair<Double, Double>>? = null,
    val tieneRetoDisponible: Boolean = false,
    val visitado: Boolean = false,
    val alias: List<String> = emptyList(),
    val imagenUrl: String? = null,
    val capituloHistoria: Int? = null,
    val itemRequeridoId: String? = null
)
