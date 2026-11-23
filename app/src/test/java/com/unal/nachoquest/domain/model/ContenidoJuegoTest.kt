package com.unal.nachoquest.domain.model

import com.unal.nachoquest.ui.screens.map.LimitesMapa
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Revisa que la historia, el banco de retos, el catálogo de ítems y los edificios del
 * campus (assets/edificios_unal.json) sigan siendo coherentes entre sí al editarlos.
 */
class ContenidoJuegoTest {

    private val puntos: List<JsonObject> by lazy {
        // Las pruebas unitarias se ejecutan con el módulo app/ como directorio de trabajo
        val texto = File("src/main/assets/edificios_unal.json").readText(Charsets.UTF_8)
        Json.parseToJsonElement(texto).jsonObject.getValue("puntos").jsonArray.map { it.jsonObject }
    }

    private val idsPuntos: Set<String> by lazy { puntos.map { it.texto("id") }.toSet() }

    private fun JsonObject.texto(campo: String): String = getValue(campo).jsonPrimitive.content

    @Test
    fun `los edificios tienen ids únicos y todos los campos que lee la app`() {
        val repetidos = puntos.groupBy { it.texto("id") }.filterValues { it.size > 1 }.keys
        assertTrue("Ids repetidos: $repetidos", repetidos.isEmpty())

        puntos.forEach { punto ->
            listOf("id", "nombre", "edificio", "latitud", "longitud", "alias").forEach { campo ->
                assertTrue("${punto.texto("id")} no tiene '$campo'", campo in punto)
            }
            assertTrue("${punto.texto("id")} no tiene nombre", punto.texto("nombre").isNotBlank())
        }
    }

    @Test
    fun `las categorías del json existen en la app`() {
        // CategoriaLugar.desde() usa ACADEMICO si no reconoce el valor, lo que escondería un error de tipeo
        val validas = CategoriaLugar.entries.map { it.name }.toSet()
        puntos.forEach { punto ->
            val categoria = punto["categoria"]?.jsonPrimitive?.content
            assertTrue("${punto.texto("id")}: categoría desconocida '$categoria'", categoria in validas)
        }
    }

    @Test
    fun `los edificios están dentro del área navegable del mapa`() {
        // Los mismos límites que usa el mapa (LimitesMapa): un lugar fuera de ellos no se podría ver
        puntos.forEach { punto ->
            val lat = punto.getValue("latitud").jsonPrimitive.double
            val lon = punto.getValue("longitud").jsonPrimitive.double
            assertTrue("${punto.texto("id")} está fuera del mapa ($lat, $lon)", LimitesMapa.contiene(lat, lon))
        }
    }

    @Test
    fun `cada capítulo apunta a un edificio real del campus`() {
        Historia.capitulos.forEach { capitulo ->
            (listOf(capitulo.puntoId) + capitulo.puntoIdsAlternos).forEach { id ->
                assertTrue("El capítulo ${capitulo.numero} usa el punto '$id', que no está en el json", id in idsPuntos)
            }
        }
    }

    @Test
    fun `solo los capítulos tienen retos en el json`() {
        // Los demás edificios usarían el reto genérico, que no está pensado para el mapa
        val conReto = puntos.filter { it["tieneRetoDisponible"]?.jsonPrimitive?.boolean == true }.map { it.texto("id") }
        conReto.forEach { id -> assertNotNull("'$id' tiene reto pero no es un capítulo", Historia.capituloPorPunto(id)) }
    }

    @Test
    fun `los ítems del catálogo hablan de lugares que existen`() {
        CatalogoItems.todosLosItems.forEach { item ->
            assertTrue("${item.id}: origen '${item.puntoOrigenId}' no existe", item.puntoOrigenId in idsPuntos)
            item.desbloqueaPuntoId?.let { assertTrue("${item.id}: desbloquea '$it', que no existe", it in idsPuntos) }
        }
        val ids = CatalogoItems.todosLosItems.map { it.id }
        assertEquals("Hay ítems con el mismo id", ids.size, ids.toSet().size)
    }

