package com.unal.nachoquest.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.domain.model.Hablante
import com.unal.nachoquest.domain.model.LineaDialogo
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

private const val MS_POR_LETRA = 24L

/**
 * Escena de historia a pantalla completa. Tocar la pantalla completa el texto actual
 * o pasa a la siguiente línea; "Saltar" termina la escena de inmediato.
 */
@Composable
fun EscenaDialogo(
    lineas: List<LineaDialogo>,
    onTerminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (lineas.isEmpty()) {
        LaunchedEffect(Unit) { onTerminar() }
        return
    }

    val haptic = LocalHapticFeedback.current
    var indice by rememberSaveable { mutableIntStateOf(0) }
    var letrasVisibles by remember { mutableIntStateOf(0) }
    val linea = lineas[indice.coerceAtMost(lineas.lastIndex)]
    val escribiendo = letrasVisibles < linea.texto.length

    LaunchedEffect(indice) {
        letrasVisibles = 0
        while (letrasVisibles < linea.texto.length) {
            val anterior = linea.texto[letrasVisibles]
            letrasVisibles++
            // Pausas naturales tras la puntuación
            delay(
                when (anterior) {
                    '.', '!', '?' -> MS_POR_LETRA * 8
                    ',', ':' -> MS_POR_LETRA * 4
                    else -> MS_POR_LETRA
                }
            )
        }
    }

    val esOlvido = linea.hablante == Hablante.OLVIDO
    val colorArriba by animateColorAsState(
        if (esOlvido) Color(0xFF2B2D31) else NachoGreenDark,
        tween(700),
        label = "fondoArriba"
    )
    val colorAbajo by animateColorAsState(
        if (esOlvido) Color(0xFF5B5E66) else NachoGreen,
        tween(700),
        label = "fondoAbajo"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colorArriba, colorAbajo)))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (escribiendo) {
                    letrasVisibles = linea.texto.length
                } else if (indice < lineas.lastIndex) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    indice++
                } else {
                    onTerminar()
                }
            }
    ) {
        ParticulasFlotantes(
            modifier = Modifier.fillMaxSize(),
            color = if (esOlvido) Color(0xFFB0B4BC) else NachoYellow
        )

        TextButton(
            onClick = onTerminar,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Text("Saltar ⏭", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
        }

        // Personaje que habla
        AnimatedContent(
            targetState = linea.hablante,
            transitionSpec = {
                (fadeIn(tween(350)) + scaleIn(tween(350), initialScale = 0.85f))
                    .togetherWith(fadeOut(tween(250)))
            },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 140.dp),
            label = "personaje"
        ) { hablante ->
            when (hablante) {
                Hablante.NACHO -> Nacho(
                    modifier = Modifier.size(190.dp),
                    expresion = linea.expresion,
                    hablando = escribiendo
                )
                Hablante.OLVIDO -> Olvido(modifier = Modifier.size(240.dp))
                Hablante.NARRADOR -> Spacer(modifier = Modifier.size(190.dp))
            }
        }

        // Caja de diálogo
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            if (linea.hablante != Hablante.NARRADOR) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(if (esOlvido) Color(0xFF3A3C42) else NachoYellow)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (esOlvido) "El Olvido" else "Nacho",
                        color = if (esOlvido) Color(0xFFE8F0FF) else NachoGreenDark,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        when (linea.hablante) {
                            Hablante.OLVIDO -> Color(0xF01E1F23)
                            Hablante.NARRADOR -> Color(0xCC000000)
                            Hablante.NACHO -> NachoCream
                        }
                    )
                    .padding(18.dp)
            ) {
                Text(
                    text = linea.texto.take(letrasVisibles),
                    color = if (linea.hablante == Hablante.NACHO) Color(0xFF1E1E1E) else Color(0xFFEDEDED),
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    fontStyle = if (linea.hablante == Hablante.NACHO) FontStyle.Normal else FontStyle.Italic,
                    fontWeight = if (linea.hablante == Hablante.NACHO) FontWeight.Medium else FontWeight.Normal
                )

                if (!escribiendo) {
                    IndicadorContinuar(
                        color = if (linea.hablante == Hablante.NACHO) NachoGreen else Color.White,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                lineas.indices.forEach { i ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = if (i == indice) 18.dp else 6.dp, height = 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = if (i <= indice) 0.9f else 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicadorContinuar(color: Color, modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "continuar")
    val salto by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "salto"
    )
    Text(
        text = "▼",
        color = color,
        fontSize = 14.sp,
        modifier = modifier.graphicsLayer { translationY = salto * 6f }
    )
}
