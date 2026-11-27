package com.unal.nachoquest.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.sin

/** La tinta roja del Sello Pendiente. */
val TintaSello = Color(0xFFC62828)

/**
 * El Sello Pendiente, el personaje del Acto IV: un sello de caucho de oficina con mango de
 * madera que salta y estampa sin parar. Nació del miedo a que las cosas se acaben, así que deja
 * todo «PENDIENTE». Tiene cara de preocupado más que de malo.
 */
@Composable
fun Sello(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "sello")
    // Sube y baja como si estampara: lento al subir, de golpe al bajar
    val salto by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "salto"
    )
    val fase by transicion.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "fase"
    )

    Canvas(modifier = modifier) {
        val ancho = size.minDimension * 0.62f
        val cx = size.width / 2
        val base = size.height * 0.86f
        val subida = size.minDimension * 0.12f * salto
        val grosor = ancho * 0.035f

        // Las marcas de tinta que va dejando en el piso
        val altoMarca = ancho * 0.12f
        drawRoundRect(
            TintaSello.copy(alpha = 0.25f + 0.35f * (1f - salto)),
            topLeft = Offset(cx - ancho * 0.55f, base),
            size = Size(ancho * 1.1f, altoMarca),
            cornerRadius = CornerRadius(altoMarca / 3),
            style = Stroke(width = grosor)
        )

        // Placa de caucho roja (la cara del personaje)
        val altoPlaca = ancho * 0.48f
        val topPlaca = base - altoPlaca - subida
        drawRoundRect(
            Color(0xFF6D4C41),
            topLeft = Offset(cx - ancho / 2 - grosor, topPlaca - ancho * 0.08f),
            size = Size(ancho + grosor * 2, ancho * 0.12f),
            cornerRadius = CornerRadius(grosor * 2)
        )
        drawRoundRect(
            TintaSello,
            topLeft = Offset(cx - ancho / 2, topPlaca),
            size = Size(ancho, altoPlaca),
            cornerRadius = CornerRadius(ancho * 0.08f)
        )

        // Mango de madera con su perilla
        val anchoMango = ancho * 0.22f
        val altoMango = ancho * 0.5f
        drawRoundRect(
            Color(0xFF8D6E63),
            topLeft = Offset(cx - anchoMango / 2, topPlaca - ancho * 0.08f - altoMango),
            size = Size(anchoMango, altoMango),
            cornerRadius = CornerRadius(anchoMango / 3)
        )
        drawCircle(
            Color(0xFFA1887F),
            radius = ancho * 0.2f,
            center = Offset(cx, topPlaca - ancho * 0.08f - altoMango - ancho * 0.12f)
        )
        drawCircle(
            Color.White.copy(alpha = 0.35f),
            radius = ancho * 0.06f,
            center = Offset(cx - ancho * 0.07f, topPlaca - ancho * 0.08f - altoMango - ancho * 0.18f)
        )

        // Ojos preocupados: cejas inclinadas hacia arriba en el centro
        val yOjos = topPlaca + altoPlaca * 0.42f
        val separacion = ancho * 0.2f
        val radioOjo = ancho * 0.085f
        val mirada = sin(fase) * radioOjo * 0.35f
        listOf(-1f, 1f).forEach { lado ->
            val ojo = Offset(cx + lado * separacion, yOjos)
            drawCircle(Color.White, radius = radioOjo, center = ojo)
            drawCircle(Color(0xFF1B1B1F), radius = radioOjo * 0.5f, center = ojo + Offset(mirada, radioOjo * 0.15f))
            // El extremo de la ceja que da hacia el centro de la cara va más alto: preocupado, no bravo
            drawLine(
                Color(0xFF4A0E0E),
                start = Offset(ojo.x + lado * radioOjo * 1.1f, ojo.y - radioOjo * 1.3f),
                end = Offset(ojo.x - lado * radioOjo * 0.6f, ojo.y - radioOjo * 1.9f),
                strokeWidth = grosor * 1.2f,
                cap = StrokeCap.Round
            )
        }
        // Boca ondulada, nerviosa
        val boca = Path().apply {
            val y = topPlaca + altoPlaca * 0.75f
            moveTo(cx - ancho * 0.16f, y)
            for (i in 1..8) {
                val t = i / 8f
                lineTo(cx - ancho * 0.16f + ancho * 0.32f * t, y + sin(t * PI.toFloat() * 3 + fase) * ancho * 0.025f)
            }
        }
        drawPath(boca, Color.White, style = Stroke(width = grosor, cap = StrokeCap.Round))
    }
}
