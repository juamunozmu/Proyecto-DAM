package com.unal.nachoquest.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Colores de los cinco Hilos de la Memoria, en el orden en que se recuperan en el Acto III. */
val ColoresHilos = listOf(
    Color(0xFFE53935), // rojo
    Color(0xFF1E88E5), // azul
    Color(0xFF43A047), // verde
    Color(0xFFFDD835), // amarillo
    Color(0xFF8E24AA)  // violeta
)

/**
 * El Enredo, el personaje del Acto III: una madeja de hilos de colores que gira, con hebras
 * sueltas que se mueven y dos ojos grandes que miran a todos lados. No borra como el Olvido:
 * revuelve.
 */
@Composable
fun Enredo(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "enredo")
    val giro by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "giro"
    )
    val fase by transicion.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)),
        label = "fase"
    )
    val rebote by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "rebote"
    )

    Canvas(modifier = modifier) {
        val radio = size.minDimension * 0.27f
        val centro = Offset(size.width / 2, size.height / 2 - radio * 0.08f * rebote)
        val grosor = radio * 0.09f

        // Hebras sueltas que salen de la madeja y ondulan
        ColoresHilos.forEachIndexed { i, color ->
            val angulo = (PI / 2 + (i - 2) * 0.55).toFloat()
            val inicio = Offset(centro.x + cos(angulo) * radio * 0.9f, centro.y + sin(angulo) * radio * 0.9f)
            val camino = Path().apply {
                moveTo(inicio.x, inicio.y)
                val pasos = 12
                for (p in 1..pasos) {
                    val t = p / pasos.toFloat()
                    val largo = radio * (0.9f + t * 0.85f)
                    val onda = sin(fase + i * 1.3f + t * 5f) * radio * 0.16f * t
                    val x = centro.x + cos(angulo) * largo - sin(angulo) * onda
                    val y = centro.y + sin(angulo) * largo + cos(angulo) * onda
                    lineTo(x, y)
                }
            }
            drawPath(camino, color, style = Stroke(width = grosor * 0.8f, cap = StrokeCap.Round))
        }

        // La madeja: un fondo oscuro y vueltas de hilo en óvalos inclinados que giran
        drawCircle(Color(0xFF3A1F4D), radius = radio, center = centro)
        rotate(giro, pivot = centro) {
            for (vuelta in 0 until 10) {
                val color = ColoresHilos[vuelta % ColoresHilos.size]
                val inclinacion = vuelta * 36f
                val ancho = radio * (1.7f - (vuelta % 3) * 0.18f)
                val alto = radio * (0.55f + (vuelta % 4) * 0.12f)
                rotate(inclinacion, pivot = centro) {
                    drawOval(
                        color = color.copy(alpha = 0.9f),
                        topLeft = Offset(centro.x - ancho / 2, centro.y - alto / 2),
                        size = Size(ancho, alto),
                        style = Stroke(width = grosor)
                    )
                }
            }
        }

        // Ojos grandes y blancos; las pupilas dan vueltas, como si buscara con quién jugar
        val separacion = radio * 0.36f
        val radioOjo = radio * 0.27f
        val radioPupila = radioOjo * 0.48f
        val mirada = Offset(cos(fase) * radioOjo * 0.38f, sin(fase * 2) * radioOjo * 0.25f)
        listOf(-1f, 1f).forEach { lado ->
            val ojo = Offset(centro.x + lado * separacion, centro.y - radio * 0.08f)
            drawCircle(Color.White, radius = radioOjo, center = ojo)
            drawCircle(Color(0xFF1B1B1F), radius = radioPupila, center = ojo + mirada)
            drawCircle(Color.White, radius = radioPupila * 0.35f, center = ojo + mirada + Offset(-radioPupila * 0.3f, -radioPupila * 0.3f))
        }
        // Sonrisa traviesa, de lado
        val boca = Path().apply {
            moveTo(centro.x - radio * 0.28f, centro.y + radio * 0.38f)
            quadraticTo(centro.x, centro.y + radio * (0.62f + 0.06f * rebote), centro.x + radio * 0.34f, centro.y + radio * 0.30f)
        }
        drawPath(boca, Color.White, style = Stroke(width = grosor * 0.9f, cap = StrokeCap.Round))
    }
}
