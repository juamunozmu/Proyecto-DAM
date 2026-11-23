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
import com.unal.nachoquest.ui.theme.EstiloEtiqueta
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

private const val MS_POR_LETRA = 24L

/** Colores de la escena según quién habla. */
private data class EstiloHablante(
    val fondoArriba: Color,
    val fondoAbajo: Color,
    val particulas: Color,
    // Nombre en la pestaña sobre la caja de diálogo (null: el narrador no lleva pestaña)
    val nombre: String?,
    val fondoNombre: Color,
    val textoNombre: Color,
    val fondoCaja: Color,
    val texto: Color,
    // Nacho habla normal; los demás, en cursiva
    val cursiva: Boolean
)

private fun estiloDe(hablante: Hablante): EstiloHablante = when (hablante) {
    Hablante.NACHO -> EstiloHablante(
        NachoGreenDark, NachoGreen, NachoYellow,
        "NACHO", NachoYellow, NachoGreenDark, NachoCream, Color(0xFF1E1E1E), cursiva = false
    )
    Hablante.OLVIDO -> EstiloHablante(
        Color(0xFF2B2D31), Color(0xFF5B5E66), Color(0xFFB0B4BC),
        "EL OLVIDO", Color(0xFF3A3C42), Color(0xFFE8F0FF), Color(0xF01E1F23), Color(0xFFEDEDED), cursiva = true
    )
    Hablante.RECUERDO -> EstiloHablante(
        Color(0xFF3B2A0E), Color(0xFF8A6A24), Color(0xFFF2D27A),
        "EL RECUERDO", Color(0xFFD9A93B), Color(0xFF3B2A0E), Color(0xF02A1E0A), Color(0xFFFFF3D6), cursiva = true
    )
    Hablante.ENREDO -> EstiloHablante(
        Color(0xFF2A1236), Color(0xFF6A2C7A), Color(0xFFFF8AD8),
        "EL ENREDO", Color(0xFF8E24AA), Color.White, Color(0xF0241030), Color(0xFFF6E8FF), cursiva = true
    )
    Hablante.NARRADOR -> EstiloHablante(
        NachoGreenDark, NachoGreen, NachoYellow,
        null, Color.Transparent, Color.Transparent, Color(0xCC000000), Color(0xFFEDEDED), cursiva = true
    )
}

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

    val estilo = estiloDe(linea.hablante)
    val colorArriba by animateColorAsState(estilo.fondoArriba, tween(700), label = "fondoArriba")
    val colorAbajo by animateColorAsState(estilo.fondoAbajo, tween(700), label = "fondoAbajo")

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
            color = estilo.particulas
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
                Hablante.RECUERDO -> Olvido(modifier = Modifier.size(240.dp), dorado = true)
                Hablante.ENREDO -> Enredo(modifier = Modifier.size(230.dp))
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
            if (estilo.nombre != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                        .background(estilo.fondoNombre)
                        .padding(horizontal = 14.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = estilo.nombre,
                        color = estilo.textoNombre,
                        style = EstiloEtiqueta,
                        fontSize = 19.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 130.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(estilo.fondoCaja)
                    .padding(18.dp)
            ) {
                Text(
                    text = linea.texto.take(letrasVisibles),
                    color = estilo.texto,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    fontStyle = if (estilo.cursiva) FontStyle.Italic else FontStyle.Normal,
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
