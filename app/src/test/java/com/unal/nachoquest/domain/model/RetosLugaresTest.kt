package com.unal.nachoquest.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RetosLugaresTest {

    private val puntos: Map<String, JsonObject> by lazy {
        val texto = File("src/main/assets/edificios_unal.json").readText(Charsets.UTF_8)
        Json.parseToJsonElement(texto).jsonObject.getValue("puntos").jsonArray
            .map { it.jsonObject }
            .associateBy { it.getValue("id").jsonPrimitive.content }
    }

    @Test
    fun `cada reto de lugar está en un edificio visible del mapa que no es capítulo`() {
        val ids = RetosLugares.retos.map { it.puntoId }
        assertEquals("Hay lugares con dos retos", ids.size, ids.toSet().size)
        RetosLugares.retos.forEach { reto ->
            val punto = puntos[reto.puntoId]
            assertNotNull("'${reto.puntoId}' no está en el json", punto)
            assertNull("'${reto.puntoId}' ya es un capítulo", Historia.capituloPorPunto(reto.puntoId))
            // Los edificios internos no se muestran en el mapa, así que su reto quedaría escondido
            val categoria = CategoriaLugar.desde(punto!!["categoria"]?.jsonPrimitive?.content.orEmpty())
            assertTrue("'${reto.puntoId}' es de una categoría oculta en el mapa", categoria.visibleEnMapa)
        }
    }

    @Test
    fun `cada lugar tiene preguntas de sobra para sortear`() {
        RetosLugares.retos.forEach { reto ->
            assertTrue(
                "${reto.puntoId} necesita más de ${RetosLugares.PREGUNTAS_POR_RETO} preguntas",
                reto.preguntas.size > RetosLugares.PREGUNTAS_POR_RETO
            )
        }
    }

    @Test
    fun `el banco de retos arma el reto del lugar con su id y su XP`() {
        RetosLugares.retos.forEach { lugar ->
            val reto = BancoRetos.retoParaPunto(lugar.puntoId)
            assertEquals(lugar.retoId, reto.id)
            assertEquals(Historia.retoIdParaPunto(lugar.puntoId), reto.id)
            assertEquals(RetosLugares.XP_RECOMPENSA, reto.recompensaXp)
            assertEquals(lugar.preguntas, reto.preguntas)
        }
    }

    @Test
    fun `todas las preguntas del juego tienen cuatro opciones distintas e ids únicos`() {
        val preguntas = RetosLugares.retos.flatMap { it.preguntas } +
            Historia.capitulos.flatMap { BancoRetos.retoParaPunto(it.puntoId).preguntas } +
            TriviaDiaria.banco
        preguntas.forEach { pregunta ->
            assertEquals("${pregunta.id}: debe tener 4 opciones", 4, pregunta.opciones.size)
            assertEquals("${pregunta.id}: opciones repetidas", 4, pregunta.opciones.toSet().size)
            assertTrue("${pregunta.id}: respuesta fuera de rango", pregunta.indiceCorrecta in pregunta.opciones.indices)
        }
        val repetidos = preguntas.groupBy { it.id }.filterValues { it.size > 1 }.keys
        assertTrue("Ids de pregunta repetidos: $repetidos", repetidos.isEmpty())
    }
}
