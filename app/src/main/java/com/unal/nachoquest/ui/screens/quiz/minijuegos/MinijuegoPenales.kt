package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Olvido
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.sacudir
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private enum class FaseTiro { APUNTANDO, VOLANDO, GOL, ATAJADA }

// Geometría de la cancha, en fracciones del ancho y del alto del dibujo
private const val ARCO_IZQ = 0.10f
private const val ARCO_DER = 0.90f
private const val ARCO_ARRIBA = 0.08f
private const val LINEA_DE_GOL = 0.36f
private const val Y_MIRA = 0.23f
private const val Y_PUNTO_PENAL = 0.86f
private const val LADO_ARQUERO = 0.44f   // tamaño del Olvido respecto al ancho de la cancha

private val Pasto = Color(0xFF3E8E3A)
private val PastoClaro = Color(0xFF4A9C45)
private val Graderia = Color(0xFF93A0B0)

/** Convierte una posición en el arco (0 a 1) en una fracción del ancho de la cancha. */
private fun xEnCancha(posicionEnArco: Float): Float = ARCO_IZQ + (ARCO_DER - ARCO_IZQ) * posicionEnArco

/**
 * Tanda de penales: el Olvido ataja, la mira va y viene sobre el arco y el jugador decide cuándo
 * patear. Gana al meter tres goles; mientras menos tiros necesite, más estrellas.
 */
