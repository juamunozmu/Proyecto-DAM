package com.unal.nachoquest.ui.screens.quiz.minijuegos

/**
 * Las ventanillas: cada una es una pila de carpetas, de la de abajo a la de arriba. Las carpetas
 * se numeran por tamaño (1 es la más pequeña).
 */
internal typealias Ventanillas = List<List<Int>>

/** Un movimiento: pasar la carpeta de arriba de una ventanilla a otra (índices 0 a 2). */
internal data class Movimiento(val desde: Int, val hasta: Int)

/**
 * La pila de solicitudes (capítulo 17): el Sello amontonó las carpetas de solicitudes en la
 * primera ventanilla del CADE. Hay que pasarlas todas a la tercera moviendo una a la vez y sin
 * poner nunca una carpeta grande sobre una pequeña (se doblaría). Es la Torre de Hanói: con n
 * carpetas, lo mínimo son 2^n - 1 movimientos.
 */
internal object ReglasPila {

    const val VENTANILLAS = 3
    const val DESTINO = 2
    val CARPETAS_POR_NIVEL = listOf(3, 4)

    fun inicial(carpetas: Int): Ventanillas = listOf((carpetas downTo 1).toList(), emptyList(), emptyList())

    fun puedeMover(ventanillas: Ventanillas, mov: Movimiento): Boolean {
        if (mov.desde == mov.hasta) return false
        val carpeta = ventanillas[mov.desde].lastOrNull() ?: return false
        val debajo = ventanillas[mov.hasta].lastOrNull()
        return debajo == null || debajo > carpeta
    }

    /** Hace el movimiento; si no se puede, deja las ventanillas como estaban. */
    fun mover(ventanillas: Ventanillas, mov: Movimiento): Ventanillas {
        if (!puedeMover(ventanillas, mov)) return ventanillas
        val carpeta = ventanillas[mov.desde].last()
        return ventanillas.mapIndexed { i, pila ->
            when (i) {
                mov.desde -> pila.dropLast(1)
                mov.hasta -> pila + carpeta
                else -> pila
            }
        }
    }

    fun resuelto(ventanillas: Ventanillas, carpetas: Int): Boolean =
        ventanillas[DESTINO] == (carpetas downTo 1).toList()

    fun minimo(carpetas: Int): Int = (1 shl carpetas) - 1

    /**
     * Los movimientos más cortos para llevar todas las carpetas a la tercera ventanilla desde
     * como estén. Se coloca primero la más grande: si ya está en su sitio, basta con llevar las
     * demás encima; si no, las demás se apartan a la ventanilla que sobra, se mueve la grande y
     * luego se traen las demás.
     */
    fun solucion(ventanillas: Ventanillas): List<Movimiento> {
        val pilas = ventanillas.map { it.toMutableList() }
        val movimientos = mutableListOf<Movimiento>()
        fun donde(carpeta: Int) = pilas.indexOfFirst { carpeta in it }
        fun llevar(carpeta: Int, destino: Int) {
            if (carpeta == 0) return
            val origen = donde(carpeta)
            if (origen == destino) {
                llevar(carpeta - 1, destino)
                return
            }
            val otra = VENTANILLAS - origen - destino
            llevar(carpeta - 1, otra)
            pilas[origen].removeAt(pilas[origen].lastIndex)
            pilas[destino].add(carpeta)
            movimientos.add(Movimiento(origen, destino))
            llevar(carpeta - 1, destino)
        }
        llevar(ventanillas.sumOf { it.size }, DESTINO)
        return movimientos
    }

    /** Estrellas según los movimientos de más (sobre el mínimo) en los dos niveles juntos. */
    fun estrellas(movimientosDeMas: Int): Int = when {
        movimientosDeMas <= 2 -> 3
        movimientosDeMas <= 10 -> 2
        else -> 1
    }
}
