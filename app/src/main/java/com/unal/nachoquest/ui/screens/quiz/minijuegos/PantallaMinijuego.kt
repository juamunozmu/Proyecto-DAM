package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.domain.model.Capitulo
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.domain.model.TipoMinijuego
import com.unal.nachoquest.ui.components.LluviaConfeti
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

/**
 * Marco común de los minijuegos: título, consejo de Nacho y celebración al terminar.
 * Cada minijuego llama a `alTerminar(estrellas)` cuando el jugador lo resuelve.
 */
@Composable
fun PantallaMinijuego(
    capitulo: Capitulo,
    onCompletado: (estrellas: Int) -> Unit,
    onSalir: () -> Unit
) {
    var estrellasObtenidas by remember { mutableStateOf<Int?>(null) }
    val tipo = capitulo.minijuego

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F0E0), NachoCream)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSalir) {
                    Icon(Icons.Default.Close, contentDescription = "Salir", tint = Color.DarkGray)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "CAPÍTULO ${capitulo.numero} · MINIJUEGO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NachoGreen,
                        letterSpacing = 1.sp
                    )
                    Text(
                        tipo.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = NachoGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Consejo de Nacho
            Row(verticalAlignment = Alignment.CenterVertically) {
                Nacho(
                    modifier = Modifier.size(52.dp),
                    expresion = if (estrellasObtenidas != null) Expresion.FELIZ else Expresion.DECIDIDO,
                    saltando = estrellasObtenidas != null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tipo.instrucciones,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF333333),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val alTerminar: (Int) -> Unit = { if (estrellasObtenidas == null) estrellasObtenidas = it.coerceIn(1, 3) }
                when (tipo) {
                    TipoMinijuego.MURAL -> MinijuegoMural(alTerminar)
                    TipoMinijuego.LETRAS -> MinijuegoLetras(alTerminar)
                    TipoMinijuego.CIRCUITO -> MinijuegoCircuito(alTerminar)
                    TipoMinijuego.SINFONIA -> MinijuegoSinfonia(alTerminar)
                }
            }
        }

        // Celebración al completar
        estrellasObtenidas?.let { estrellas ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                LluviaConfeti(modifier = Modifier.fillMaxSize())
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(250)
                    visible = true
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(28.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(NachoCream)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "¡Recuerdo restaurado!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = NachoGreenDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(3) { i ->
                                val escala = remember { Animatable(0f) }
                                LaunchedEffect(Unit) {
                                    delay(300L + i * 250L)
                                    escala.animateTo(1f, spring(dampingRatio = 0.4f))
                                }
                                Text(
                                    "⭐",
                                    fontSize = 40.sp,
                                    modifier = Modifier.graphicsLayer {
                                        scaleX = escala.value
                                        scaleY = escala.value
                                        alpha = if (i < estrellas) 1f else 0.2f
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Ahora responde las preguntas del guardián para quedarte con el fragmento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onCompletado(estrellas) },
                            colors = ButtonDefaults.buttonColors(containerColor = NachoGreen),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("Continuar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

/** Indicador pequeño (movimientos, errores...) que usan varios minijuegos. */
@Composable
internal fun Marcador(texto: String, modifier: Modifier = Modifier, color: Color = NachoGreenDark) {
    Text(
        text = texto,
        fontWeight = FontWeight.Bold,
        color = color,
        fontSize = 14.sp,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NachoYellow.copy(alpha = 0.25f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
