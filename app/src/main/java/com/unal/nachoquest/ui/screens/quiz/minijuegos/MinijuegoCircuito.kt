package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

private const val N = 5

// Conexiones como bits: norte, este, sur, oeste
private const val NORTE = 1
private const val ESTE = 2
private const val SUR = 4
private const val OESTE = 8

private val DIRECCIONES = listOf(NORTE to (-1 to 0), ESTE to (0 to 1), SUR to (1 to 0), OESTE to (0 to -1))

private fun opuesta(d: Int) = when (d) {
    NORTE -> SUR
    SUR -> NORTE
    ESTE -> OESTE
    else -> ESTE
}

/** Gira una máscara de conexiones 90° en sentido horario `veces` veces. */
private fun rotar(mascara: Int, veces: Int): Int {
    var m = mascara
    repeat(((veces % 4) + 4) % 4) { m = ((m shl 1) or (m shr 3)) and 0xF }
    return m
}

private class Tablero(
    val bases: IntArray,       // conexiones de cada pieza sin girar
    val giros: IntArray,       // giros iniciales
    val filaEntrada: Int,
    val filaSalida: Int,
    val giroMinimo: Int        // giros necesarios para resolverlo
)

/**
 * Genera un camino aleatorio de izquierda a derecha y rellena el resto con piezas sueltas.
 * Luego gira todas las piezas al azar; siempre tiene solución.
 */
private fun generarTablero(): Tablero {
    while (true) {
        val entrada = (0 until N).random()
        val camino = mutableListOf(entrada to 0)
        val visitado = mutableSetOf(entrada to 0)

        fun buscar(): Boolean {
            val (f, c) = camino.last()
            if (c == N - 1 && camino.size >= 8) return true
            for ((_, delta) in DIRECCIONES.shuffled()) {
                val siguiente = (f + delta.first) to (c + delta.second)
                if (siguiente.first !in 0 until N || siguiente.second !in 0 until N || siguiente in visitado) continue
                camino.add(siguiente)
                visitado.add(siguiente)
                if (buscar()) return true
                camino.removeAt(camino.lastIndex)
                visitado.remove(siguiente)
            }
            return false
        }
        if (!buscar() || camino.size > 14) continue

        val bases = IntArray(N * N)
        camino.forEachIndexed { i, (f, c) ->
            var m = 0
            m = m or if (i == 0) OESTE else direccionHacia(camino[i], camino[i - 1])
            m = m or if (i == camino.lastIndex) ESTE else direccionHacia(camino[i], camino[i + 1])
            bases[f * N + c] = m
        }
        val enCamino = camino.map { it.first * N + it.second }.toSet()
        for (i in 0 until N * N) {
            if (i !in enCamino) bases[i] = listOf(NORTE or SUR, NORTE or ESTE).random()
        }

        val giros = IntArray(N * N) { (0..3).random() }
        // Giros mínimos: cuántos giros horarios faltan para que cada pieza del camino encaje
        var minimo = 0
        for (i in enCamino) {
            val objetivo = bases[i]
            val mostrada = rotar(objetivo, giros[i])
            minimo += (0..3).first { rotar(mostrada, it) == objetivo }
        }
        // Las piezas del camino se guardan "sin girar" y lo mostrado es base rotada por los giros
        if (minimo < 6) continue
        return Tablero(bases, giros, entrada, camino.last().first, minimo)
    }
}

private fun direccionHacia(desde: Pair<Int, Int>, hacia: Pair<Int, Int>): Int = when {
    hacia.first < desde.first -> NORTE
    hacia.first > desde.first -> SUR
    hacia.second > desde.second -> ESTE
    else -> OESTE
}

/** Celdas con energía, partiendo de la entrada (borde izquierdo de `filaEntrada`). */
private fun celdasConEnergia(t: Tablero, giros: List<Int>): Set<Int> {
    val conectadas = mutableSetOf<Int>()
    val inicio = t.filaEntrada * N
    if (rotar(t.bases[inicio], giros[inicio]) and OESTE == 0) return conectadas
    val pendientes = ArrayDeque(listOf(inicio))
    conectadas.add(inicio)
    while (pendientes.isNotEmpty()) {
        val i = pendientes.removeFirst()
        val m = rotar(t.bases[i], giros[i])
        for ((dir, delta) in DIRECCIONES) {
            if (m and dir == 0) continue
            val f = i / N + delta.first
            val c = i % N + delta.second
            if (f !in 0 until N || c !in 0 until N) continue
            val j = f * N + c
            if (j in conectadas) continue
            if (rotar(t.bases[j], giros[j]) and opuesta(dir) != 0) {
                conectadas.add(j)
                pendientes.add(j)
            }
        }
    }
    return conectadas
}

