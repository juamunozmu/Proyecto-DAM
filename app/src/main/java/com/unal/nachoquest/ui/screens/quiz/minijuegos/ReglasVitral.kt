package com.unal.nachoquest.ui.screens.quiz.minijuegos

/**
 * Un nivel del vitral: un cuadro de [lado] × [lado] vidrios. El nivel se arma tocando
 * [toquesQueLoRompen] sobre el vitral encendido, así que siempre tiene solución.
 */
internal data class NivelVitral(val lado: Int, val toquesQueLoRompen: List<Int>)

/**
 * Enciende el vitral (capítulo 12): el acertijo de «apaga las luces» al revés. Tocar un vidrio
 * cambia ese vidrio y los que tiene arriba, abajo y a los lados (encendido ↔ apagado). Hay que
 * dejarlos todos encendidos. Los vidrios se numeran por filas: fila × lado + columna.
 */
internal object ReglasVitral {

    val NIVELES = listOf(
        NivelVitral(lado = 3, toquesQueLoRompen = listOf(0, 8)),
        NivelVitral(lado = 4, toquesQueLoRompen = listOf(1, 6, 11, 12)),
        NivelVitral(lado = 5, toquesQueLoRompen = listOf(0, 7, 12, 16, 24))
    )

    fun encendido(lado: Int): List<Boolean> = List(lado * lado) { true }

    /** El vitral roto con el que empieza el nivel. */
    fun inicial(nivel: NivelVitral): List<Boolean> =
        nivel.toquesQueLoRompen.fold(encendido(nivel.lado)) { vitral, vidrio -> tocar(vitral, nivel.lado, vidrio) }

    /** Cambia el vidrio tocado y sus vecinos de arriba, abajo, izquierda y derecha. */
    fun tocar(vitral: List<Boolean>, lado: Int, vidrio: Int): List<Boolean> {
        val fila = vidrio / lado
        val columna = vidrio % lado
        val cambian = buildSet {
            add(vidrio)
            if (fila > 0) add(vidrio - lado)
            if (fila < lado - 1) add(vidrio + lado)
            if (columna > 0) add(vidrio - 1)
            if (columna < lado - 1) add(vidrio + 1)
        }
        return vitral.mapIndexed { i, prendido -> if (i in cambian) !prendido else prendido }
    }

    fun resuelto(vitral: List<Boolean>): Boolean = vitral.all { it }

    /**
     * Los vidrios que hay que tocar, la menor cantidad posible, para encender todo [vitral].
     * Se prueba cada combinación de toques en la primera fila; con ella el resto queda decidido,
     * porque un vidrio apagado solo se puede arreglar tocando el que tiene justo debajo. Vacía si
     * ya está encendido o si no tiene solución.
     */
    fun solucionMinima(vitral: List<Boolean>, lado: Int): List<Int> {
        var mejor: List<Int>? = null
        for (mascara in 0 until (1 shl lado)) {
            var actual = vitral
            val toques = mutableListOf<Int>()
            for (columna in 0 until lado) {
                if (mascara and (1 shl columna) != 0) {
                    actual = tocar(actual, lado, columna)
                    toques.add(columna)
                }
            }
            for (fila in 1 until lado) {
                for (columna in 0 until lado) {
                    if (!actual[(fila - 1) * lado + columna]) {
                        val vidrio = fila * lado + columna
                        actual = tocar(actual, lado, vidrio)
                        toques.add(vidrio)
                    }
                }
            }
            if (resuelto(actual) && (mejor == null || toques.size < mejor.size)) mejor = toques
        }
        return mejor.orEmpty()
    }

    /** Toques mínimos para encender el vitral con el que empieza el nivel. */
    fun minimo(nivel: NivelVitral): Int = solucionMinima(inicial(nivel), nivel.lado).size

    /** Estrellas según los toques de más (sobre el mínimo) en los tres niveles juntos. */
    fun estrellas(toquesDeMas: Int): Int = when {
        toquesDeMas <= 2 -> 3
        toquesDeMas <= 8 -> 2
        else -> 1
    }
}
