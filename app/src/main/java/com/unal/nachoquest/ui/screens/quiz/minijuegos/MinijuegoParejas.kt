package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.sacudir
import com.unal.nachoquest.ui.theme.EstiloEtiqueta
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val COLUMNAS = 4
// Ancho / alto de cada carta
private const val PROPORCION_CARTA = 0.78f
private val ESPACIO_CARTAS = 8.dp

/**
 * Parejas del potrero: 16 cartas boca abajo con 8 animales. Se voltean de dos en dos; si son
 * iguales se quedan a la vista, si no, se vuelven a tapar.
 */
@Composable
fun MinijuegoParejas(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val mazo = remember { ReglasParejas.mazo() }
    val emparejadas = remember { mutableStateListOf<Int>() }
    val volteadas = remember { mutableStateListOf<Int>() }
    var fallos by remember { mutableIntStateOf(0) }
    var revisando by remember { mutableStateOf(false) }
    val terminado = emparejadas.size == mazo.size

    LaunchedEffect(terminado) {
        if (!terminado) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        delay(900)
        alTerminar(ReglasParejas.estrellas(fallos))
    }

    // Pista: Nacho junta una pareja que todavía no se ha encontrado
    LaunchedEffect(pistas) {
        if (pistas == 0 || terminado) return@LaunchedEffect
        while (revisando) delay(50)
        val animal = mazo.indices.filter { it !in emparejadas }.map { mazo[it] }.randomOrNull() ?: return@LaunchedEffect
        volteadas.clear()
        emparejadas.addAll(mazo.indices.filter { mazo[it] == animal })
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    fun voltear(carta: Int) {
        if (revisando || terminado || carta in emparejadas || carta in volteadas) return
        volteadas.add(carta)
        Sonidos.reproducir(Efecto.TOQUE)
        if (volteadas.size < 2) return
        val (a, b) = volteadas.toList()
        if (mazo[a] == mazo[b]) {
            emparejadas.addAll(listOf(a, b))
            volteadas.clear()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            Sonidos.reproducir(Efecto.ACIERTO)
        } else {
            revisando = true
            fallos++
            scope.launch {
                delay(750)
                volteadas.clear()
                revisando = false
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Parejas: ${emparejadas.size / 2}/${ReglasParejas.ANIMALES.size}")
            Marcador("Fallos: $fallos")
        }
        Spacer(modifier = Modifier.height(12.dp))

        // La grilla cabe a lo ancho y a lo alto (tabletas en horizontal), centrada
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            contentAlignment = Alignment.Center
        ) {
            val filas = (mazo.size + COLUMNAS - 1) / COLUMNAS
            val anchoCarta = minOf(
                (maxWidth - ESPACIO_CARTAS * (COLUMNAS - 1)) / COLUMNAS,
                (maxHeight - ESPACIO_CARTAS * (filas - 1)) / filas * PROPORCION_CARTA
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(ESPACIO_CARTAS),
                modifier = Modifier
                    .width(anchoCarta * COLUMNAS + ESPACIO_CARTAS * (COLUMNAS - 1))
                    .sacudir(fallos)
            ) {
                mazo.indices.chunked(COLUMNAS).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(ESPACIO_CARTAS), modifier = Modifier.fillMaxWidth()) {
                        fila.forEach { carta ->
                            CartaAnimal(
                                animal = ReglasParejas.ANIMALES[mazo[carta]],
                                bocaArriba = carta in volteadas || carta in emparejadas,
                                emparejada = carta in emparejadas,
                                onClick = { voltear(carta) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (terminado) "¡Cada animal encontró a su pareja!" else "Pista: fíjate bien dónde queda cada animal.",
            color = if (terminado) NachoGreenDark else Color.Gray,
            fontWeight = if (terminado) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = if (terminado) 17.sp else 13.sp
        )
    }
}

@Composable
private fun CartaAnimal(
    animal: Animal,
    bocaArriba: Boolean,
    emparejada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val giro by animateFloatAsState(if (bocaArriba) 180f else 0f, tween(300), label = "giroCarta")
    val muestraCara = giro > 90f
    Box(
        modifier = modifier
            .aspectRatio(PROPORCION_CARTA)
            .graphicsLayer {
                rotationY = giro
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    !muestraCara -> NachoGreen
                    emparejada -> Color(0xFFE6F2D9)
                    else -> Color.White
                }
            )
            .border(2.dp, if (muestraCara && emparejada) NachoGreen else NachoYellow, RoundedCornerShape(12.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (muestraCara) {
            // La cara se dibuja girada para que no se vea al revés
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            ) {
                Text(animal.emoji, fontSize = 30.sp)
                Text(animal.nombre, style = EstiloEtiqueta, fontSize = 10.sp, color = NachoGreenDark, maxLines = 1)
            }
        } else {
            Text("UN", style = EstiloEtiqueta, fontSize = 18.sp, color = NachoYellow)
        }
    }
}
