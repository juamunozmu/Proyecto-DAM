package com.unal.nachoquest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PerfilViejoTest {

    @Test
    fun `los retos completados sin puntuación cuentan como una estrella`() {
        val perfil = UserProfile(
            retosCompletados = listOf("reto_plaza_che", "reto_biblio", "reto_cyt"),
            estrellasPorReto = mapOf("reto_cyt" to 3)
        )
        assertEquals(
            mapOf("reto_plaza_che" to 1, "reto_biblio" to 1, "reto_cyt" to 3),
            perfil.estrellasPorRetoReparadas()
        )
    }

    @Test
    fun `un perfil sin datos viejos no cambia`() {
        val perfil = UserProfile(
            retosCompletados = listOf("reto_plaza_che"),
            estrellasPorReto = mapOf("reto_plaza_che" to 2)
        )
        assertEquals(perfil.estrellasPorReto, perfil.estrellasPorRetoReparadas())
    }
}
