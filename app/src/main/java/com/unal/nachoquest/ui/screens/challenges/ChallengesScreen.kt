package com.unal.nachoquest.ui.screens.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.components.Olvido
import com.unal.nachoquest.ui.components.pulso
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow

@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel = hiltViewModel(),
    onIniciarReto: (String) -> Unit = {},
    onVerEscena: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NachoCream),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { Encabezado(state, onVerEscena) }

        itemsIndexed(state.capitulos) { indice, cap ->
            NodoCapitulo(
                cap = cap,
                esUltimo = indice == state.capitulos.lastIndex,
                onIniciarReto = onIniciarReto,
                onVerEscena = onVerEscena
            )
        }

        if (state.historiaCompletada) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(NachoYellow.copy(alpha = 0.25f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👑 ¡Eres una Leyenda de la Ciudad Blanca!", fontWeight = FontWeight.ExtraBold, color = NachoGreenDark)
                    Text(
                        "La brújula apunta al Estadio Alfonso López... Próximamente: Temporada 2.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { onVerEscena(Historia.ESCENA_EPILOGO) }) {
                        Text("Ver epílogo", color = NachoGreenDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun Encabezado(state: ChallengesUiState, onVerEscena: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    if (state.historiaCompletada) listOf(NachoGreenDark, NachoGreen)
                    else listOf(Color(0xFF3A3D44), NachoGreenDark)
                )
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("LA HISTORIA", color = NachoYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(
                    "El Olvido de la\nCiudad Blanca",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Capítulos: ${state.completados}/${state.capitulos.size}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "📖 Ver prólogo",
                    color = NachoYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { onVerEscena(Historia.ESCENA_PROLOGO) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Box(contentAlignment = Alignment.Center) {
                if (!state.historiaCompletada) {
                    Olvido(modifier = Modifier.size(120.dp))
                }
                Nacho(
                    modifier = Modifier.size(80.dp),
                    expresion = if (state.historiaCompletada) Expresion.FELIZ else Expresion.DECIDIDO
                )
            }
        }
    }
}

@Composable
private fun NodoCapitulo(
    cap: CapituloUi,
    esUltimo: Boolean,
    onIniciarReto: (String) -> Unit,
    onVerEscena: (String) -> Unit
) {
    val c = cap.capitulo
    val colorNodo = when (cap.estado) {
        EstadoCapitulo.COMPLETADO -> NachoGreen
        EstadoCapitulo.ACTUAL -> NachoYellow
        EstadoCapitulo.BLOQUEADO -> Color(0xFFBDBDBD)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
    ) {
        // Línea de tiempo
        Column(
            modifier = Modifier.width(44.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .pulso(activo = cap.estado == EstadoCapitulo.ACTUAL, intensidad = 0.12f)
                    .clip(CircleShape)
                    .background(colorNodo),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (cap.estado) {
                        EstadoCapitulo.COMPLETADO -> "✓"
                        EstadoCapitulo.ACTUAL -> "${c.numero}"
                        EstadoCapitulo.BLOQUEADO -> "🔒"
                    },
                    color = if (cap.estado == EstadoCapitulo.ACTUAL) NachoGreenDark else Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (cap.estado == EstadoCapitulo.BLOQUEADO) 14.sp else 16.sp
                )
            }
            if (!esUltimo) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .weight(1f)
                        .background(if (cap.estado == EstadoCapitulo.COMPLETADO) NachoGreen else Color(0xFFDDDDDD))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Tarjeta del capítulo
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (cap.estado == EstadoCapitulo.BLOQUEADO) Color(0xFFF0EEE8) else Color.White)
                .then(
                    if (cap.estado == EstadoCapitulo.ACTUAL) Modifier.border(2.dp, NachoYellow, RoundedCornerShape(18.dp))
                    else Modifier
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "CAPÍTULO ${c.numero}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = if (cap.estado == EstadoCapitulo.BLOQUEADO) Color.Gray else NachoGreen,
                    modifier = Modifier.weight(1f)
                )
                if (cap.estado == EstadoCapitulo.COMPLETADO) {
                    Text("⭐".repeat(cap.estrellas) + "☆".repeat(3 - cap.estrellas), fontSize = 13.sp, color = NachoYellow)
                }
            }
            Text(
                text = c.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (cap.estado == EstadoCapitulo.BLOQUEADO) Color.Gray else Color(0xFF1E1E1E)
            )
            Text(
                text = "📍 ${c.lugar}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (cap.estado == EstadoCapitulo.BLOQUEADO) Color.Gray else NachoGreenDark
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                // Los capítulos bloqueados no revelan su trama
                text = if (cap.estado == EstadoCapitulo.BLOQUEADO) "La bruma cubre este capítulo..." else c.sinopsis,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.04f))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚡ +${c.xpRecompensa} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NachoGreenDark)
                cap.itemRecompensa?.let {
                    Text(
                        text = if (cap.estado == EstadoCapitulo.BLOQUEADO) "❓ Ítem misterioso" else "${it.emojiIcono} ${it.nombre}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }

            if (cap.estado == EstadoCapitulo.BLOQUEADO && cap.itemRequerido != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🔒 Necesitas: ${cap.itemRequerido.nombre} (Capítulo ${c.numero - 1})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE65100),
                    fontWeight = FontWeight.Bold
                )
            }

            if (cap.estado != EstadoCapitulo.BLOQUEADO) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onIniciarReto(c.puntoId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (cap.estado == EstadoCapitulo.COMPLETADO) Color(0xFF1E88E5) else NachoGreen
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .pulso(activo = cap.estado == EstadoCapitulo.ACTUAL, intensidad = 0.015f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (cap.estado == EstadoCapitulo.COMPLETADO) "Repetir" else "Jugar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (cap.estado == EstadoCapitulo.COMPLETADO && c.escenaOutro in cap.escenasVistas) {
                        OutlinedButton(
                            onClick = { onVerEscena(c.escenaOutro) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("🎬 Escena", color = NachoGreenDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

