package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.sacudir
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private const val TICK_MS = 100L
private val ESPACIO = 6.dp
private val LADO_MAXIMO = 60.dp

/**
 * Completa la hélice (capítulo 11): tres cadenas de ADN cada vez más largas. Si el tiempo se
 * acaba, la cadena se desenrolla: suma errores y se empieza otra del mismo largo.
 */
@Composable
fun MinijuegoHelice(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var ronda by remember { mutableIntStateOf(0) }
    val largo = ReglasHelice.LARGOS[ronda]
    val totalMs = ReglasHelice.SEGUNDOS[ronda] * 1000L
    // Sube cada vez que una cadena se desenrolla, para empezar otra del mismo largo
    var intento by remember(ronda) { mutableIntStateOf(0) }
    val cadena = remember(ronda, intento) { ReglasHelice.cadena(largo) }
    var colocadas by remember(ronda, intento) { mutableIntStateOf(0) }
    var restanteMs by remember(ronda, intento) { mutableLongStateOf(totalMs) }
    var errores by remember { mutableIntStateOf(0) }
    var sacudidas by remember { mutableIntStateOf(0) }
    var desenrollando by remember { mutableStateOf(false) }
    var terminado by remember { mutableStateOf(false) }
    val completa = colocadas == largo

    // El reloj corre mientras la cadena no esté completa
    LaunchedEffect(ronda, intento, completa) {
        if (completa) return@LaunchedEffect
        while (restanteMs > 0) {
            delay(TICK_MS)
            restanteMs -= TICK_MS
        }
        errores += ReglasHelice.ERRORES_POR_DESENROLLAR
        desenrollando = true
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.ERROR)
        delay(1300)
        desenrollando = false
        intento++
    }

    LaunchedEffect(ronda, completa) {
        if (!completa) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.ACIERTO)
        delay(1100)
        if (ronda < ReglasHelice.LARGOS.lastIndex) {
            ronda++
        } else if (!terminado) {
            terminado = true
            alTerminar(ReglasHelice.estrellas(errores))
        }
    }

    // Pista: Nacho pone la base que sigue
    LaunchedEffect(pistas) {
        if (pistas == 0 || terminado || completa || desenrollando) return@LaunchedEffect
        colocadas++
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    fun elegir(base: Base) {
        if (completa || desenrollando || terminado) return
        if (ReglasHelice.esPareja(cadena[colocadas], base)) {
            colocadas++
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            Sonidos.reproducir(Efecto.TOQUE)
        } else {
            errores++
            sacudidas++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Sonidos.reproducir(Efecto.ERROR)
        }
    }

    val transicion = rememberInfiniteTransition(label = "helice")
    val fase by transicion.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "fase"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Cadena ${ronda + 1}/${ReglasHelice.LARGOS.size}")
            Marcador("Errores: $errores")
        }
        Spacer(modifier = Modifier.height(10.dp))
        BarraDesenrollo(fraccion = restanteMs / totalMs.toFloat(), segundos = (restanteMs / 1000).toInt())
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when {
                desenrollando -> "🌀 ¡Se desenrolló! Va otra cadena..."
                completa -> "🧬 ¡Hélice completa!"
                else -> "Ponle su pareja a cada letra de arriba"
            },
            fontWeight = FontWeight.Bold,
            color = if (desenrollando) Color(0xFFD32F2F) else NachoGreenDark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        // La hélice cabe a lo ancho y a lo alto (tabletas en horizontal)
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            contentAlignment = Alignment.Center
        ) {
            val lado: Dp = minOf((maxWidth - ESPACIO * (largo - 1)) / largo, LADO_MAXIMO, maxHeight / 3.4f)
            val amplitud = with(androidx.compose.ui.platform.LocalDensity.current) { (lado * 0.18f).toPx() }
            Row(horizontalArrangement = Arrangement.spacedBy(ESPACIO)) {
                cadena.forEachIndexed { i, base ->
                    val ondulacion = if (desenrollando) 0f else sin(fase + i * 0.75f) * amplitud
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset { IntOffset(0, ondulacion.roundToInt()) }
                    ) {
                        FichaBase(base, lado)
                        // Peldaño que une las dos bases
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(lado * 0.9f)
                                .background(if (i < colocadas) Color(0xFF90A4AE) else Color(0x3390A4AE))
                        )
                        val pareja = ReglasHelice.complemento(base)
                        when {
                            i < colocadas -> FichaBase(pareja, lado)
                            i == colocadas && !desenrollando -> Box(
                                modifier = Modifier
                                    .size(lado)
                                    .sacudir(sacudidas)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(3.dp, NachoYellow, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("?", fontWeight = FontWeight.ExtraBold, color = NachoYellow, fontSize = 18.sp)
                            }
                            else -> Box(
                                modifier = Modifier
                                    .size(lado)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, Color(0x55908A70), RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("A ↔ T     C ↔ G", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Base.entries.forEach { base ->
                BotonJuego(
                    texto = base.name,
                    onClick = { elegir(base) },
                    color = base.color,
                    colorTexto = if (base == Base.T) NachoGreenDark else Color.White,
                    habilitado = !completa && !desenrollando,
                    alto = 56.dp,
                    tamanoTexto = 22.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FichaBase(base: Base, lado: Dp) {
    Box(
        modifier = Modifier
            .size(lado)
            .clip(RoundedCornerShape(8.dp))
            .background(base.color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            base.name,
            fontWeight = FontWeight.ExtraBold,
            fontSize = (lado.value * 0.45f).sp,
            color = if (base == Base.T) NachoGreenDark else Color.White
        )
    }
}

/** Tiempo que le queda a la hélice antes de desenrollarse. */
@Composable
private fun BarraDesenrollo(fraccion: Float, segundos: Int) {
    val color = when {
        fraccion > 0.5f -> Color(0xFF43A047)
        fraccion > 0.25f -> NachoYellow
        else -> Color(0xFFD32F2F)
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
        Text("🧬", fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.LightGray.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text("${segundos}s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.width(30.dp))
    }
}
