package com.unal.nachoquest.ui.screens.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.domain.model.Pregunta
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow

@Composable
fun QuizScreen(
    puntoId: String,
    onTerminar: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(puntoId) {
        viewModel.cargarRetoParaPunto(puntoId)
    }

    if (state.reto == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cargando reto...")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.quizTerminado) {
            PantallaResultados(state, onTerminar)
        } else {
            PantallaPregunta(state, viewModel)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.PantallaPregunta(state: QuizUiState, viewModel: QuizViewModel) {
    val reto = state.reto!!
    val pregunta = state.preguntaActual!!
    val progreso = (state.indicePreguntaActual + 1).toFloat() / reto.preguntas.size

    // Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = reto.titulo,
            style = MaterialTheme.typography.titleMedium,
            color = NachoGreenDark,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(NachoYellow.copy(alpha = 0.2f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${state.indicePreguntaActual + 1} / ${reto.preguntas.size}",
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    LinearProgressIndicator(
        progress = { progreso },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = NachoGreen,
        trackColor = Color.LightGray
    )

    Spacer(modifier = Modifier.height(40.dp))

    // Pregunta
    Text(
        text = pregunta.enunciado,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(40.dp))

    // Opciones
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        pregunta.opciones.forEachIndexed { index, textoOpcion ->
            OpcionPregunta(
                texto = textoOpcion,
                estaSeleccionada = state.respuestaSeleccionada == index,
                mostrarResultado = state.mostrarResultadoPregunta,
                esCorrecta = index == pregunta.indiceCorrecta,
                onClick = { viewModel.seleccionarRespuesta(index) }
            )
        }
    }

    Spacer(modifier = Modifier.weight(1f))

    // Botón de acción principal
    Button(
        onClick = {
            if (state.mostrarResultadoPregunta) {
                viewModel.avanzarSiguientePregunta()
            } else {
                viewModel.confirmarRespuesta()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (state.respuestaSeleccionada != null) NachoGreen else Color.LightGray
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = state.respuestaSeleccionada != null
    ) {
        Text(
            text = if (state.mostrarResultadoPregunta) "Siguiente" else "Comprobar",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OpcionPregunta(
    texto: String,
    estaSeleccionada: Boolean,
    mostrarResultado: Boolean,
    esCorrecta: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        mostrarResultado && esCorrecta -> NachoGreen.copy(alpha = 0.15f)
        mostrarResultado && estaSeleccionada && !esCorrecta -> Color.Red.copy(alpha = 0.1f)
        estaSeleccionada -> Color.LightGray.copy(alpha = 0.3f)
        else -> Color.White
    }

    val borderColor = when {
        mostrarResultado && esCorrecta -> NachoGreen
        mostrarResultado && estaSeleccionada && !esCorrecta -> Color.Red
        estaSeleccionada -> Color.DarkGray
        else -> Color.LightGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (estaSeleccionada && !mostrarResultado) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                fontWeight = if (estaSeleccionada || (mostrarResultado && esCorrecta)) FontWeight.Bold else FontWeight.Normal
            )
            
            if (mostrarResultado) {
                if (esCorrecta) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Correcto", tint = NachoGreen)
                } else if (estaSeleccionada) {
                    Icon(Icons.Default.Close, contentDescription = "Incorrecto", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
private fun PantallaResultados(state: QuizUiState, onTerminar: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = NachoYellow
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "¡Reto Completado!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NachoGreenDark
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Acertaste ${state.respuestasCorrectas} de ${state.reto?.preguntas?.size} preguntas.",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            colors = CardDefaults.cardColors(containerColor = NachoGreen.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Recompensas obtenidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("+${state.xpGanada} XP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onTerminar,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NachoGreen),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Volver al mapa", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}