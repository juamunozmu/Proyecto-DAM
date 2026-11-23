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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.JugadorRanking
import com.unal.nachoquest.domain.model.RetosLugares
import com.unal.nachoquest.domain.model.TriviaDiaria
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Etiqueta
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.components.Olvido
import com.unal.nachoquest.ui.components.pulso
import com.unal.nachoquest.ui.components.tarjetaJuego
import com.unal.nachoquest.ui.theme.CantoCrema
import com.unal.nachoquest.ui.theme.EstiloEtiqueta
import com.unal.nachoquest.ui.theme.NachoAzul
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoNaranja
import com.unal.nachoquest.ui.theme.NachoYellow
import com.unal.nachoquest.ui.theme.Papel
import com.unal.nachoquest.ui.theme.Tinta

@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel = hiltViewModel(),
    onIniciarReto: (String) -> Unit = {},
    onVerEscena: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val ranking by viewModel.ranking.collectAsState()

    if (ranking.visible) {
        DialogoRanking(ranking = ranking, onCerrar = viewModel::cerrarRanking)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NachoCream),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item { Encabezado(state, onVerEscena) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaTrivia(
                    trivia = state.trivia,
                    onJugar = { onIniciarReto(TriviaDiaria.PUNTO_ID) },
                    modifier = Modifier.weight(1.5f).fillMaxHeight()
                )
                TarjetaRanking(onAbrir = viewModel::abrirRanking, modifier = Modifier.weight(1f).fillMaxHeight())
            }
        }

        itemsIndexed(state.capitulos) { indice, cap ->
            val acto = cap.capitulo.acto
            // Separador al empezar cada acto; la línea de tiempo se corta al final de cada uno
            if (indice == 0 || state.capitulos[indice - 1].capitulo.acto != acto) {
                SeparadorActo(acto)
            }
            NodoCapitulo(
                cap = cap,
                esUltimo = state.capitulos.getOrNull(indice + 1)?.capitulo?.acto != acto,
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
                        .tarjetaJuego(fondo = Color(0xFFF7E9B8), borde = NachoYellow)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🌟", fontSize = 34.sp)
                    Text(
                        "¡Guardián de la Memoria!",
                        style = MaterialTheme.typography.titleLarge,
                        color = NachoGreenDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Reuniste a los tres Ecos y el Olvido ahora es el Recuerdo. Repite los capítulos " +
                            "para conseguir todas las estrellas, juega la trivia cada día y sube en el ranking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    BotonJuego(
                        texto = "Ver el epílogo final",
                        onClick = { onVerEscena(Historia.ESCENA_EPILOGO_FINAL) },
                        color = NachoYellow,
                        colorTexto = NachoGreenDark,
                        alto = 44.dp,
                        tamanoTexto = 14.sp
                    )
                }
            }
        }

        if (state.retosLugar.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Retos del campus",
                            style = MaterialTheme.typography.titleLarge,
                            color = Tinta,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${state.retosLugarCompletados}/${state.retosLugar.size}",
                            fontWeight = FontWeight.ExtraBold,
                            color = NachoGreenDark
                        )
                    }
                    Text(
                        "Busca los pines ❓ en el mapa y acércate a cada lugar para jugar su reto. " +
                            "Los que ya superaste se pueden repetir desde aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }
            items(state.retosLugar, key = { it.reto.puntoId }) { lugar ->
                FilaRetoLugar(lugar = lugar, onRepetir = { onIniciarReto(lugar.reto.puntoId) })
            }
        }
    }
}

@Composable
private fun FilaRetoLugar(lugar: RetoLugarUi, onRepetir: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .tarjetaJuego(
                fondo = Papel,
                borde = if (lugar.completado) CantoCrema else Color(0xFFD1B3E0),
                radio = 14.dp,
                canto = 3.dp
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (lugar.completado) "✅" else "❓", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                lugar.reto.nombre,
                fontWeight = FontWeight.Bold,
                color = Tinta,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (lugar.completado) "⭐".repeat(lugar.estrellas) + "☆".repeat(3 - lugar.estrellas)
                else "📍 Juégalo en el lugar · +${RetosLugares.XP_RECOMPENSA} XP",
                style = MaterialTheme.typography.bodySmall,
                color = if (lugar.completado) NachoYellow else Color.Gray
            )
        }
        if (lugar.completado) {
            BotonJuego(
                texto = "Repetir",
                onClick = onRepetir,
                color = NachoAzul,
                alto = 38.dp,
                tamanoTexto = 13.sp
            )
        }
    }
}

@Composable
private fun TarjetaTrivia(trivia: TriviaUi, onJugar: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .pulso(activo = !trivia.jugadaHoy, intensidad = 0.015f)
            .tarjetaJuego(
                pincel = Brush.linearGradient(listOf(Color(0xFFFFB300), Color(0xFFF57C00))),
                borde = Color(0xFFB85C00)
            )
            .clickable(onClick = onJugar)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Etiqueta("TRIVIA DEL DÍA", color = Color.White, modifier = Modifier.weight(1f))
            if (trivia.rachaDias > 0) {
                Text(
                    "🔥 ${trivia.rachaDias}",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (trivia.jugadaHoy) "¡Hecha por hoy!" else "5 preguntas nuevas",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 23.sp,
            lineHeight = 24.sp,
            color = Color.White
        )
        Text(
            text = if (trivia.jugadaHoy) "Vuelve mañana para alargar tu racha. Puedes practicar otra vez."
            else "+${trivia.xpDisponible} XP, más bonos por rapidez y por no fallar.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.95f)
        )
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (trivia.jugadaHoy) "Practicar ▶" else "Jugar ▶",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = Color(0xFFE65100),
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun TarjetaRanking(onAbrir: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .tarjetaJuego(
                pincel = Brush.linearGradient(listOf(NachoGreen, NachoGreenDark)),
                borde = Color(0xFF0E1B05)
            )
            .clickable(onClick = onAbrir)
            .padding(14.dp)
    ) {
        Etiqueta("RANKING", color = NachoYellow)
        Spacer(modifier = Modifier.height(4.dp))
        Text("🏆", fontSize = 30.sp)
        Text(
            "Compite con otros exploradores",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f)
        )
        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Ver tabla ▶", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = NachoYellow)
    }
}

