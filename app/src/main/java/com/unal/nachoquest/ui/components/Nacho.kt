package com.unal.nachoquest.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.unal.nachoquest.domain.model.Expresion
import kotlinx.coroutines.delay
import kotlin.random.Random

/*
 * Nacho, la mascota de NachoQuest, dibujado como pixel art (igual que en los mockups).
 * Cada carácter del sprite es un píxel; las filas de ojos y boca se reemplazan según la expresión.
 */

private const val ANCHO_SPRITE = 14

private val SPRITE_BASE = listOf(
    "...HHHHHHHH...",
    "..HHHHHHHHHH..",
    "..SSSSSSSSSS..",
    "..SSSSSSSSSS..",
    "..SSEESSEESS..", // 4: ojos (fila superior)
    "..SSEESSEESS..", // 5: ojos (fila inferior)
    "..SSSSSSSSSS..",
    "..SSSSSSSSSS..", // 7: boca
    "..SSSSSSSSSS..", // 8: boca
    "...BBBBBBBB...",
    ".ABBBBBBBBBBA.",
    ".ABBBBBBBBBBA.",
    ".ABBBBBBBBBBKK",
    ".SBBBBBBBBBBKK",
    "..BBBBBBBBBBKK",
    "...LLL..LLL...",
    "...LLL..LLL...",
    "..DDDD..DDDD.."
)

private val COLORES = mapOf(
    'H' to Color(0xFFC8742A), // cabello
    'S' to Color(0xFF8B4A2B), // piel
    'E' to Color(0xFF1B1B1B), // ojos
    'M' to Color(0xFF3B1A0C), // boca
    'C' to Color(0xFF5A2E18), // cejas
    'R' to Color(0xFFD9776A), // mejillas
    'B' to Color(0xFF2D5016), // camiseta UNAL
    'A' to Color(0xFF244012), // brazos
    'K' to Color(0xFF2F6FDE), // libro
    'L' to Color(0xFF1F2A44), // pantalón
    'D' to Color(0xFF111111)  // zapatos
)

private fun spriteConExpresion(expresion: Expresion, parpadeando: Boolean, bocaAbierta: Boolean): List<String> {
    val filas = SPRITE_BASE.toMutableList()

    fun pintar(fila: Int, columnas: List<Int>, c: Char) {
        val chars = filas[fila].toCharArray()
        columnas.forEach { chars[it] = c }
        filas[fila] = String(chars)
    }

    // Ojos
    when {
        parpadeando -> {
            pintar(4, listOf(4, 5, 8, 9), 'S')
        }
        expresion == Expresion.FELIZ -> {
            // Mejillas sonrosadas
            pintar(6, listOf(3, 10), 'R')
        }
        expresion == Expresion.SORPRENDIDO -> {
            pintar(3, listOf(4, 5, 8, 9), 'E')
        }
        expresion == Expresion.PREOCUPADO -> {
            // Cejas con el extremo interior levantado
            pintar(2, listOf(5, 8), 'C')
            pintar(3, listOf(4, 9), 'C')
        }
        expresion == Expresion.DECIDIDO -> {
            // Cejas fruncidas y ojos entrecerrados
            pintar(2, listOf(4, 9), 'C')
            pintar(3, listOf(5, 8), 'C')
            pintar(4, listOf(4, 5, 8, 9), 'S')
        }
    }

    // Boca
    when {
        bocaAbierta -> {
            pintar(7, listOf(6, 7), 'M')
            pintar(8, listOf(6, 7), 'M')
        }
        expresion == Expresion.FELIZ -> {
            pintar(7, listOf(4, 9), 'M')
            pintar(8, listOf(5, 6, 7, 8), 'M')
        }
        expresion == Expresion.SORPRENDIDO -> {
            pintar(7, listOf(6, 7), 'M')
            pintar(8, listOf(6, 7), 'M')
        }
        expresion == Expresion.PREOCUPADO -> {
            pintar(8, listOf(4, 9), 'M')
            pintar(7, listOf(5, 6, 7, 8), 'M')
        }
        else -> pintar(8, listOf(5, 6, 7, 8), 'M')
    }

    return filas
}

/**
 * Nacho animado: flota suavemente, parpadea cada pocos segundos y mueve la boca mientras habla.
 *
 * @param hablando si es true, la boca se abre y cierra (para acompañar texto que se está escribiendo).
 * @param saltando si es true, rebota con energía (celebraciones).
 */
@Composable
fun Nacho(
    modifier: Modifier = Modifier,
    expresion: Expresion = Expresion.NORMAL,
    hablando: Boolean = false,
    saltando: Boolean = false
) {
    val transicion = rememberInfiniteTransition(label = "nacho")
    val flotar by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (saltando) 380 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flotar"
    )

    var parpadeando by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2200, 4200))
            parpadeando = true
            delay(130)
            parpadeando = false
        }
    }

    var bocaAbierta by remember { mutableStateOf(false) }
    LaunchedEffect(hablando) {
        bocaAbierta = false
        while (hablando) {
            delay(110)
            bocaAbierta = !bocaAbierta
        }
        bocaAbierta = false
    }

    val sprite = spriteConExpresion(expresion, parpadeando, bocaAbierta)

    Canvas(
        modifier = modifier.graphicsLayer {
            val amplitud = if (saltando) 0.16f else 0.04f
            translationY = -size.height * amplitud * flotar
            // Al saltar se "aplasta" un poco al tocar el suelo
            if (saltando) {
                scaleY = 0.94f + 0.06f * flotar
                scaleX = 1.04f - 0.04f * flotar
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
            }
        }
    ) {
        dibujarSprite(sprite)
    }
}

private fun DrawScope.dibujarSprite(sprite: List<String>) {
    val pixel = minOf(size.width / ANCHO_SPRITE, size.height / sprite.size)
    val origenX = (size.width - pixel * ANCHO_SPRITE) / 2f
    val origenY = (size.height - pixel * sprite.size) / 2f
    // Se agranda medio píxel para que no queden líneas entre celdas
    val celda = Size(pixel + 0.5f, pixel + 0.5f)

    sprite.forEachIndexed { y, fila ->
        fila.forEachIndexed { x, c ->
            val color = COLORES[c] ?: return@forEachIndexed
            drawRect(color, Offset(origenX + x * pixel, origenY + y * pixel), celda)
        }
    }

    // Letras "UN" en la camiseta
    val paint = android.graphics.Paint().apply {
        color = Color.White.toArgb()
        textSize = pixel * 2.6f
        isFakeBoldText = true
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(
        "UN",
        origenX + pixel * 7f,
        origenY + pixel * 12.9f,
        paint
    )
}
