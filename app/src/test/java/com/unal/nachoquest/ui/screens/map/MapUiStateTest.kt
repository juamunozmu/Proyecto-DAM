package com.unal.nachoquest.ui.screens.map

import com.unal.nachoquest.domain.model.PointOfInterest
import org.junit.Assert.assertEquals
import org.junit.Test

class MapUiStateTest {

    // Biblioteca Central: capítulo 2, se abre con el Carné Histórico
    private val biblioteca = PointOfInterest(
        id = "edif_102",
        nombre = "Biblioteca Central",
        edificio = "102",
        descripcion = "",
        latitud = 4.6352789,
        longitud = -74.0832676,
        tieneRetoDisponible = true,
        capituloHistoria = 2,
        itemRequeridoId = "carnet_antiguo"
    )

    // Un punto a ~0,0005° de latitud (≈ 55 m) y otro a ~0,002° (≈ 220 m)
    private val cerca = (biblioteca.latitud + 0.0005) to biblioteca.longitud
    private val lejos = (biblioteca.latitud + 0.002) to biblioteca.longitud

    @Test
    fun `sin el ítem de la historia el capítulo está bloqueado`() {
        val estado = MapUiState(ubicacionUsuario = cerca)
        assertEquals(EstadoEdificioReto.BLOQUEADO_HISTORIA, estado.obtenerEstadoReto(biblioteca))
    }

    @Test
    fun `con el ítem hay que acercarse a menos de 75 metros`() {
        val conCarne = MapUiState(itemsUsuario = setOf("carnet_antiguo"))
        assertEquals(EstadoEdificioReto.FUERA_DE_RANGO, conCarne.obtenerEstadoReto(biblioteca))
        assertEquals(EstadoEdificioReto.FUERA_DE_RANGO, conCarne.copy(ubicacionUsuario = lejos).obtenerEstadoReto(biblioteca))
        assertEquals(EstadoEdificioReto.LISTO_PARA_JUGAR, conCarne.copy(ubicacionUsuario = cerca).obtenerEstadoReto(biblioteca))
    }

    @Test
    fun `el modo de pruebas permite jugar desde cualquier lugar`() {
        val estado = MapUiState(itemsUsuario = setOf("carnet_antiguo"), modoSimulacionGps = true)
        assertEquals(EstadoEdificioReto.LISTO_PARA_JUGAR, estado.obtenerEstadoReto(biblioteca))
    }

    @Test
    fun `un capítulo superado queda como completado`() {
        val estado = MapUiState(retosCompletados = setOf("reto_biblio"))
        assertEquals(EstadoEdificioReto.YA_COMPLETADO, estado.obtenerEstadoReto(biblioteca))
    }

    @Test
    fun `los edificios informativos no tienen reto`() {
        val cafeteria = biblioteca.copy(id = "edif_cafe", tieneRetoDisponible = false, capituloHistoria = null, itemRequeridoId = null)
        assertEquals(EstadoEdificioReto.SIN_RETO, MapUiState().obtenerEstadoReto(cafeteria))
    }

    @Test
    fun `la distancia usa la fórmula de haversine`() {
        // Un milésimo de grado de latitud son unos 111 metros
        assertEquals(111.19, distanciaMetros(4.635, -74.083, 4.636, -74.083), 0.05)
        assertEquals(0.0, distanciaMetros(4.635, -74.083, 4.635, -74.083), 0.0)
    }

    @Test
    fun `las distancias se muestran en metros o kilómetros`() {
        assertEquals("250 m", formatearDistancia(250.7))
        assertEquals("1,5 km", formatearDistancia(1500.0))
    }
}