@Composable
private fun DialogoRanking(ranking: RankingUiState, onCerrar: () -> Unit) {
    Dialog(onDismissRequest = onCerrar) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(NachoCream)
                .padding(18.dp)
        ) {
            Text(
                "🏆 Ranking de exploradores",
                style = MaterialTheme.typography.titleLarge,
                color = NachoGreenDark
            )
            Text("Los jugadores con más XP", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            when {
                ranking.cargando -> Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NachoGreen)
                }
                ranking.error -> Text(
                    "No pudimos cargar el ranking. Revisa tu conexión a internet e inténtalo de nuevo.",
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                ranking.jugadores.isEmpty() -> Text(
                    "Todavía no hay nadie en la tabla. ¡Gana XP y sé el primero!",
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(ranking.jugadores) { indice, jugador -> FilaRanking(indice + 1, jugador) }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            BotonJuego(texto = "Cerrar", onClick = onCerrar, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun FilaRanking(puesto: Int, jugador: JugadorRanking) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (jugador.esJugadorActual) NachoYellow.copy(alpha = 0.3f) else Color.White)
            .then(
                if (jugador.esJugadorActual) Modifier.border(1.5.dp, NachoYellow, RoundedCornerShape(12.dp))
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when (puesto) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "$puesto"
            },
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (puesto <= 3) 20.sp else 15.sp,
            color = Color.Gray,
            modifier = Modifier.width(34.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (jugador.esJugadorActual) "${jugador.nombre} (tú)" else jugador.nombre,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF1E1E1E)
            )
            Text("Nivel ${jugador.nivel} · ⭐ ${jugador.estrellas}", fontSize = 12.sp, color = Color.Gray)
        }
        Text("${jugador.xp} XP", style = EstiloEtiqueta, fontSize = 19.sp, color = NachoGreenDark)
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
                Etiqueta(if (state.actoActual == 1) "LA HISTORIA · ACTO I" else "LA HISTORIA · ACTO II", color = NachoYellow)
                Text(
                    Historia.titulosActos[state.actoActual].orEmpty(),
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
private fun SeparadorActo(acto: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Etiqueta(if (acto == 1) "ACTO I" else "ACTO II", color = NachoGreen)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            Historia.titulosActos[acto].orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Tinta
        )
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
                .padding(vertical = 8.dp)
                .tarjetaJuego(
                    fondo = if (cap.estado == EstadoCapitulo.BLOQUEADO) Color(0xFFECE7D8) else Papel,
                    borde = when (cap.estado) {
                        EstadoCapitulo.ACTUAL -> NachoYellow
                        EstadoCapitulo.BLOQUEADO -> Color(0xFFD3CCB8)
                        EstadoCapitulo.COMPLETADO -> CantoCrema
                    }
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Etiqueta(
                    texto = "CAPÍTULO ${c.numero}",
                    color = if (cap.estado == EstadoCapitulo.BLOQUEADO) Color.Gray else NachoGreen,
                    modifier = Modifier.weight(1f)
                )
                if (cap.estado == EstadoCapitulo.COMPLETADO) {
                    Text("⭐".repeat(cap.estrellas) + "☆".repeat(3 - cap.estrellas), fontSize = 13.sp, color = NachoYellow)
                }
            }
            Text(
                text = c.titulo,
                style = MaterialTheme.typography.titleLarge,
                color = if (cap.estado == EstadoCapitulo.BLOQUEADO) Color.Gray else Tinta
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
                    color = NachoNaranja,
                    fontWeight = FontWeight.Bold
                )
            }

            if (cap.estado != EstadoCapitulo.BLOQUEADO) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotonJuego(
                        texto = if (cap.estado == EstadoCapitulo.COMPLETADO) "Repetir" else "Jugar",
                        onClick = { onIniciarReto(c.puntoId) },
                        color = if (cap.estado == EstadoCapitulo.COMPLETADO) NachoAzul else NachoGreen,
                        icono = Icons.Default.PlayArrow,
                        alto = 46.dp,
                        modifier = Modifier
                            .weight(1f)
                            .pulso(activo = cap.estado == EstadoCapitulo.ACTUAL, intensidad = 0.015f)
                    )
                    if (cap.estado == EstadoCapitulo.COMPLETADO && c.escenaOutro in cap.escenasVistas) {
                        BotonJuego(
                            texto = "🎬 Escena",
                            onClick = { onVerEscena(c.escenaOutro) },
                            color = Color.White,
                            colorTexto = NachoGreenDark,
                            borde = CantoCrema,
                            alto = 46.dp,
                            tamanoTexto = 14.sp
                        )
                    }
                }
            }
        }
    }
}

