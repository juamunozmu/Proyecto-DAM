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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * El Olvido: una bruma gris que ondula, con dos ojos pálidos que se entrecierran.
 * Con [dorado] es el Recuerdo, en lo que se convirtió al final del Acto II: la misma bruma, dorada.
 */
@Composable
fun Olvido(modifier: Modifier = Modifier, dorado: Boolean = false) {
    val colorNube = if (dorado) Color(0xCCD9A93B) else Color(0xCC6E7076)
    val colorCentro = if (dorado) Color(0xEEB8862B) else Color(0xEE4A4C52)
    val colorOjos = if (dorado) Color(0xFFFFF6DC) else Color(0xFFE8F0FF)
    val transicion = rememberInfiniteTransition(label = "olvido")
    val fase by transicion.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "fase"
    )
    val mirada by transicion.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "mirada"
    )

    Canvas(modifier = modifier) {
        val centro = Offset(size.width / 2, size.height / 2)
        val radioBase = size.minDimension * 0.28f

        // Varias "nubes" que giran alrededor del centro, cada una a distinto ritmo
        for (i in 0 until 7) {
            val angulo = fase * (if (i % 2 == 0) 1f else -0.7f) + i * (2 * PI / 7).toFloat()
            val distancia = radioBase * (0.55f + 0.15f * sin(fase * 2 + i))
            val c = Offset(centro.x + cos(angulo) * distancia, centro.y + sin(angulo) * distancia * 0.7f)
            val r = radioBase * (0.75f + 0.12f * cos(fase + i * 1.3f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colorNube, colorNube.copy(alpha = 0f)),
                    center = c,
                    radius = r
                ),
                radius = r,
                center = c
            )
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(colorCentro, colorCentro.copy(alpha = 0f)),
                center = centro,
                radius = radioBase * 1.2f
            ),
            radius = radioBase * 1.2f,
            center = centro
        )

        // Ojos: elipses blancas que se entrecierran lentamente
        val anchoOjo = radioBase * 0.32f
        val altoOjo = radioBase * 0.22f * mirada
        val separacion = radioBase * 0.38f
        val yOjos = centro.y - radioBase * 0.1f + sin(fase) * radioBase * 0.06f
        listOf(-1f, 1f).forEach { lado ->
            drawOval(
                color = colorOjos,
                topLeft = Offset(centro.x + lado * separacion - anchoOjo / 2, yOjos - altoOjo / 2),
                size = Size(anchoOjo, altoOjo)
            )
        }
    }
}
