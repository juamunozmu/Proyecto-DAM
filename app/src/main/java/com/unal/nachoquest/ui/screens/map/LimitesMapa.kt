package com.unal.nachoquest.ui.screens.map

/**
 * Área del mapa: los lugares del campus (edificios_unal.json) ocupan más o menos
 * lat 4.6324–4.6430 y lon -74.0911–-74.0798; se deja un margen de unos 250 m alrededor para
 * que se vean las calles de acceso (calle 26, carrera 30, calle 45) sin que sobre ciudad.
 */
object LimitesMapa {

    const val NORTE = 4.6455
    const val SUR = 4.6300
    const val ESTE = -74.0775
    const val OESTE = -74.0935

    const val CENTRO_LAT = (NORTE + SUR) / 2
    const val CENTRO_LON = (ESTE + OESTE) / 2

    const val ZOOM_INICIAL = 17.0
    const val ZOOM_MAXIMO = 20.0

    // Por si el cálculo según la pantalla no se puede hacer (vista sin medir)
    const val ZOOM_MINIMO_POR_DEFECTO = 16.0

    // Ancho (en grados de longitud) que se ve con el zoom mínimo: el campus con unos 100 m de
    // margen a cada lado. Es menor que el área navegable para que en pantallas verticales no
    // sobre tanta ciudad arriba y abajo; el resto del margen se ve moviendo el mapa.
    const val ANCHO_VISIBLE_MINIMO = 0.0133

    fun contiene(lat: Double, lon: Double): Boolean = lat in SUR..NORTE && lon in OESTE..ESTE

    /**
     * Zoom mínimo para una vista de [anchoPx] píxeles: el que hace que [ANCHO_VISIBLE_MINIMO]
     * llene justo la pantalla, para que al alejarse se vea el campus de lado a lado y no más.
     * [tamanoTesela] es el tamaño en píxeles de una tesela del mapa (256 por defecto en osmdroid).
     */
    fun zoomMinimo(anchoPx: Int, tamanoTesela: Int = 256): Double {
        if (anchoPx <= 0) return ZOOM_MINIMO_POR_DEFECTO
        // A zoom z el mundo mide tamanoTesela * 2^z píxeles de ancho (360 grados de longitud)
        val fraccionDelMundo = ANCHO_VISIBLE_MINIMO / 360.0
        val zoom = Math.log(anchoPx / (tamanoTesela * fraccionDelMundo)) / Math.log(2.0)
        return zoom.coerceIn(14.0, ZOOM_INICIAL)
    }
}
