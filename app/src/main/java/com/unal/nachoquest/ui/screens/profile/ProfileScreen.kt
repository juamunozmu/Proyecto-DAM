package com.unal.nachoquest.ui.screens.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.Logro
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow

/**
 * Pantalla de perfil del usuario.
 * Muestra avatar, nivel, XP, estadísticas, progreso de la historia y logros.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onCerrarSesion: () -> Unit,
    onVerEscena: (String) -> Unit = {}
) {
    val state by viewModel.profileState.collectAsState()
    var menuAbierto by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NachoCream)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera con Nacho como avatar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(NachoGreenDark, NachoGreen)))
                .padding(20.dp)
        ) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { menuAbierto = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White)
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    DropdownMenuItem(
                        text = { Text("📖 Ver el prólogo otra vez") },
                        onClick = {
                            menuAbierto = false
                            onVerEscena(Historia.ESCENA_PROLOGO)
                        }
                    )
                    if (state.historiaCompletada) {
                        DropdownMenuItem(
                            text = { Text("👑 Ver el epílogo") },
                            onClick = {
                                menuAbierto = false
                                onVerEscena(Historia.ESCENA_EPILOGO)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión", color = Color(0xFFC62828)) },
                        onClick = {
                            menuAbierto = false
                            viewModel.cerrarSesion()
                            onCerrarSesion()
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(3.dp, NachoYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Nacho(
                        modifier = Modifier.size(70.dp),
                        expresion = if (state.historiaCompletada) Expresion.FELIZ else Expresion.NORMAL
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.cargando) "Cargando..." else state.nombre.ifBlank { "Explorador" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = tituloPorNivel(state.nivel, state.historiaCompletada),
                        color = NachoYellow,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BarraXp(state)
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatItem(valor = state.retosCompletados, etiqueta = "Retos", emoji = "🏆", modifier = Modifier.weight(1f))
                StatItem(valor = state.lugaresDescubiertos, etiqueta = "Lugares", emoji = "📍", modifier = Modifier.weight(1f))
                StatItem(valor = state.insigniasObtenidas, etiqueta = "Insignias", emoji = "🎖️", modifier = Modifier.weight(1f))
                StatItem(valor = state.estrellas, etiqueta = "Estrellas", emoji = "⭐", modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progreso de la historia
            Text("La historia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.historiaCompletada) "¡El Olvido fue derrotado!" else "El Olvido de la Ciudad Blanca",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${state.capitulosCompletados}/${state.totalCapitulos}",
                        fontWeight = FontWeight.Bold,
                        color = NachoGreen
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(state.totalCapitulos) { i ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(if (i < state.capitulosCompletados) NachoGreen else Color(0xFFE6E6E6))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logros
            val desbloqueados = state.logros.count { it.desbloqueado }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mis logros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("$desbloqueados/${state.logros.size}", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            state.logros.forEach { logro ->
                TarjetaLogro(logro)
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = {
                    viewModel.cerrarSesion()
                    onCerrarSesion()
                },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = Color.Red.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun tituloPorNivel(nivel: Int, historiaCompletada: Boolean): String = when {
    historiaCompletada -> "👑 Leyenda de la Ciudad Blanca · Nivel $nivel"
    nivel >= 6 -> "Guardián del campus · Nivel $nivel"
    nivel >= 3 -> "Explorador · Nivel $nivel"
    else -> "Primíparo · Nivel $nivel"
}

@Composable
private fun BarraXp(state: ProfileUiState) {
    val progreso = remember { Animatable(0f) }
    val objetivo = state.xpEnNivel.toFloat() / state.xpPorNivel
    LaunchedEffect(objetivo) {
        progreso.animateTo(objetivo, tween(900))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.White.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progreso.value.coerceIn(0f, 1f))
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(NachoYellow)
        )
    }
    Text(
        text = "${state.xpEnNivel} / ${state.xpPorNivel} XP para el nivel ${state.nivel + 1}",
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}

/**
 * Componente de estadística individual (Retos, Lugares, Insignias, Estrellas).
 */
@Composable
private fun StatItem(valor: Int, etiqueta: String, emoji: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 18.sp)
        Text("$valor", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = NachoGreenDark)
        Text(etiqueta, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun TarjetaLogro(logro: Logro) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (logro.desbloqueado) Color.White else Color(0xFFF0EEE8))
            .then(
                if (logro.desbloqueado) Modifier.border(1.5.dp, NachoYellow.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                else Modifier
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (logro.desbloqueado) NachoGreen.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (logro.desbloqueado) logro.emoji else "🔒",
                fontSize = 20.sp,
                modifier = Modifier.graphicsLayer { alpha = if (logro.desbloqueado) 1f else 0.6f }
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = logro.titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (logro.desbloqueado) Color(0xFF1E1E1E) else Color.Gray
            )
            Text(
                text = logro.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            if (!logro.desbloqueado && logro.meta > 1) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(logro.progreso.toFloat() / logro.meta)
                            .height(5.dp)
                            .background(NachoGreen.copy(alpha = 0.6f))
                    )
                }
            }
        }
        if (logro.meta > 1) {
            Spacer(modifier = Modifier.width(8.dp))
            Text("${logro.progreso}/${logro.meta}", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}
