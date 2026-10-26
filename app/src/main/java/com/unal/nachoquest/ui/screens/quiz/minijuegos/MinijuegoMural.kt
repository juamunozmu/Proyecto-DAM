package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

private const val LADO = 3

/**
 * Rompecabezas 3x3: el recuerdo de la Plaza Che está desordenado.
 * Se tocan dos piezas para intercambiarlas.
 */
@Composable
fun MinijuegoMural(alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    // piezas[posición] = pieza que está en esa posición
    val piezas = remember { mutableStateListOf<Int>().apply { addAll(mezclaDificil()) } }
    var seleccion by remember { mutableStateOf<Int?>(null) }
    var movimientos by remember { mutableIntStateOf(0) }
    var resuelto by remember { mutableStateOf(false) }
    val minimo = remember { intercambiosMinimos(piezas.toList()) }

    LaunchedEffect(resuelto) {
        if (resuelto) {
            delay(900)
            val estrellas = when {
                movimientos <= minimo + 1 -> 3
                movimientos <= minimo + 5 -> 2
                else -> 1
            }
            alTerminar(estrellas)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Movimientos: $movimientos")
            Marcador("Piezas en su sitio: ${piezas.withIndex().count { it.index == it.value }}/9")
        }
        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            val espacio by animateDpAsState(if (resuelto) 0.dp else 4.dp, label = "espacio")
            val ladoPieza = (maxWidth - espacio * (LADO - 1)) / LADO
            Column(verticalArrangement = Arrangement.spacedBy(espacio)) {
                for (fila in 0 until LADO) {
                    Row(horizontalArrangement = Arrangement.spacedBy(espacio)) {
                        for (col in 0 until LADO) {
                            val posicion = fila * LADO + col
                            val pieza = piezas[posicion]
                            val seleccionada = seleccion == posicion
                            val escala by animateFloatAsState(
                                if (seleccionada) 0.9f else 1f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = "escalaPieza"
                            )
                            Box(
                                modifier = Modifier
                                    .size(ladoPieza)
                                    .graphicsLayer {
                                        scaleX = escala
                                        scaleY = escala
                                    }
                                    .clip(RoundedCornerShape(if (resuelto) 0.dp else 8.dp))
                                    .then(
                                        if (seleccionada) Modifier.border(3.dp, NachoYellow, RoundedCornerShape(8.dp))
                                        else Modifier
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        enabled = !resuelto
                                    ) {
                                        val previa = seleccion
                                        when {
                                            previa == null -> seleccion = posicion
                                            previa == posicion -> seleccion = null
                                            else -> {
                                                val tmp = piezas[previa]
                                                piezas[previa] = piezas[posicion]
                                                piezas[posicion] = tmp
                                                movimientos++
                                                seleccion = null
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (piezas.withIndex().all { it.index == it.value }) resuelto = true
                                            }
                                        }
                                    }
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Se dibuja la escena completa desplazada para mostrar solo esta pieza
                                    val lado = size.width
                                    val origenFila = pieza / LADO
                                    val origenCol = pieza % LADO
                                    clipRect {
                                        translate(-origenCol * lado, -origenFila * lado) {
                                            dibujarPlaza(lado * LADO)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (resuelto) "¡La plaza recuerda!" else "Pista: el cielo va arriba y la estatua en el centro.",
            color = if (resuelto) NachoGreenDark else Color.Gray,
            fontWeight = if (resuelto) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = if (resuelto) 18.sp else 13.sp
        )
    }
}

/** Permutación aleatoria con al menos 6 piezas fuera de su sitio. */
private fun mezclaDificil(): List<Int> {
    while (true) {
        val p = (0 until LADO * LADO).shuffled()
        if (p.withIndex().count { it.index != it.value } >= 6) return p
    }
}

/** Número mínimo de intercambios para ordenar la permutación (n − ciclos). */
private fun intercambiosMinimos(p: List<Int>): Int {
    val visto = BooleanArray(p.size)
    var ciclos = 0
    for (i in p.indices) {
        if (visto[i]) continue
        ciclos++
        var j = i
        while (!visto[j]) {
            visto[j] = true
            j = p[j]
        }
    }
    return p.size - ciclos
}

/**
 * Ilustración de la Plaza Che: cerros, edificios blancos de la Ciudad Blanca,
 * el auditorio, la estatua de Santander, la bandera y la plaza.
 */
private fun DrawScope.dibujarPlaza(t: Float) {
    // Cielo
    drawRect(Brush.verticalGradient(listOf(Color(0xFF6EC6FF), Color(0xFFD7F0FF)), endY = t * 0.5f), size = Size(t, t * 0.5f))
    // Sol y nubes
    drawCircle(Color(0xFFFFD54F), radius = t * 0.07f, center = Offset(t * 0.15f, t * 0.12f))
    drawCircle(Color(0x55FFD54F), radius = t * 0.1f, center = Offset(t * 0.15f, t * 0.12f))
    listOf(Offset(t * 0.5f, t * 0.1f), Offset(t * 0.82f, t * 0.17f)).forEach { c ->
        drawOval(Color.White, Offset(c.x - t * 0.08f, c.y - t * 0.025f), Size(t * 0.16f, t * 0.05f))
        drawOval(Color.White, Offset(c.x - t * 0.04f, c.y - t * 0.05f), Size(t * 0.09f, t * 0.06f))
    }
    // Cerros orientales
    val cerros = Path().apply {
        moveTo(0f, t * 0.38f)
        lineTo(t * 0.2f, t * 0.27f)
        lineTo(t * 0.38f, t * 0.33f)
        lineTo(t * 0.62f, t * 0.22f)
        lineTo(t * 0.85f, t * 0.3f)
        lineTo(t, t * 0.25f)
        lineTo(t, t * 0.5f)
        lineTo(0f, t * 0.5f)
        close()
    }
    drawPath(cerros, Color(0xFF3E7D3A))
    // Edificios blancos con ventanas
    data class Edificio(val x: Float, val ancho: Float, val alto: Float, val color: Color)
    listOf(
        Edificio(0.02f, 0.22f, 0.2f, Color(0xFFF5F5F0)),
        Edificio(0.70f, 0.28f, 0.22f, Color(0xFFB5523B)) // biblioteca de ladrillo
    ).forEach { e ->
        val arriba = t * (0.6f - e.alto)
        drawRect(e.color, Offset(t * e.x, arriba), Size(t * e.ancho, t * e.alto))
        for (fila in 0 until 3) for (col in 0 until 4) {
            drawRect(
                Color(0xFF8DA4B5),
                Offset(t * (e.x + 0.02f + col * e.ancho / 4.2f), arriba + t * (0.03f + fila * 0.055f)),
                Size(t * 0.03f, t * 0.03f)
            )
        }
    }
    // Auditorio León de Greiff (techo curvo)
    drawRect(Color(0xFFECEAE2), Offset(t * 0.28f, t * 0.42f), Size(t * 0.38f, t * 0.18f))
    drawArc(Color(0xFFD8D3C4), 180f, 180f, true, Offset(t * 0.28f, t * 0.34f), Size(t * 0.38f, t * 0.16f))
    for (i in 0 until 6) {
        drawRect(Color(0xFFBDB7A6), Offset(t * (0.31f + i * 0.058f), t * 0.47f), Size(t * 0.02f, t * 0.13f))
    }
    // Plaza (baldosas)
    drawRect(Color(0xFFD9CBB0), Offset(0f, t * 0.6f), Size(t, t * 0.4f))
    for (i in 0..8) {
        val x = t * i / 8f
        drawLine(Color(0x22000000), Offset(t / 2 + (x - t / 2) * 0.4f, t * 0.6f), Offset(x, t), strokeWidth = t * 0.004f)
    }
    for (i in 1..4) {
        val y = t * (0.6f + i * i * 0.025f)
        drawLine(Color(0x22000000), Offset(0f, y), Offset(t, y), strokeWidth = t * 0.004f)
    }
    // Pasto en las esquinas
    drawOval(Color(0xFF66BB6A), Offset(-t * 0.12f, t * 0.82f), Size(t * 0.35f, t * 0.3f))
    drawOval(Color(0xFF66BB6A), Offset(t * 0.78f, t * 0.84f), Size(t * 0.35f, t * 0.3f))
    // Estatua de Santander sobre su pedestal
    drawRoundRect(Color(0xFF9E9E9E), Offset(t * 0.43f, t * 0.7f), Size(t * 0.14f, t * 0.14f), CornerRadius(t * 0.01f))
    drawRect(Color(0xFF757575), Offset(t * 0.41f, t * 0.83f), Size(t * 0.18f, t * 0.03f))
    drawCircle(Color(0xFF4E5B4F), radius = t * 0.022f, center = Offset(t * 0.5f, t * 0.55f))
    drawRoundRect(Color(0xFF4E5B4F), Offset(t * 0.47f, t * 0.57f), Size(t * 0.06f, t * 0.13f), CornerRadius(t * 0.01f))
    // Bandera de Colombia
    drawRect(Color(0xFF5D4037), Offset(t * 0.86f, t * 0.6f), Size(t * 0.012f, t * 0.28f))
    drawRect(Color(0xFFFCD116), Offset(t * 0.872f, t * 0.6f), Size(t * 0.1f, t * 0.03f))
    drawRect(Color(0xFF003893), Offset(t * 0.872f, t * 0.63f), Size(t * 0.1f, t * 0.015f))
    drawRect(Color(0xFFCE1126), Offset(t * 0.872f, t * 0.645f), Size(t * 0.1f, t * 0.015f))
    // Personas en la plaza
    listOf(0.2f to 0.75f, 0.3f to 0.9f, 0.68f to 0.78f, 0.15f to 0.66f).forEach { (x, y) ->
        drawCircle(Color(0xFF5D4037), radius = t * 0.012f, center = Offset(t * x, t * y))
        drawRect(Color(0xFF2D5016), Offset(t * (x - 0.012f), t * (y + 0.012f)), Size(t * 0.024f, t * 0.04f))
    }
}
