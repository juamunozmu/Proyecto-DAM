package com.unal.nachoquest.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.components.ParticulasFlotantes
import com.unal.nachoquest.ui.components.pulso
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoQuestTheme
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida de NachoQuest.
 * Nacho cae desde arriba, aparece el título y luego los botones de acceso.
 */
@Composable
fun SplashScreen(
    onComenzar: () -> Unit,
    onIniciarSesion: () -> Unit,
    sesionActiva: Boolean = false
) {
    val caidaNacho = remember { Animatable(-900f) }
    val escalaTitulo = remember { Animatable(0f) }
    var mostrarBotones by remember { mutableStateOf(false) }
    var expresion by remember { mutableStateOf(Expresion.SORPRENDIDO) }

    LaunchedEffect(Unit) {
        launch {
            caidaNacho.animateTo(
                0f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            expresion = Expresion.FELIZ
        }
        delay(350)
        escalaTitulo.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow))
        delay(250)
        mostrarBotones = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NachoGreenDark, NachoGreen, Color(0xFF5E8F3A))))
    ) {
        ParticulasFlotantes(modifier = Modifier.fillMaxSize(), cantidad = 30)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.6f))

            // Título
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = escalaTitulo.value
                    scaleY = escalaTitulo.value
                    alpha = escalaTitulo.value.coerceIn(0f, 1f)
                }
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = NachoYellow)) { append("Nacho") }
                        withStyle(SpanStyle(color = Color.White)) { append("Quest") }
                    },
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Explora. Descubre.\nVive la UNAL.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Nacho(
                modifier = Modifier
                    .size(170.dp)
                    .graphicsLayer { translationY = caidaNacho.value },
                expresion = expresion
            )

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = mostrarBotones,
                enter = slideInVertically(tween(450)) { it } + fadeIn(tween(450))
            ) {
                Column {
                    Text(
                        text = "La Ciudad Blanca está olvidando su historia.\nSolo tú puedes ayudar a recuperarla.",
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onComenzar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .pulso(intensidad = 0.03f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NachoYellow,
                            contentColor = NachoGreenDark
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = if (sesionActiva) "Continuar la aventura" else "Comenzar la aventura",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }

                    if (!sesionActiva) {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onIniciarSesion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text(
                            text = "Ya tengo cuenta",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    NachoQuestTheme {
        SplashScreen(onComenzar = {}, onIniciarSesion = {})
    }
}