    @Test
    fun `cada capítulo se abre con el ítem que entrega el anterior`() {
        val capitulos = Historia.capitulos
        assertNull("El primer capítulo no debe pedir ítems", capitulos.first().itemRequeridoId)
        capitulos.zipWithNext().forEach { (anterior, siguiente) ->
            assertEquals(
                "El capítulo ${siguiente.numero} debería pedir lo que entrega el ${anterior.numero}",
                anterior.itemRecompensaId,
                siguiente.itemRequeridoId
            )
        }
        capitulos.forEach { capitulo ->
            val item = CatalogoItems.obtenerPorId(capitulo.itemRecompensaId)
            assertNotNull("El capítulo ${capitulo.numero} entrega un ítem que no existe", item)
            assertEquals(CategoriaItem.OBJETO, item!!.categoria)
        }
    }

    @Test
    fun `cada capítulo tiene su escena de inicio y de cierre`() {
        Historia.capitulos.forEach { capitulo ->
            assertTrue(Historia.escena(capitulo.escenaIntro, "Ana").isNotEmpty())
            assertTrue(Historia.escena(capitulo.escenaOutro, "Ana").isNotEmpty())
        }
        assertTrue(Historia.escena(Historia.ESCENA_PROLOGO, "Ana").isNotEmpty())
        assertTrue(Historia.escena(Historia.ESCENA_EPILOGO, "Ana").isNotEmpty())
    }

    @Test
    fun `el reto de cada capítulo coincide con lo que promete la historia`() {
        Historia.capitulos.forEach { capitulo ->
            val reto = BancoRetos.retoParaPunto(capitulo.puntoId)
            assertEquals(capitulo.retoId, reto.id)
            assertEquals("XP del capítulo ${capitulo.numero}", capitulo.xpRecompensa, reto.recompensaXp)
            assertEquals("Ítem del capítulo ${capitulo.numero}", capitulo.itemRecompensaId, reto.itemRecompensaId)
            // Tras el minijuego se sacan 3 preguntas al azar del banco del capítulo
            assertTrue("El capítulo ${capitulo.numero} necesita al menos 3 preguntas", reto.preguntas.size >= 3)
        }
    }

    @Test
    fun `las recompensas de los retos existen y son del tipo correcto`() {
        val retos = Historia.capitulos.map { BancoRetos.retoParaPunto(it.puntoId) } + BancoRetos.retoParaPunto("cualquier_lugar")
        retos.forEach { reto ->
            reto.itemRecompensaId?.let { assertEquals(CategoriaItem.OBJETO, CatalogoItems.obtenerPorId(it)?.categoria) }
            reto.insigniaRecompensaId?.let { assertEquals(CategoriaItem.INSIGNIA, CatalogoItems.obtenerPorId(it)?.categoria) }
            reto.coleccionableRecompensaId?.let { assertEquals(CategoriaItem.COLECCIONABLE, CatalogoItems.obtenerPorId(it)?.categoria) }
        }
    }

    @Test
    fun `las preguntas tienen cuatro opciones distintas y una respuesta válida`() {
        val retos = Historia.capitulos.map { BancoRetos.retoParaPunto(it.puntoId) } + BancoRetos.retoParaPunto("cualquier_lugar")
        val preguntas = retos.flatMap { it.preguntas }
        preguntas.forEach { pregunta ->
            assertEquals("${pregunta.id}: debe tener 4 opciones", 4, pregunta.opciones.size)
            assertEquals("${pregunta.id}: opciones repetidas", 4, pregunta.opciones.toSet().size)
            assertTrue("${pregunta.id}: respuesta fuera de rango", pregunta.indiceCorrecta in pregunta.opciones.indices)
        }
        val ids = preguntas.map { it.id }
        assertEquals("Hay preguntas con el mismo id", ids.size, ids.toSet().size)
    }
}
