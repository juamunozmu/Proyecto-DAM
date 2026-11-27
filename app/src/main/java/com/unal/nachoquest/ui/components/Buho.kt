package com.unal.nachoquest.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.sin

/** El dorado de los ojos del Búho. */
val OjosBuho = Color(0xFFF2C94C)

/**
 * El Búho, guardián del Archivo Central e Histórico (Acto IV). El búho es símbolo de la
 * Universidad Nacional, y muchos ven su forma en el plano de la Ciudad Universitaria. Es sabio,
 * calmado y nocturno: ladea la cabeza, parpadea despacio y lleva unas gafas redondas de lectura.
 */
@Composable
fun Buho(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "buho")
    val ladeo by transicion.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(2400), RepeatMode.Reverse),
        label = "ladeo"
    )
    // Parpadeo lento: los ojos se cierran un momento cada pocos segundos
    val parpado by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 4200
                0f at 0
                0f at 3700
                1f at 3900
                0f at 4200
            }
        ),
        label = "parpado"
    )
    val respiro by transicion.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "respiro"
    )

    Canvas(modifier = modifier) {
        val r = size.minDimension * 0.3f
        val cx = size.width / 2
        val cuerpoCentro = Offset(cx, size.height * 0.6f)
        val escala = 1f + sin(respiro) * 0.015f
        val plumaje = Color(0xFF6D4C2F)
        val pecho = Color(0xFFD7B98E)

        // Cuerpo y pecho
        drawOval(
            plumaje,
            topLeft = Offset(cuerpoCentro.x - r * escala, cuerpoCentro.y - r * 1.05f),
            size = Size(r * 2 * escala, r * 2.1f)
        )
        drawOval(
            pecho,
            topLeft = Offset(cuerpoCentro.x - r * 0.62f, cuerpoCentro.y - r * 0.55f),
            size = Size(r * 1.24f, r * 1.5f)
        )
        // Plumas del pecho en forma de «v»
        for (fila in 0 until 3) {
            for (col in -1..1) {
                val p = Offset(cuerpoCentro.x + col * r * 0.3f + (fila % 2) * r * 0.15f, cuerpoCentro.y - r * 0.2f + fila * r * 0.32f)
                val v = Path().apply {
                    moveTo(p.x - r * 0.09f, p.y)
                    lineTo(p.x, p.y + r * 0.08f)
                    lineTo(p.x + r * 0.09f, p.y)
                }
                drawPath(v, plumaje.copy(alpha = 0.5f), style = Stroke(width = r * 0.04f))
            }
        }
        // Patas sobre una rama
        drawLine(Color(0xFF4E342E), Offset(cx - r * 1.4f, cuerpoCentro.y + r * 1.02f), Offset(cx + r * 1.4f, cuerpoCentro.y + r * 1.02f), strokeWidth = r * 0.14f)
        listOf(-1f, 1f).forEach { lado ->
            drawCircle(Color(0xFFF2A93B), radius = r * 0.1f, center = Offset(cx + lado * r * 0.3f, cuerpoCentro.y + r * 0.98f))
        }

        // Cabeza, que se ladea despacio
        val cabeza = Offset(cx, size.height * 0.3f)
        rotate(ladeo, pivot = cabeza) {
            // Orejas de plumas (los penachos)
            listOf(-1f, 1f).forEach { lado ->
                val oreja = Path().apply {
                    moveTo(cabeza.x + lado * r * 0.55f, cabeza.y - r * 0.45f)
                    lineTo(cabeza.x + lado * r * 0.95f, cabeza.y - r * 1.05f)
                    lineTo(cabeza.x + lado * r * 0.2f, cabeza.y - r * 0.62f)
                    close()
                }
                drawPath(oreja, plumaje)
            }
            drawOval(plumaje, topLeft = Offset(cabeza.x - r * 0.95f, cabeza.y - r * 0.75f), size = Size(r * 1.9f, r * 1.5f))
            // Disco facial claro
            drawOval(pecho, topLeft = Offset(cabeza.x - r * 0.82f, cabeza.y - r * 0.55f), size = Size(r * 1.64f, r * 1.15f))

            // Ojos grandes y dorados, con párpados que bajan al parpadear
            val separacion = r * 0.4f
            val radioOjo = r * 0.3f
            listOf(-1f, 1f).forEach { lado ->
                val ojo = Offset(cabeza.x + lado * separacion, cabeza.y - r * 0.05f)
                drawCircle(OjosBuho, radius = radioOjo, center = ojo)
                drawCircle(Color(0xFF1B1B1F), radius = radioOjo * 0.5f, center = ojo)
                drawCircle(Color.White, radius = radioOjo * 0.16f, center = ojo + Offset(-radioOjo * 0.2f, -radioOjo * 0.22f))
                if (parpado > 0f) {
                    drawArc(
                        plumaje,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(ojo.x - radioOjo, ojo.y - radioOjo),
                        size = Size(radioOjo * 2, radioOjo * 2 * parpado)
                    )
                }
                // Gafas redondas de lectura
                drawCircle(Color(0xFF3E2723), radius = radioOjo * 1.15f, center = ojo, style = Stroke(width = r * 0.05f))
            }
            drawLine(
                Color(0xFF3E2723),
                Offset(cabeza.x - separacion + radioOjo * 1.15f, cabeza.y - r * 0.05f),
                Offset(cabeza.x + separacion - radioOjo * 1.15f, cabeza.y - r * 0.05f),
                strokeWidth = r * 0.05f
            )
            // Pico
            val pico = Path().apply {
                moveTo(cabeza.x - r * 0.1f, cabeza.y + r * 0.18f)
                lineTo(cabeza.x + r * 0.1f, cabeza.y + r * 0.18f)
                lineTo(cabeza.x, cabeza.y + r * 0.4f)
                close()
            }
            drawPath(pico, Color(0xFFF2A93B))
        }
    }
}
