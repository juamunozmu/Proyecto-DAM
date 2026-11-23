package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.sacudir
import com.unal.nachoquest.ui.theme.EstiloEtiqueta
import com.unal.nachoquest.ui.theme.NachoGreenDark
import kotlinx.coroutines.delay
import kotlin.math.max

private val Marco = Color(0xFF8D6E4A)
private val Acierto = Color(0xFF2E7D32)
private val Fallo = Color(0xFFD32F2F)

/**
 * La copia falsa: arriba está el cuadro original y abajo la copia que pintó el Eco, con cinco
 * cosas cambiadas. Se tocan en la copia; tocar donde no hay nada distinto cuenta como error.
 */
@Composable
fun MinijuegoDiferencias(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val diferencias = remember { ReglasDiferencias.elegir() }
    val alteradas = remember { diferencias.map { it.id }.toSet() }
    val encontradas = remember { mutableStateListOf<Int>() }
    var errores by remember { mutableIntStateOf(0) }
    var ultimoFallo by remember { mutableStateOf<Offset?>(null) }
    var mensaje by remember { mutableStateOf("Toca en la copia cada cosa que cambió.") }
    val terminado = encontradas.size == diferencias.size

    LaunchedEffect(terminado) {
        if (!terminado) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        delay(900)
        alTerminar(ReglasDiferencias.estrellas(errores))
    }

    // Pista: Nacho señala una diferencia que falta
    LaunchedEffect(pistas) {
        if (pistas == 0 || terminado) return@LaunchedEffect
        val diferencia = diferencias.firstOrNull { it.id !in encontradas } ?: return@LaunchedEffect
        encontradas.add(diferencia.id)
        mensaje = "Nacho: «${diferencia.descripcion}»"
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    LaunchedEffect(ultimoFallo) {
        if (ultimoFallo != null) {
            delay(600)
            ultimoFallo = null
        }
    }

    fun tocar(px: Float, py: Float) {
        if (terminado) return
        val pendientes = diferencias.filter { it.id !in encontradas }
        val diferencia = ReglasDiferencias.tocar(px, py, pendientes)
        if (diferencia != null) {
            encontradas.add(diferencia.id)
            mensaje = "¡Bien visto! ${diferencia.descripcion}."
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            Sonidos.reproducir(Efecto.ACIERTO)
        } else if (diferencias.none { it.id in encontradas && it.contiene(px, py, 0.03f) }) {
            // Tocar otra vez una diferencia ya encontrada no cuenta como error
            errores++
            ultimoFallo = Offset(px, py)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Sonidos.reproducir(Efecto.ERROR)
        }
    }

    val marcas = diferencias.filter { it.id in encontradas }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Diferencias: ${encontradas.size}/${diferencias.size}")
            Marcador("Errores: $errores")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("ORIGINAL", style = EstiloEtiqueta, color = Color.Gray)
        Cuadro(
            alteradas = emptySet(),
            marcas = marcas,
            fallo = null,
            modifier = Modifier
                .weight(1f, fill = false)
                .aspectRatio(4f / 3f, matchHeightConstraintsFirst = true)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("COPIA DEL ECO", style = EstiloEtiqueta, color = Fallo)
        Cuadro(
            alteradas = alteradas,
            marcas = marcas,
            fallo = ultimoFallo,
            modifier = Modifier
                .weight(1f, fill = false)
                .aspectRatio(4f / 3f, matchHeightConstraintsFirst = true)
                .sacudir(errores)
                .pointerInput(Unit) {
                    detectTapGestures { toque -> tocar(toque.x / size.width, toque.y / size.height) }
                }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (terminado) "¡El cuadro recuperó sus colores!" else mensaje,
            color = if (terminado) NachoGreenDark else Color.DarkGray,
            fontWeight = if (terminado) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = if (terminado) 17.sp else 13.sp
        )
    }
}

@Composable
private fun Cuadro(
    alteradas: Set<Int>,
    marcas: List<Diferencia>,
    fallo: Offset?,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(5.dp, Marco, RoundedCornerShape(6.dp))
    ) {
        dibujarCuadro(alteradas)
        val w = size.width
        val h = size.height
        marcas.forEach { d ->
            val radio = max(d.ancho * w, d.alto * h) / 2 + 6.dp.toPx()
            drawCircle(Acierto, radio, Offset(d.centroX * w, d.centroY * h), style = Stroke(3.dp.toPx()))
        }
        fallo?.let {
            val c = Offset(it.x * w, it.y * h)
            val l = 10.dp.toPx()
            drawLine(Fallo, Offset(c.x - l, c.y - l), Offset(c.x + l, c.y + l), 4.dp.toPx())
            drawLine(Fallo, Offset(c.x - l, c.y + l), Offset(c.x + l, c.y - l), 4.dp.toPx())
        }
    }
}

/**
 * Paisaje de la sabana al estilo naíf: sol, nube, pájaro, cerros, una casa, un árbol con frutas
 * y flores. [alteradas] dice qué cambia en la copia del Eco (ids de [ReglasDiferencias]).
 */
