package com.unal.nachoquest.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private val COLORES_CONFETI = listOf(
    Color(0xFFD4A017), Color(0xFF2D5016), Color(0xFF4A7D2A),
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFFFFFFFF)
)

private data class Confeti(
    val x: Float,
    val retraso: Float,
    val velocidad: Float,
    val oscilacion: Float,
    val giro: Float,
    val color: Color,
    val ancho: Float
)

/**
 * Lluvia de confeti que cae una vez (o en bucle si [continuo] es true).
 */
@Composable
fun LluviaConfeti(modifier: Modifier = Modifier, cantidad: Int = 70, continuo: Boolean = false) {
    val piezas = remember {
        List(cantidad) {
            Confeti(
                x = Random.nextFloat(),
                retraso = Random.nextFloat() * 0.35f,
                velocidad = 0.7f + Random.nextFloat() * 0.6f,
                oscilacion = Random.nextFloat() * 2 * PI.toFloat(),
                giro = Random.nextFloat() * 720f - 360f,
                color = COLORES_CONFETI.random(),
                ancho = 6f + Random.nextFloat() * 8f
            )
        }
    }
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(continuo) {
        do {
            progreso.snapTo(0f)
            progreso.animateTo(1f, tween(3200, easing = LinearEasing))
        } while (continuo)
    }

    Canvas(modifier = modifier) {
        val t = progreso.value
        piezas.forEach { p ->
            val local = ((t - p.retraso) / (1f - p.retraso)).coerceIn(0f, 1f)
            if (local <= 0f || local >= 1f) return@forEach
            val y = -40f + local * p.velocidad * (size.height + 80f)
            val x = p.x * size.width + sin(local * 10f + p.oscilacion) * 30f
            rotate(p.giro * local, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = 1f - local * 0.5f),
                    topLeft = Offset(x, y),
                    size = Size(p.ancho, p.ancho * 0.55f)
                )
            }
        }
    }
}

/**
 * Partículas doradas que flotan hacia arriba (fondos de splash y escenas).
 */
@Composable
fun ParticulasFlotantes(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE5C04A),
    cantidad: Int = 24
) {
    val semillas = remember {
        List(cantidad) { Triple(Random.nextFloat(), Random.nextFloat(), 2f + Random.nextFloat() * 4f) }
    }
    val transicion = rememberInfiniteTransition(label = "particulas")
    val t by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "t"
    )
    Canvas(modifier = modifier) {
        semillas.forEachIndexed { i, (sx, sy, radio) ->
            val avance = (sy + t * (0.6f + (i % 3) * 0.2f)) % 1f
            val x = sx * size.width + sin((avance + i) * 6f) * 18f
            val y = size.height * (1f - avance)
            val alpha = sin(avance * PI.toFloat()).coerceAtLeast(0f) * 0.8f
            drawCircle(color.copy(alpha = alpha), radius = radio, center = Offset(x, y))
        }
    }
}

/**
 * Sacude el contenido horizontalmente cada vez que cambia [disparador] (si es mayor que 0).
 */
fun Modifier.sacudir(disparador: Int): Modifier = composed {
    val desplazamiento = remember { Animatable(0f) }
    LaunchedEffect(disparador) {
        if (disparador > 0) {
            for (paso in listOf(-18f, 16f, -12f, 9f, -5f, 0f)) {
                desplazamiento.animateTo(paso, tween(45))
            }
        }
    }
    graphicsLayer { translationX = desplazamiento.value }
}

/**
 * Pulso suave de escala, útil para llamar la atención sobre un botón u objetivo.
 */
fun Modifier.pulso(activo: Boolean = true, intensidad: Float = 0.06f): Modifier = composed {
    if (!activo) return@composed this
    val transicion = rememberInfiniteTransition(label = "pulso")
    val escala by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 1f + intensidad,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "escala"
    )
    graphicsLayer {
        scaleX = escala
        scaleY = escala
    }
}
