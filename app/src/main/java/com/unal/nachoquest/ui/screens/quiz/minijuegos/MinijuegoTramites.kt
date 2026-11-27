package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.TintaSello
import com.unal.nachoquest.ui.theme.NachoAzul
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

private val ColorPapel = Color(0xFFFFFDF5)

/** Cómo terminó el papel que se está mostrando. */
private enum class FinPapel { BIEN, MAL, SELLADO }

/**
 * Cada papel a su bandeja (capítulo 14): veinte trámites, cada uno con menos tiempo que el
 * anterior. Si el tiempo se acaba, el Sello le estampa «PENDIENTE».
 */
@Composable
fun MinijuegoTramites(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val papeles = remember { ReglasTramites.papeles() }
    var indice by remember { mutableIntStateOf(0) }
    var errores by remember { mutableIntStateOf(0) }
    var fin by remember { mutableStateOf<FinPapel?>(null) }
    var terminado by remember { mutableStateOf(false) }
    // El reloj no corre hasta que el jugador abre la ventanilla: primero se leen las instrucciones
    var abierta by remember { mutableStateOf(false) }
    val tiempo = remember { Animatable(1f) }
    val papel = papeles.getOrNull(indice)

    // El reloj de cada papel: si llega a cero, el Sello lo marca como pendiente
    LaunchedEffect(indice, abierta) {
        if (!abierta || indice >= papeles.size) return@LaunchedEffect
        fin = null
        tiempo.snapTo(1f)
        tiempo.animateTo(
            0f,
            tween((ReglasTramites.segundosPara(indice) * 1000).toInt(), easing = LinearEasing)
        )
        if (fin == null) {
            fin = FinPapel.SELLADO
            errores++
            Sonidos.reproducir(Efecto.ERROR)
        }
    }

    // Después de cada papel, una pausa corta para ver cómo quedó y pasar al siguiente
    LaunchedEffect(fin) {
        if (fin == null) return@LaunchedEffect
        delay(if (fin == FinPapel.BIEN) 250 else 700)
        if (indice < papeles.lastIndex) {
            indice++
        } else if (!terminado) {
            terminado = true
            Sonidos.reproducir(Efecto.VICTORIA)
            alTerminar(ReglasTramites.estrellas(errores))
        }
    }

    fun archivar(bandeja: TipoTramite, ayuda: Boolean = false) {
        val actual = papel ?: return
        if (!abierta || fin != null || terminado) return
        if (ReglasTramites.esCorrecta(actual, bandeja)) {
            fin = FinPapel.BIEN
            Sonidos.reproducir(if (ayuda) Efecto.ACIERTO else Efecto.TOQUE)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } else {
            fin = FinPapel.MAL
            errores++
            Sonidos.reproducir(Efecto.ERROR)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Pista: Nacho archiva el papel actual en su bandeja (no cuenta como error ni como acierto)
    LaunchedEffect(pistas) {
        if (pistas == 0) return@LaunchedEffect
        papel?.let { archivar(it, ayuda = true) }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Papel ${minOf(indice + 1, papeles.size)}/${papeles.size}")
            Marcador("Errores: $errores", color = if (errores > 0) TintaSello else NachoGreenDark)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = when (fin) {
                FinPapel.BIEN -> "✅ ¡Archivado!"
                FinPapel.MAL -> "❌ Esa no era su bandeja"
                FinPapel.SELLADO -> "🔴 ¡El Sello lo dejó PENDIENTE!"
                null -> if (abierta) "¿A qué bandeja va este papel?" else "Cuando estés listo, abre la ventanilla"
            },
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = NachoGreenDark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        // El papel ocupa el espacio que sobra y se encoge si no cabe (tabletas en horizontal)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            if (!abierta) {
                BotonJuego(
                    texto = "Abrir la ventanilla",
                    prefijo = "🗂️",
                    onClick = { abierta = true },
                    color = NachoGreen
                )
            } else if (papel != null) {
                Papel(papel, tiempo.value, fin)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        val colores = listOf(NachoAzul, NachoGreen, Color(0xFF8D6E63), Color(0xFF6A1B9A))
        TipoTramite.entries.chunked(2).forEachIndexed { fila, par ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                par.forEachIndexed { columna, bandeja ->
                    BotonJuego(
                        texto = bandeja.bandeja,
                        prefijo = bandeja.emoji,
                        onClick = { archivar(bandeja) },
                        color = colores[fila * 2 + columna],
                        habilitado = abierta && fin == null && !terminado,
                        alto = 60.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun Papel(papel: TipoTramite, tiempo: Float, fin: FinPapel?) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxHeight(0.95f)
            .aspectRatio(0.78f, matchHeightConstraintsFirst = true)
            .clip(RoundedCornerShape(10.dp))
            .background(ColorPapel)
            .border(2.dp, Color(0xFFBDBDBD), RoundedCornerShape(10.dp))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(14.dp)) {
            Text(papel.emoji, fontSize = 64.sp)
            Text(papel.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF333333), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(14.dp))
            // Barra de tiempo: se pone roja cuando queda poco
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(tiempo.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(if (tiempo < 0.3f) TintaSello else NachoYellow)
                )
            }
        }
        if (fin == FinPapel.SELLADO) {
            Text(
                "PENDIENTE",
                color = TintaSello,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                modifier = Modifier
                    .rotate(-18f)
                    .border(3.dp, TintaSello, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            )
        }
    }
}
