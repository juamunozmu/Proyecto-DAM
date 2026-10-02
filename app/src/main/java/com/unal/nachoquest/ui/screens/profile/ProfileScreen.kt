package com.unal.nachoquest.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow

/**
 * Pantalla de perfil del usuario.
 * Muestra avatar, nivel, XP, estadÃ­sticas y logros.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onCerrarSesion: () -> Unit
) {
    val state by viewModel.profileState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header con settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mi perfil",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = NachoGreenDark
            )
            IconButton(onClick = { /* TODO: pantalla de configuraciÃ³n */ }) {
                Icon(Icons.Default.Settings, contentDescription = "ConfiguraciÃ³n")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(NachoGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.nombre.take(2).uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nombre y nivel
        Text(
            text = state.nombre.ifBlank { "Explorador" },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "Nivel ${state.nivel}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${state.xp} / ${state.xpParaSiguienteNivel} XP",
                style = MaterialTheme.typography.bodySmall,
                color = NachoGreen,
                fontWeight = FontWeight.Bold
            )
        }

        // Barra de progreso XP
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = {
                if (state.xpParaSiguienteNivel > 0) state.xp.toFloat() / state.xpParaSiguienteNivel
                else 0f
            },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = NachoGreen,
            trackColor = Color.LightGray.copy(alpha = 0.3f),
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(32.dp))

        // EstadÃ­sticas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = "${state.retosCompletados}", label = "Retos", icon = Icons.Default.EmojiEvents)
            StatItem(value = "${state.lugaresVisitados}", label = "Lugares", icon = Icons.Default.LocationOn)
            StatItem(value = "${state.insigniasObtenidas}", label = "Insignias", icon = Icons.Default.Star)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mis logros
        Text(
            text = "Mis logros",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Logros placeholder
        val logros = listOf(
            "Primera exploraciÃ³n" to "Visitaste tu primer punto de interÃ©s",
            "Maestro de biblioteca" to "Completa todos los retos del edificio",
            "Caminante incansable" to "Visita 10 puntos de interÃ©s"
        )

        logros.forEach { (titulo, descripcion) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("ðŸ”’", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Cerrar sesiÃ³n
        TextButton(
            onClick = onCerrarSesion,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "Cerrar sesiÃ³n",
                color = Color.Red.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Componente de estadÃ­stica individual (Retos, Lugares, Insignias).
 */
@Composable
private fun StatItem(value: String, label: String, icon: ImageVector) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NachoGreenDark
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
