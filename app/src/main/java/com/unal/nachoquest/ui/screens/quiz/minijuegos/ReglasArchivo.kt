package com.unal.nachoquest.ui.screens.quiz.minijuegos

import kotlin.random.Random

/** Lo que se ve al abrir una caja: el folio buscado está antes, después o justo ahí. */
internal enum class Comparacion { ANTES, AQUI, DESPUES }

/** Un estante: los números de las cajas, de menor a mayor, y cuál guarda el folio buscado. */
internal data class Estante(val numeros: List<Int>, val objetivo: Int) {
    val folioBuscado: Int get() = numeros[objetivo]
}

/**
 * Busca el folio (capítulo 15): las cajas del Archivo Central e Histórico están en orden de
 * número, pero cerradas. Al abrir una se ve su número y se sabe si el folio buscado está antes o
 * después. Abriendo siempre la del medio de las que quedan (la búsqueda binaria que enseña el
 * Búho), cualquier folio se encuentra en pocas aperturas.
 */
internal object ReglasArchivo {

    // 7, 15 y 31 cajas: con la búsqueda binaria bastan 3, 4 y 5 aperturas
    val CAJAS_POR_ESTANTE = listOf(7, 15, 31)

    /** Un estante con [cajas] números distintos entre 100 y 999, en orden, y un folio al azar. */
    fun estante(cajas: Int, random: Random = Random.Default): Estante {
        val numeros = (100..999).shuffled(random).take(cajas).sorted()
        return Estante(numeros, random.nextInt(cajas))
    }

    fun comparar(estante: Estante, abierta: Int): Comparacion = when {
        abierta == estante.objetivo -> Comparacion.AQUI
        estante.objetivo < abierta -> Comparacion.ANTES
        else -> Comparacion.DESPUES
    }

    /**
     * Las cajas donde todavía puede estar el folio, según lo que se ha visto al abrir las
     * [abiertas]. Si ya se encontró, el rango es solo esa caja.
     */
    fun rangoPosible(estante: Estante, abiertas: Collection<Int>): IntRange {
        var desde = 0
        var hasta = estante.numeros.lastIndex
        for (caja in abiertas) {
            when (comparar(estante, caja)) {
                Comparacion.AQUI -> return caja..caja
                Comparacion.ANTES -> hasta = minOf(hasta, caja - 1)
                Comparacion.DESPUES -> desde = maxOf(desde, caja + 1)
            }
        }
        return desde..hasta
    }

    /** La caja que aconseja abrir el Búho: la del medio de las que quedan. */
    fun delMedio(rango: IntRange): Int = (rango.first + rango.last) / 2

    /** Aperturas que necesita en el peor caso quien siempre abre la del medio. */
    fun optimo(cajas: Int): Int {
        var n = 0
        var cubiertas = 0
        while (cubiertas < cajas) {
            n++
            cubiertas = cubiertas * 2 + 1
        }
        return n
    }

    /** Estrellas según las aperturas de más (sobre el óptimo) en los tres estantes juntos. */
    fun estrellas(aperturasDeMas: Int): Int = when {
        aperturasDeMas <= 1 -> 3
        aperturasDeMas <= 5 -> 2
        else -> 1
    }
}
