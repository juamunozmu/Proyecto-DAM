package com.unal.nachoquest.domain.model

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
    // Lista de coordenadas (Lat, Lon) que forman el polígono de la zona
    val zonaPoligono: List<Pair<Double, Double>>? = null,
    val tieneRetoDisponible: Boolean = false,
    val visitado: Boolean = false
)