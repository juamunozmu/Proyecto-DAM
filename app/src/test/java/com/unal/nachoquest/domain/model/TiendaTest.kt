package com.unal.nachoquest.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TiendaTest {

    @Test
    fun `la ayuda se paga primero con la reserva y después con monedas`() {
        val conReserva = UserProfile(monedas = 0, ayudas = mapOf("comodin" to 1))
        assertEquals(PagoAyuda.DE_RESERVA, Tienda.formaDePago(conReserva, TipoAyuda.COMODIN))

        val conMonedas = UserProfile(monedas = TipoAyuda.COMODIN.precio, ayudas = mapOf("comodin" to 0))
        assertEquals(PagoAyuda.CON_MONEDAS, Tienda.formaDePago(conMonedas, TipoAyuda.COMODIN))

        val sinNada = UserProfile(monedas = TipoAyuda.COMODIN.precio - 1)
        assertEquals(PagoAyuda.SIN_SALDO, Tienda.formaDePago(sinNada, TipoAyuda.COMODIN))
    }

    @Test
    fun `la reserva de cada ayuda es independiente`() {
        val perfil = UserProfile(ayudas = mapOf("tiempo" to 3))
        assertEquals(3, Tienda.reserva(perfil, TipoAyuda.TIEMPO))
        assertEquals(0, Tienda.reserva(perfil, TipoAyuda.CORAZON))
    }

    @Test
    fun `equipar un aspecto reemplaza al de la misma ranura y tocarlo otra vez lo quita`() {
        var equipados = Tienda.alternarEquipado(emptyList(), "gorra")
        equipados = Tienda.alternarEquipado(equipados, "gafas")
        assertEquals(setOf("gorra", "gafas"), equipados.toSet())

        // El birrete también va en la cabeza: reemplaza a la gorra
        equipados = Tienda.alternarEquipado(equipados, "birrete")
        assertEquals(setOf("birrete", "gafas"), equipados.toSet())

        equipados = Tienda.alternarEquipado(equipados, "gafas")
        assertEquals(listOf("birrete"), equipados)

        // Un id que no existe no cambia nada
        assertEquals(listOf("birrete"), Tienda.alternarEquipado(equipados, "capa_invisible"))
    }

    @Test
    fun `el catálogo no repite ids y todo tiene precio`() {
        val ids = Tienda.aspectos.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(Tienda.aspectos.all { it.precio > 0 })
        assertTrue(TipoAyuda.entries.all { it.precio > 0 })
        assertEquals(TipoAyuda.entries.size, TipoAyuda.entries.map { it.id }.toSet().size)
        TipoAyuda.entries.forEach { assertEquals(it, TipoAyuda.desde(it.id)) }
    }

    @Test
    fun `todos los retos dan monedas la primera vez`() {
        val retos = Historia.capitulos.map { BancoRetos.retoParaPunto(it.puntoId) } +
            RetosLugares.retos.map { BancoRetos.retoParaPunto(it.puntoId) }
        retos.forEach { assertTrue("${it.id} no da monedas", it.recompensaMonedas > 0) }
    }
}
