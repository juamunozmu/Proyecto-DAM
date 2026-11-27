package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.OjosBuho
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

// Un color por tamaño de carpeta, de la más pequeña a la más grande
private val ColoresCarpeta = listOf(
    Color(0xFFFDD835), Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFF8E24AA), Color(0xFFE53935)
)

/**
 * La pila de solicitudes (capítulo 17): dos niveles, con 3 y 4 carpetas. Se toca la ventanilla
 * de donde sale la carpeta y luego a donde va. Se cuentan los movimientos de más.
 */
@Composable
fun MinijuegoPila(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var nivel by remember { mutableIntStateOf(0) }
    val carpetas = ReglasPila.CARPETAS_POR_NIVEL[nivel]
    var ventanillas by remember(nivel) { mutableStateOf(ReglasPila.inicial(carpetas)) }
    var elegida by remember(nivel) { mutableStateOf<Int?>(null) }
    var movimientos by remember(nivel) { mutableIntStateOf(0) }
    var aviso by remember(nivel) { mutableStateOf<String?>(null) }
    var deMas by remember { mutableIntStateOf(0) }
    var terminado by remember { mutableStateOf(false) }
    val resuelto = ReglasPila.resuelto(ventanillas, carpetas)

    LaunchedEffect(nivel, resuelto) {
        if (!resuelto) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.ACIERTO)
        delay(1300)
        deMas += (movimientos - ReglasPila.minimo(carpetas)).coerceAtLeast(0)
        if (nivel < ReglasPila.CARPETAS_POR_NIVEL.lastIndex) {
            nivel++
        } else if (!terminado) {
            terminado = true
            alTerminar(ReglasPila.estrellas(deMas))
        }
    }

    // Pista: el Búho hace el mejor movimiento desde como están las ventanillas (no se cuenta)
    LaunchedEffect(pistas) {
        if (pistas == 0 || resuelto || terminado) return@LaunchedEffect
        val mov = ReglasPila.solucion(ventanillas).firstOrNull() ?: return@LaunchedEffect
        ventanillas = ReglasPila.mover(ventanillas, mov)
        elegida = null
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    fun tocar(ventanilla: Int) {
        if (resuelto || terminado) return
        val desde = elegida
        when {
            desde == null -> {
                if (ventanillas[ventanilla].isEmpty()) return
                elegida = ventanilla
                aviso = null
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
            desde == ventanilla -> elegida = null
            else -> {
                val mov = Movimiento(desde, ventanilla)
                if (ReglasPila.puedeMover(ventanillas, mov)) {
                    ventanillas = ReglasPila.mover(ventanillas, mov)
                    movimientos++
                    Sonidos.reproducir(Efecto.TOQUE)
                } else {
                    aviso = "Una carpeta grande no puede ir sobre una pequeña."
                    Sonidos.reproducir(Efecto.ERROR)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
                elegida = null
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Pila ${nivel + 1}/${ReglasPila.CARPETAS_POR_NIVEL.size}")
            Marcador("Movimientos: $movimientos")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = when {
                resuelto -> "📂 ¡Todas las solicitudes en la ventanilla 3!"
                elegida == null -> "Toca la ventanilla de donde sacas la carpeta"
                else -> "Ahora toca a dónde la llevas"
            },
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = NachoGreenDark,
            textAlign = TextAlign.Center
        )
        Text(
            aviso ?: "Se puede en ${ReglasPila.minimo(carpetas)} movimientos.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Las tres ventanillas ocupan el espacio que sobra (tabletas en horizontal)
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val altoCarpeta = minOf(maxHeight / (carpetas + 3), 44.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                for (v in 0 until ReglasPila.VENTANILLAS) {
                    VentanillaPila(
                        numero = v + 1,
                        pila = ventanillas[v],
                        carpetas = carpetas,
                        elegida = elegida == v,
                        destino = v == ReglasPila.DESTINO,
                        altoCarpeta = altoCarpeta,
                        onClick = { tocar(v) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Consejo del Búho: con un número impar de carpetas, la más pequeña va primero a la ventanilla 3.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun VentanillaPila(
    numero: Int,
    pila: List<Int>,
    carpetas: Int,
    elegida: Boolean,
    destino: Boolean,
    altoCarpeta: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(10.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            .fillMaxHeight()
            .clip(forma)
            .background(if (destino) Color(0x2243A047) else Color(0x14000000))
            .border(if (elegida) 3.dp else 1.dp, if (elegida) OjosBuho else Color(0xFFBDBDBD), forma)
            .semantics { contentDescription = "Ventanilla $numero con ${pila.size} carpetas" }
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        // Las carpetas de arriba abajo; la de arriba se levanta un poco si está elegida
        pila.asReversed().forEachIndexed { i, carpeta ->
            val levantada by animateFloatAsState(if (elegida && i == 0) -10f else 0f, label = "levantar")
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = levantada.dp)
                    .fillMaxWidth(0.35f + 0.65f * carpeta / carpetas)
                    .height(altoCarpeta)
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ColoresCarpeta[(carpeta - 1) % ColoresCarpeta.size])
            ) {
                Text("📁", fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (destino) NachoYellow else Color(0xFF8D6E63))
                .padding(vertical = 4.dp)
        ) {
            Text(
                "Ventanilla $numero",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (destino) NachoGreenDark else Color.White
            )
        }
    }
}
