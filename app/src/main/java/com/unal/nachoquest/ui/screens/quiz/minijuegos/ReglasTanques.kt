package com.unal.nachoquest.ui.screens.quiz.minijuegos

/** Lo que se puede hacer con los dos tanques. */
internal enum class AccionTanque { LLENAR_A, LLENAR_B, VACIAR_A, VACIAR_B, PASAR_A_B, PASAR_B_A }

/** Un nivel: las capacidades de los tanques (en litros) y los litros que hay que dejar en uno. */
internal data class NivelTanques(val capacidadA: Int, val capacidadB: Int, val objetivo: Int)

/** Litros que tiene cada tanque. */
internal data class EstadoTanques(val a: Int = 0, val b: Int = 0)

/**
 * Agua exacta (capítulo 10): el acertijo clásico de las jarras. Con dos tanques sin marcas
 * intermedias, solo se puede llenar uno hasta el tope, vaciarlo o pasar agua al otro hasta que
 * uno se vacíe o el otro se llene. Hay que lograr que uno quede con el objetivo exacto.
 */
internal object ReglasTanques {

    val NIVELES = listOf(
        NivelTanques(capacidadA = 3, capacidadB = 5, objetivo = 4),
        NivelTanques(capacidadA = 4, capacidadB = 9, objetivo = 6),
        NivelTanques(capacidadA = 5, capacidadB = 7, objetivo = 6)
    )

    fun aplicar(nivel: NivelTanques, estado: EstadoTanques, accion: AccionTanque): EstadoTanques = when (accion) {
        AccionTanque.LLENAR_A -> estado.copy(a = nivel.capacidadA)
        AccionTanque.LLENAR_B -> estado.copy(b = nivel.capacidadB)
        AccionTanque.VACIAR_A -> estado.copy(a = 0)
        AccionTanque.VACIAR_B -> estado.copy(b = 0)
        AccionTanque.PASAR_A_B -> {
            val pasa = minOf(estado.a, nivel.capacidadB - estado.b)
            EstadoTanques(estado.a - pasa, estado.b + pasa)
        }
        AccionTanque.PASAR_B_A -> {
            val pasa = minOf(estado.b, nivel.capacidadA - estado.a)
            EstadoTanques(estado.a + pasa, estado.b - pasa)
        }
    }

    fun resuelto(nivel: NivelTanques, estado: EstadoTanques): Boolean =
        estado.a == nivel.objetivo || estado.b == nivel.objetivo

    /**
     * Las acciones más cortas para resolver el nivel desde [desde] (búsqueda en anchura sobre
     * todos los estados posibles, que son pocos). Vacía si ya está resuelto o no tiene solución.
     */
    fun caminoMinimo(nivel: NivelTanques, desde: EstadoTanques = EstadoTanques()): List<AccionTanque> {
        if (resuelto(nivel, desde)) return emptyList()
        val previo = HashMap<EstadoTanques, Pair<EstadoTanques, AccionTanque>>()
        val cola = ArrayDeque(listOf(desde))
        val visto = hashSetOf(desde)
        while (cola.isNotEmpty()) {
            val actual = cola.removeFirst()
            for (accion in AccionTanque.entries) {
                val siguiente = aplicar(nivel, actual, accion)
                if (!visto.add(siguiente)) continue
                previo[siguiente] = actual to accion
                if (resuelto(nivel, siguiente)) {
                    val camino = mutableListOf<AccionTanque>()
                    var paso = siguiente
                    while (paso != desde) {
                        val (antes, hecha) = previo.getValue(paso)
                        camino.add(hecha)
                        paso = antes
                    }
                    return camino.reversed()
                }
                cola.addLast(siguiente)
            }
        }
        return emptyList()
    }

    /** Movimientos mínimos para resolver el nivel desde los tanques vacíos. */
    fun minimo(nivel: NivelTanques): Int = caminoMinimo(nivel).size

    /** Estrellas según los movimientos de más (sobre el mínimo) en los tres niveles juntos. */
    fun estrellas(movimientosDeMas: Int): Int = when {
        movimientosDeMas <= 2 -> 3
        movimientosDeMas <= 8 -> 2
        else -> 1
    }
}
