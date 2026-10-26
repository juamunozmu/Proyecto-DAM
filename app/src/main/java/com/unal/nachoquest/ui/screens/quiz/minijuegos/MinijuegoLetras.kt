package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.sacudir
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import kotlinx.coroutines.delay
import java.text.Normalizer

private const val TINTA_POR_PALABRA = 6
private const val PALABRAS_POR_PARTIDA = 3

private data class PalabraBorrada(val palabra: String, val pista: String)

private val BANCO_PALABRAS = listOf(
    PalabraBorrada("MACONDO", "El pueblo que imaginó García Márquez en «Cien años de soledad»."),
    PalabraBorrada("ARACATACA", "El pueblo del Magdalena donde nació Gabriel García Márquez."),
    PalabraBorrada("NOBEL", "El premio de literatura que recibió Gabo en 1982."),
    PalabraBorrada("REALISMO MÁGICO", "El estilo literario que hizo famoso a Gabo en el mundo."),
    PalabraBorrada("SOLEDAD", "«Cien años de...»"),
    PalabraBorrada("BIBLIOTECA", "El lugar donde estás ahora mismo."),
    PalabraBorrada("PARCIAL", "Lo que todo estudiante prepara aquí a última hora.")
)

private val TECLADO = listOf("QWERTYUIOP", "ASDFGHJKLÑ", "ZXCVBNM")

private fun sinTilde(c: Char): Char =
    if (c == 'Ñ') c else Normalizer.normalize(c.toString(), Normalizer.Form.NFD)[0]

/**
 * Ahorcado temático: el Olvido borró las letras de palabras ligadas a la biblioteca y a Gabo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MinijuegoLetras(alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val palabras = remember { BANCO_PALABRAS.shuffled().take(PALABRAS_POR_PARTIDA) }
    var ronda by remember { mutableIntStateOf(0) }
    var usadas by remember { mutableStateOf(setOf<Char>()) }
    var tinta by remember { mutableIntStateOf(TINTA_POR_PALABRA) }
    var erroresTotales by remember { mutableIntStateOf(0) }
    var fallos by remember { mutableIntStateOf(0) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val actual = palabras[ronda]
    val letrasNecesarias = actual.palabra.filter { it.isLetter() }.map(::sinTilde).toSet()
    val completa = letrasNecesarias.all { it in usadas }
    val sinTinta = tinta <= 0
    val rondaTerminada = completa || sinTinta

    LaunchedEffect(ronda, rondaTerminada) {
        if (!rondaTerminada) return@LaunchedEffect
        mensaje = if (completa) "¡Palabra recuperada!" else "Se acabó la tinta... era «${actual.palabra}»"
        delay(1600)
        mensaje = null
        if (ronda < palabras.lastIndex) {
            ronda++
            usadas = emptySet()
            tinta = TINTA_POR_PALABRA
        } else {
            alTerminar(
                when {
                    erroresTotales <= 3 -> 3
                    erroresTotales <= 7 -> 2
                    else -> 1
                }
            )
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Marcador("Página ${ronda + 1}/${palabras.size}")
            Row {
                repeat(TINTA_POR_PALABRA) { i ->
                    val lleno = i < tinta
                    val escala by animateFloatAsState(if (lleno) 1f else 0.6f, spring(dampingRatio = 0.4f), label = "gota")
                    Text(
                        "💧",
                        fontSize = 18.sp,
                        modifier = Modifier.graphicsLayer {
                            scaleX = escala
                            scaleY = escala
                            alpha = if (lleno) 1f else 0.2f
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Página del libro con la palabra
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFF8E7))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "“${actual.pista}”",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Serif,
                color = Color(0xFF5D4037),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.sacudir(fallos)
            ) {
                actual.palabra.split(" ").forEach { parte ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(horizontal = 6.dp)) {
                        parte.forEach { letra ->
                            CasillaLetra(
                                letra = letra,
                                visible = sinTilde(letra) in usadas || sinTinta,
                                perdida = sinTinta && sinTilde(letra) !in usadas
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = mensaje ?: " ",
                fontWeight = FontWeight.Bold,
                color = if (completa) NachoGreenDark else Color(0xFF8D6E63)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Teclado
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            TECLADO.forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    fila.forEach { tecla ->
                        val usada = tecla in usadas
                        val acierto = usada && tecla in letrasNecesarias
                        val fondo by animateColorAsState(
                            when {
                                acierto -> NachoGreen
                                usada -> Color(0xFFBDBDBD)
                                else -> Color.White
                            },
                            label = "tecla"
                        )
                        Box(
                            modifier = Modifier
                                .width(31.dp)
                                .height(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(fondo)
                                .clickable(enabled = !usada && !rondaTerminada) {
                                    usadas = usadas + tecla
                                    if (tecla !in letrasNecesarias) {
                                        tinta--
                                        erroresTotales++
                                        fallos++
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    } else {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                tecla.toString(),
                                fontWeight = FontWeight.Bold,
                                color = if (acierto) Color.White else if (usada) Color.Gray else Color(0xFF333333)
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CasillaLetra(letra: Char, visible: Boolean, perdida: Boolean) {
    val escala by animateFloatAsState(
        if (visible) 1f else 0.7f,
        spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "letra"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(22.dp)) {
        Text(
            text = if (visible) letra.toString() else " ",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = if (perdida) Color(0xFFB0A090) else Color(0xFF3E2723),
            modifier = Modifier.graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
        )
        Box(
            modifier = Modifier
                .size(width = 20.dp, height = 3.dp)
                .background(if (visible) Color(0xFF8D6E63) else Color(0x558D6E63))
        )
    }
}
