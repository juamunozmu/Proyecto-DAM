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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.unal.nachoquest.domain.model.Tienda
import com.unal.nachoquest.domain.model.TipoAyuda
import com.unal.nachoquest.domain.model.TipoMinijuego
import com.unal.nachoquest.domain.model.UserProfile
import com.unal.nachoquest.ui.components.BotonAyuda
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.SaldoMonedas
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Etiqueta
import com.unal.nachoquest.ui.components.LluviaConfeti
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

/**
 * Marco común de los minijuegos: título, consejo de Nacho, botón de pista y celebración al terminar.
 * Cada minijuego llama a `alTerminar(estrellas)` cuando el jugador lo resuelve, y recibe `pistas`
 * (cuántas ha pagado el jugador) para aplicar una ayuda cada vez que el número sube.
 *
 * @param onPedirPista cobra la pista (reserva o monedas); devuelve false si no se pudo pagar
 */
@Composable
fun PantallaMinijuego(
    capitulo: Capitulo,
    perfil: UserProfile,
    onPedirPista: () -> Boolean,
    onCompletado: (estrellas: Int) -> Unit,
    onSalir: () -> Unit
) {
    var estrellasObtenidas by remember { mutableStateOf<Int?>(null) }
    var pistas by remember { mutableIntStateOf(0) }
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
                    Etiqueta("CAPÍTULO ${capitulo.numero} · MINIJUEGO", color = NachoGreen)
                    Text(
                        tipo.titulo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = NachoGreenDark
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    SaldoMonedas(perfil.monedas)
                    Spacer(modifier = Modifier.height(4.dp))
                    BotonAyuda(
                        tipo = TipoAyuda.PISTA,
                        etiqueta = "Pista ${pistas}/${Tienda.MAX_PISTAS_MINIJUEGO}",
                        reserva = Tienda.reserva(perfil, TipoAyuda.PISTA),
                        habilitado = estrellasObtenidas == null && pistas < Tienda.MAX_PISTAS_MINIJUEGO,
                        textoInactivo = if (estrellasObtenidas == null) "SIN PISTAS" else "LISTO",
                        onClick = { if (onPedirPista()) pistas++ }
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
                    TipoMinijuego.MURAL -> MinijuegoMural(pistas, alTerminar)
                    TipoMinijuego.LETRAS -> MinijuegoLetras(pistas, alTerminar)
                    TipoMinijuego.CIRCUITO -> MinijuegoCircuito(pistas, alTerminar)
                    TipoMinijuego.SINFONIA -> MinijuegoSinfonia(pistas, alTerminar)
                    TipoMinijuego.PENALES -> MinijuegoPenales(pistas, alTerminar)
                    TipoMinijuego.DIFERENCIAS -> MinijuegoDiferencias(pistas, alTerminar)
                    TipoMinijuego.PAREJAS -> MinijuegoParejas(pistas, alTerminar)
                    TipoMinijuego.LABERINTO -> MinijuegoLaberinto(pistas, alTerminar)
                    TipoMinijuego.MONTAJE -> MinijuegoMontaje(pistas, alTerminar)
                    TipoMinijuego.TANQUES -> MinijuegoTanques(pistas, alTerminar)
                    TipoMinijuego.HELICE -> MinijuegoHelice(pistas, alTerminar)
                    TipoMinijuego.VITRAL -> MinijuegoVitral(pistas, alTerminar)
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
                    Sonidos.reproducir(Efecto.VICTORIA)
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
                            when (tipo) {
                                TipoMinijuego.PENALES -> "¡Tanda ganada!"
                                TipoMinijuego.LABERINTO -> "¡Encontraste al Eco!"
                                TipoMinijuego.MONTAJE -> "¡Película armada!"
                                TipoMinijuego.TANQUES -> "¡Agua exacta!"
                                TipoMinijuego.HELICE -> "¡Hélice completa!"
                                TipoMinijuego.VITRAL -> "¡Vitral encendido!"
                                else -> "¡Recuerdo restaurado!"
                            },
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
                        BotonJuego(
                            texto = "Continuar",
                            onClick = { onCompletado(estrellas) },
                            modifier = Modifier.fillMaxWidth()
                        )
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