private fun DrawScope.dibujarCuadro(alteradas: Set<Int>) {
    val w = size.width
    val h = size.height
    fun p(x: Float, y: Float) = Offset(x * w, y * h)

    // Cielo y pasto
    drawRect(Brush.verticalGradient(listOf(Color(0xFF7EC8F2), Color(0xFFDDF1FB)), endY = h * 0.62f), size = Size(w, h * 0.62f))
    drawRect(Color(0xFF7DB45A), topLeft = p(0f, 0.62f), size = Size(w, h * 0.38f))

    // Cerros
    val cerros = Path().apply {
        moveTo(0f, h * 0.62f)
        lineTo(0f, h * 0.5f)
        lineTo(w * 0.18f, h * 0.4f)
        lineTo(w * 0.4f, h * 0.52f)
        lineTo(w * 0.62f, h * 0.38f)
        lineTo(w * 0.85f, h * 0.48f)
        lineTo(w, h * 0.42f)
        lineTo(w, h * 0.62f)
        close()
    }
    drawPath(cerros, Color(0xFF4F8A3E))

    // Sol
    if (ReglasDiferencias.SOL !in alteradas) {
        drawCircle(Color(0x55FFD54F), radius = w * 0.095f, center = p(0.16f, 0.16f))
        drawCircle(Color(0xFFFFC928), radius = w * 0.065f, center = p(0.16f, 0.16f))
    }

    // Nube fija a la derecha y nube de más en la copia
    fun nube(cx: Float, cy: Float) {
        drawOval(Color.White, p(cx - 0.09f, cy - 0.035f), Size(w * 0.18f, h * 0.07f))
        drawOval(Color.White, p(cx - 0.045f, cy - 0.07f), Size(w * 0.09f, h * 0.08f))
    }
    nube(0.88f, 0.24f)
    if (ReglasDiferencias.NUBE in alteradas) nube(0.66f, 0.13f)

    // Pájaro
    if (ReglasDiferencias.PAJARO !in alteradas) {
        val trazo = Stroke(w * 0.008f)
        val ala = Path().apply {
            moveTo(w * 0.38f, h * 0.17f)
            quadraticBezierTo(w * 0.405f, h * 0.14f, w * 0.425f, h * 0.175f)
            quadraticBezierTo(w * 0.445f, h * 0.14f, w * 0.47f, h * 0.17f)
        }
        drawPath(ala, Color(0xFF263238), style = trazo)
    }

    // Árbol con frutas
    drawRect(Color(0xFF6D4C41), topLeft = p(0.19f, 0.5f), size = Size(w * 0.04f, h * 0.2f))
    drawCircle(Color(0xFF2E7D32), radius = w * 0.11f, center = p(0.21f, 0.45f))
    if (ReglasDiferencias.FRUTAS !in alteradas) {
        listOf(0.16f to 0.4f, 0.25f to 0.38f, 0.2f to 0.49f, 0.28f to 0.5f, 0.14f to 0.52f).forEach { (x, y) ->
            drawCircle(Color(0xFFFF9800), radius = w * 0.017f, center = p(x, y))
        }
    }

    // Casa
    val techo = Path().apply {
        moveTo(w * 0.51f, h * 0.51f)
        lineTo(w * 0.83f, h * 0.51f)
        lineTo(w * 0.67f, h * 0.37f)
        close()
    }
    drawPath(techo, if (ReglasDiferencias.TECHO in alteradas) Color(0xFF1E5AA8) else Color(0xFFC62828))
    drawRect(Color(0xFFF7F3EA), topLeft = p(0.54f, 0.51f), size = Size(w * 0.26f, h * 0.25f))
    if (ReglasDiferencias.VENTANA !in alteradas) {
        drawRect(Color(0xFF90CAF9), topLeft = p(0.58f, 0.55f), size = Size(w * 0.08f, h * 0.08f))
        drawLine(Color.White, p(0.62f, 0.55f), p(0.62f, 0.63f), w * 0.005f)
        drawLine(Color.White, p(0.58f, 0.59f), p(0.66f, 0.59f), w * 0.005f)
    }
    drawRect(
        if (ReglasDiferencias.PUERTA in alteradas) Color(0xFF2E7D32) else Color(0xFF795548),
        topLeft = p(0.71f, 0.6f),
        size = Size(w * 0.07f, h * 0.16f)
    )

    // Flores (la de la derecha es la que cambia)
    val colores = listOf(Color(0xFFE91E63), Color(0xFFFFEB3B), Color(0xFFF44336), Color(0xFFE91E63), Color(0xFFFFEB3B))
    listOf(0.1f, 0.26f, 0.42f, 0.58f, 0.74f).forEachIndexed { i, x -> flor(p(x, 0.88f), colores[i], w) }
    flor(p(0.89f, 0.88f), if (ReglasDiferencias.FLOR in alteradas) Color(0xFF7B1FA2) else Color(0xFFFFEB3B), w)
}

private fun DrawScope.flor(centro: Offset, color: Color, w: Float) {
    drawLine(Color(0xFF33691E), centro, Offset(centro.x, centro.y + w * 0.05f), w * 0.006f)
    val r = w * 0.016f
    listOf(-1f to 0f, 1f to 0f, 0f to -1f, 0f to 1f).forEach { (dx, dy) ->
        drawCircle(color, r, Offset(centro.x + dx * r, centro.y + dy * r))
    }
    drawCircle(Color(0xFF6D4C41), r * 0.7f, centro)
}