@Composable
fun MinijuegoCircuito(alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val tablero = remember { generarTablero() }
    // Giros acumulados (no se reduce a 0-3 para que la animación siempre gire en el mismo sentido)
    val giros = remember { mutableStateListOf<Int>().apply { addAll(tablero.giros.toList()) } }
    var toques by remember { mutableIntStateOf(0) }

    val energia = celdasConEnergia(tablero, giros)
    val salida = tablero.filaSalida * N + (N - 1)
    val resuelto = salida in energia && rotar(tablero.bases[salida], giros[salida]) and ESTE != 0

    LaunchedEffect(resuelto) {
        if (resuelto) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(1000)
            alTerminar(
                when {
                    toques <= tablero.giroMinimo + 4 -> 3
                    toques <= tablero.giroMinimo + 12 -> 2
                    else -> 1
                }
            )
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Giros: $toques")
            Marcador(if (resuelto) "⚡ ¡Energía restaurada!" else "Energía: ${energia.size} piezas")
        }
        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val ladoIndicador = 30.dp
            val lado = (maxWidth - ladoIndicador * 2) / N
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Generador a la izquierda
                Column(modifier = Modifier.width(ladoIndicador)) {
                    for (f in 0 until N) {
                        Box(modifier = Modifier.size(ladoIndicador, lado), contentAlignment = Alignment.Center) {
                            if (f == tablero.filaEntrada) Text("⚡", fontSize = 22.sp)
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF263238))
                ) {
                    for (f in 0 until N) {
                        Row {
                            for (c in 0 until N) {
                                val i = f * N + c
                                PiezaCircuito(
                                    base = tablero.bases[i],
                                    giros = giros[i],
                                    conEnergia = i in energia,
                                    lado = lado,
                                    habilitada = !resuelto
                                ) {
                                    giros[i] = giros[i] + 1
                                    toques++
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        }
                    }
                }
                // Tablero a encender a la derecha
                Column(modifier = Modifier.width(ladoIndicador)) {
                    for (f in 0 until N) {
                        Box(modifier = Modifier.size(ladoIndicador, lado), contentAlignment = Alignment.Center) {
                            if (f == tablero.filaSalida) {
                                Text(if (resuelto) "💡" else "🔌", fontSize = 22.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (resuelto) "¡Los laboratorios vuelven a funcionar!" else "Toca una pieza para girarla. Las piezas con energía brillan.",
            color = if (resuelto) NachoGreenDark else Color.Gray,
            fontWeight = if (resuelto) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = if (resuelto) 17.sp else 13.sp
        )
    }
}

@Composable
private fun PiezaCircuito(
    base: Int,
    giros: Int,
    conEnergia: Boolean,
    lado: androidx.compose.ui.unit.Dp,
    habilitada: Boolean,
    onGirar: () -> Unit
) {
    val angulo by animateFloatAsState(
        giros * 90f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "giro"
    )
    val color by animateColorAsState(if (conEnergia) NachoYellow else Color(0xFF78909C), label = "energia")

    Box(
        modifier = Modifier
            .size(lado)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = habilitada,
                onClick = onGirar
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = angulo }
        ) {
            val centro = Offset(size.width / 2, size.height / 2)
            val grosor = size.width * 0.2f
            if (conEnergia) {
                drawCircle(color.copy(alpha = 0.18f), radius = size.width * 0.45f, center = centro)
            }
            for ((dir, delta) in DIRECCIONES) {
                if (base and dir == 0) continue
                val fin = Offset(centro.x + delta.second * size.width / 2, centro.y + delta.first * size.height / 2)
                drawLine(color, centro, fin, strokeWidth = grosor, cap = StrokeCap.Butt)
            }
            drawCircle(color, radius = grosor * 0.62f, center = centro)
        }
    }
}
