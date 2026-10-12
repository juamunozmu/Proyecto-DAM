package com.unal.nachoquest.ui.screens.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow

@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel = hiltViewModel(),
    onIniciarReto: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val totalMisiones = viewModel.misionesHistoria.size
    val completadas = viewModel.misionesHistoria.count { state.estaCompletado(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Título y progreso general
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Aventura UNAL",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = NachoGreenDark
                )
                Text(
                    text = "Cadena de Capítulos y Misiones",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NachoGreen.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Capítulos: $completadas/$totalMisiones",
                    fontWeight = FontWeight.Bold,
                    color = NachoGreenDark,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Capítulos de la Historia
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(viewModel.misionesHistoria) { hito ->
                val completado = state.estaCompletado(hito)
                val desbloqueado = state.estaDesbloqueado(hito)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            completado -> Color(0xFFF1F8E9) // Verde muy suave
                            desbloqueado -> Color.White
                            else -> Color(0xFFF5F5F5)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (desbloqueado) 3.dp else 0.dp
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            completado -> NachoGreen
                                            desbloqueado -> NachoGreenDark
                                            else -> Color.Gray
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "CAPÍTULO ${hito.capitulo}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }

                            if (completado) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NachoGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Superado", color = NachoGreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else if (!desbloqueado) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bloqueado", color = Color.Gray, fontSize = 12.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(NachoYellow.copy(alpha = 0.3f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("⚡ Disponible", color = Color(0xFF8D6E00), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = hito.tituloMision,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (desbloqueado) Color.Black else Color.Gray
                        )

                        Text(
                            text = "📍 ${hito.nombreLugar}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (desbloqueado) NachoGreenDark else Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = hito.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Recompensas del capítulo
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.03f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Recompensa:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⚡ +${hito.xpRecompensa} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NachoGreenDark)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("${hito.emojiRecompensa} ${hito.nombreItemRecompensa}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Requisito si está bloqueado
                        if (!desbloqueado && hito.nombreItemRequerido != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "🔒 Requiere: ${hito.nombreItemRequerido} (Se obtiene en el Capítulo ${hito.capitulo - 1})",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Botón de acción
                        if (desbloqueado) {
                            Button(
                                onClick = { onIniciarReto(hito.puntoId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (completado) Color(0xFF1E88E5) else NachoGreen
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (completado) "Repetir reto" else "Iniciar reto de capítulo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