@Composable
fun MinijuegoPenales(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var goles by remember { mutableIntStateOf(0) }
    var tiros by remember { mutableIntStateOf(0) }
    var atajadas by remember { mutableIntStateOf(0) }
    var fase by remember { mutableStateOf(FaseTiro.APUNTANDO) }
    var xTiro by remember { mutableFloatStateOf(0.5f) }
    val vuelo = remember { Animatable(0f) }

    // Fases del vaivén. Solo se leen al ubicar y dibujar, para no recomponer en cada cuadro
    val faseMira = remember { mutableFloatStateOf(0f) }
    val faseArquero = remember { mutableFloatStateOf(PI.toFloat()) }

    val ganado = goles >= ReglasPenales.GOLES_PARA_GANAR

    // Pista: cámara lenta para el próximo tiro (la mira y el arquero se mueven más despacio)
    var camaraLenta by remember { mutableStateOf(false) }
    LaunchedEffect(pistas) {
        if (pistas > 0 && !ganado) camaraLenta = true
    }

    LaunchedEffect(Unit) {
        var anterior = 0L
        while (true) {
            withFrameNanos { ahora ->
                if (anterior != 0L) {
                    // Si la app estuvo en pausa, el salto de tiempo no debe teletransportar al arquero
                    val dt = ((ahora - anterior) / 1_000_000_000f).coerceAtMost(0.05f) *
                        if (camaraLenta) ReglasPenales.FACTOR_CAMARA_LENTA else 1f
                    if (fase == FaseTiro.APUNTANDO) {
                        faseMira.floatValue += ReglasPenales.velocidadMira(goles) * dt
                    }
                    // El arquero sigue moviéndose mientras el balón va en el aire
                    if (fase == FaseTiro.APUNTANDO || fase == FaseTiro.VOLANDO) {
                        faseArquero.floatValue += ReglasPenales.velocidadArquero(goles) * dt
                    }
                }
                anterior = ahora
            }
        }
    }

    fun patear() {
        if (fase != FaseTiro.APUNTANDO || ganado) return
        xTiro = ReglasPenales.posicion(faseMira.floatValue, ReglasPenales.AMPLITUD_MIRA)
        tiros++
        fase = FaseTiro.VOLANDO
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        scope.launch {
            vuelo.snapTo(0f)
            vuelo.animateTo(1f, tween(ReglasPenales.DURACION_VUELO_MS, easing = LinearEasing))
            val xArquero = ReglasPenales.posicion(faseArquero.floatValue, ReglasPenales.AMPLITUD_ARQUERO)
            if (ReglasPenales.esGol(xTiro, xArquero)) {
                goles++
                fase = FaseTiro.GOL
                Sonidos.reproducir(Efecto.GOL)
            } else {
                atajadas++
                fase = FaseTiro.ATAJADA
                Sonidos.reproducir(Efecto.ATAJADA)
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            camaraLenta = false
            delay(1100)
            if (goles >= ReglasPenales.GOLES_PARA_GANAR) {
                alTerminar(ReglasPenales.estrellas(tiros))
            } else {
                vuelo.snapTo(0f)
                fase = FaseTiro.APUNTANDO
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Goles: $goles/${ReglasPenales.GOLES_PARA_GANAR}")
            Marcador("Tiros: $tiros")
            if (camaraLenta) Marcador("🐢 Cámara lenta")
        }
        Spacer(modifier = Modifier.height(12.dp))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .sacudir(atajadas)
                .clip(RoundedCornerShape(18.dp))
                .background(Pasto)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = ::patear
                )
        ) {
            val anchoPx = constraints.maxWidth.toFloat()
            val altoPx = constraints.maxHeight.toFloat()
            val ladoArquero = maxWidth * LADO_ARQUERO

            Canvas(modifier = Modifier.fillMaxSize()) {
                dibujarCancha()
                val origen = Offset(size.width * 0.5f, size.height * Y_PUNTO_PENAL)

                if (fase == FaseTiro.APUNTANDO) {
                    val mira = Offset(
                        size.width * xEnCancha(ReglasPenales.posicion(faseMira.floatValue, ReglasPenales.AMPLITUD_MIRA)),
                        size.height * Y_MIRA
                    )
                    dibujarMira(origen, mira)
                }

                // Balón: va del punto penal al lugar donde estaba la mira, con un poco de altura
                val avance = vuelo.value
                val destino = Offset(size.width * xEnCancha(xTiro), size.height * Y_MIRA)
                val centro = Offset(
                    x = origen.x + (destino.x - origen.x) * avance,
                    y = origen.y + (destino.y - origen.y) * avance - sin(avance * PI.toFloat()) * size.height * 0.05f
                )
                dibujarBalon(centro, radio = size.width * (0.05f - 0.02f * avance))
            }

            // El Olvido, de arquero. Se desvanece cuando pierde la tanda
            val presenciaArquero by animateFloatAsState(if (ganado) 0f else 1f, tween(900), label = "arquero")
            Olvido(
                modifier = Modifier
                    .offset {
                        val x = xEnCancha(ReglasPenales.posicion(faseArquero.floatValue, ReglasPenales.AMPLITUD_ARQUERO))
                        val lado = ladoArquero.toPx()
                        IntOffset((anchoPx * x - lado / 2).roundToInt(), (altoPx * Y_MIRA - lado / 2).roundToInt())
                    }
                    .size(ladoArquero)
                    .graphicsLayer { alpha = presenciaArquero }
            )

            // Con el nombre completo: aquí dentro hay un ColumnScope y un BoxScope a la vez
            androidx.compose.animation.AnimatedVisibility(
                visible = fase == FaseTiro.GOL || fase == FaseTiro.ATAJADA,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                // El texto no cambia mientras se desvanece, aunque la fase ya sea otra
                val fueGol = remember { fase == FaseTiro.GOL }
                Text(
                    text = if (fueGol) "¡GOOOL!" else "¡Atajada!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (fueGol) NachoYellow else Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(NachoGreenDark.copy(alpha = 0.85f))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        BotonJuego(
            texto = if (ganado) "¡El estadio vuelve a rugir!" else "⚽  ¡Patear!",
            onClick = ::patear,
            color = NachoYellow,
            colorTexto = NachoGreenDark,
            habilitado = fase == FaseTiro.APUNTANDO && !ganado,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

private fun DrawScope.dibujarCancha() {
    val ancho = size.width
    val alto = size.height
    val lineaGol = alto * LINEA_DE_GOL

    // Gradería detrás del arco, con el público
    drawRect(Graderia, size = Size(ancho, lineaGol))
    val coloresPublico = listOf(Color(0xFFE5C04A), Color(0xFFFFFFFF), Color(0xFF8BC34A), Color(0xFFEF5350), Color(0xFF64B5F6))
    val filas = 6
    val columnas = 22
    for (fila in 0 until filas) {
        for (col in 0 until columnas) {
            // Patrón fijo (no aleatorio) para que el público no parpadee al redibujar
            val color = coloresPublico[(fila * 7 + col * 3 + (fila * col) % 4) % coloresPublico.size]
            val desfase = if (fila % 2 == 0) 0f else 0.5f
            drawCircle(
                color = color.copy(alpha = 0.9f),
                radius = ancho * 0.011f,
                center = Offset(ancho * (col + 0.5f + desfase) / columnas, lineaGol * (0.08f + fila * 0.1f))
            )
        }
    }

    // Césped en franjas
    val franjas = 7
    val altoFranja = (alto - lineaGol) / franjas
    for (i in 0 until franjas) {
        drawRect(
            color = if (i % 2 == 0) Pasto else PastoClaro,
            topLeft = Offset(0f, lineaGol + i * altoFranja),
            size = Size(ancho, altoFranja + 1f)
        )
    }

    // Líneas de la cancha: línea de fondo, área chica y punto penal
    val trazo = ancho * 0.008f
    val blanco = Color.White.copy(alpha = 0.85f)
    drawLine(blanco, Offset(0f, lineaGol), Offset(ancho, lineaGol), strokeWidth = trazo)
    drawRect(
        color = blanco,
        topLeft = Offset(ancho * 0.22f, lineaGol),
        size = Size(ancho * 0.56f, alto * 0.17f),
        style = Stroke(width = trazo)
    )
    drawCircle(blanco, radius = ancho * 0.012f, center = Offset(ancho * 0.5f, alto * Y_PUNTO_PENAL))

    // Arco: red y palos
    val izquierda = ancho * ARCO_IZQ
    val derecha = ancho * ARCO_DER
    val arriba = alto * ARCO_ARRIBA
    drawRect(Color(0xFF1F2A36).copy(alpha = 0.22f), Offset(izquierda, arriba), Size(derecha - izquierda, lineaGol - arriba))
    val colorRed = Color.White.copy(alpha = 0.55f)
    val hilosVerticales = 16
    for (i in 1 until hilosVerticales) {
        val x = izquierda + (derecha - izquierda) * i / hilosVerticales
        drawLine(colorRed, Offset(x, arriba), Offset(x, lineaGol), strokeWidth = trazo * 0.4f)
    }
    val hilosHorizontales = 7
    for (i in 1 until hilosHorizontales) {
        val y = arriba + (lineaGol - arriba) * i / hilosHorizontales
        drawLine(colorRed, Offset(izquierda, y), Offset(derecha, y), strokeWidth = trazo * 0.4f)
    }
    // Cada palo se traza dos veces: un contorno oscuro y encima el blanco
    val palo = ancho * 0.02f
    listOf(Color(0xFF37474F) to palo * 1.5f, Color.White to palo).forEach { (color, grosor) ->
        drawLine(color, Offset(izquierda, arriba), Offset(derecha, arriba), strokeWidth = grosor)
        drawLine(color, Offset(izquierda, arriba - grosor / 2), Offset(izquierda, lineaGol), strokeWidth = grosor)
        drawLine(color, Offset(derecha, arriba - grosor / 2), Offset(derecha, lineaGol), strokeWidth = grosor)
    }
}

private fun DrawScope.dibujarMira(origen: Offset, mira: Offset) {
    // Trayectoria que seguiría el balón
    drawLine(
        color = Color.White.copy(alpha = 0.35f),
        start = origen,
        end = mira,
        strokeWidth = size.width * 0.006f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(size.width * 0.02f, size.width * 0.02f))
    )
    val radio = size.width * 0.055f
    drawCircle(NachoGreenDark.copy(alpha = 0.35f), radius = radio * 1.15f, center = mira)
    drawCircle(NachoYellow, radius = radio, center = mira, style = Stroke(width = radio * 0.22f))
    drawCircle(NachoYellow, radius = radio * 0.18f, center = mira)
    listOf(Offset(1f, 0f), Offset(-1f, 0f), Offset(0f, 1f), Offset(0f, -1f)).forEach { direccion ->
        drawLine(
            color = NachoYellow,
            start = Offset(mira.x + direccion.x * radio * 0.6f, mira.y + direccion.y * radio * 0.6f),
            end = Offset(mira.x + direccion.x * radio * 1.45f, mira.y + direccion.y * radio * 1.45f),
            strokeWidth = radio * 0.2f
        )
    }
}

private fun DrawScope.dibujarBalon(centro: Offset, radio: Float) {
    // Sombra sobre el césped
    drawOval(
        color = Color.Black.copy(alpha = 0.22f),
        topLeft = Offset(centro.x - radio, centro.y + radio * 0.75f),
        size = Size(radio * 2f, radio * 0.6f)
    )
    drawCircle(Color.White, radius = radio, center = centro)
    drawCircle(Color(0xFF263238), radius = radio, center = centro, style = Stroke(width = radio * 0.14f))
    // Parches del balón
    drawCircle(Color(0xFF263238), radius = radio * 0.3f, center = centro)
    for (i in 0 until 5) {
        val angulo = (i * 72f - 90f) * PI.toFloat() / 180f
        val parche = Offset(centro.x + kotlin.math.cos(angulo) * radio * 0.78f, centro.y + sin(angulo) * radio * 0.78f)
        drawCircle(Color(0xFF263238), radius = radio * 0.17f, center = parche)
    }
}
