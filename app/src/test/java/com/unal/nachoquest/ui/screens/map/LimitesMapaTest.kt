package com.unal.nachoquest.ui.screens.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LimitesMapaTest {

    @Test
    fun `con el zoom mínimo el campus llena justo el ancho de la pantalla`() {
        val ancho = 1080
        val zoom = LimitesMapa.zoomMinimo(ancho)
        // Ancho del mundo en píxeles a ese zoom, y la parte que ocupa el área del campus
        val mundo = 256 * Math.pow(2.0, zoom)
        val anchoArea = mundo * LimitesMapa.ANCHO_VISIBLE_MINIMO / 360.0
        assertEquals(ancho.toDouble(), anchoArea, 1.0)
    }

    @Test
    fun `en una pantalla más ancha el zoom mínimo es mayor`() {
        assertTrue(LimitesMapa.zoomMinimo(1440) > LimitesMapa.zoomMinimo(720))
    }

    @Test
    fun `sin medida de pantalla se usa el zoom por defecto`() {
        assertEquals(LimitesMapa.ZOOM_MINIMO_POR_DEFECTO, LimitesMapa.zoomMinimo(0), 0.0)
    }

    @Test
    fun `el centro está dentro del área`() {
        assertTrue(LimitesMapa.contiene(LimitesMapa.CENTRO_LAT, LimitesMapa.CENTRO_LON))
    }

    @Test
    fun `con el zoom mínimo se ve todo el campus de lado a lado`() {
        // Longitudes extremas de los lugares de edificios_unal.json
        val anchoCampus = -74.0798346 - -74.0911391
        assertTrue(LimitesMapa.ANCHO_VISIBLE_MINIMO > anchoCampus)
        assertTrue(LimitesMapa.ANCHO_VISIBLE_MINIMO <= LimitesMapa.ESTE - LimitesMapa.OESTE)
    }
}
