package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.theme.NachoAzul
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

private val ColorAgua = Color(0xFF42A5F5)
private val ColorAguaOscura = Color(0xFF1565C0)

/**
 * Agua exacta (capítulo 10): tres niveles del acertijo de las jarras. Se cuenta cuántos
 * movimientos de más hace el jugador sobre el mínimo posible.
 */
@Composable
fun MinijuegoTanques(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var indiceNivel by remember { mutableIntStateOf(0) }
    val nivel = ReglasTanques.NIVELES[indiceNivel]
    val minimo = remember(indiceNivel) { ReglasTanques.minimo(nivel) }
    var estado by remember(indiceNivel) { mutableStateOf(EstadoTanques()) }
    var movimientos by remember(indiceNivel) { mutableIntStateOf(0) }
    // Movimientos de más acumulados en los niveles ya superados
    var deMas by remember { mutableIntStateOf(0) }
    var terminado by remember { mutableStateOf(false) }
    val resuelto = ReglasTanques.resuelto(nivel, estado)

    LaunchedEffect(indiceNivel, resuelto) {
        if (!resuelto) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.ACIERTO)
        delay(1300)
        deMas += (movimientos - minimo).coerceAtLeast(0)
        if (indiceNivel < ReglasTanques.NIVELES.lastIndex) {
            indiceNivel++
        } else if (!terminado) {
            terminado = true
            alTerminar(ReglasTanques.estrellas(deMas))
        }
    }

    // Pista: Nacho hace el mejor movimiento posible desde como están los tanques (no se cuenta)
    LaunchedEffect(pistas) {
        if (pistas == 0 || terminado || resuelto) return@LaunchedEffect
        val paso = ReglasTanques.caminoMinimo(nivel, estado).firstOrNull() ?: return@LaunchedEffect
        estado = ReglasTanques.aplicar(nivel, estado, paso)
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    fun hacer(accion: AccionTanque) {
        if (resuelto || terminado) return
        val nuevo = ReglasTanques.aplicar(nivel, estado, accion)
        if (nuevo == estado) return
        estado = nuevo
        movimientos++
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        Sonidos.reproducir(Efecto.TOQUE)
    }

    val capacidadMaxima = maxOf(nivel.capacidadA, nivel.capacidadB)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Nivel ${indiceNivel + 1}/${ReglasTanques.NIVELES.size}")
            Marcador("Movimientos: $movimientos")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (resuelto) "💧 ¡Exacto! ${nivel.objetivo} litros" else "Deja exactamente ${nivel.objetivo} litros en uno de los tanques",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = NachoGreenDark,
            textAlign = TextAlign.Center
        )
        Text("Se puede en $minimo movimientos.", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))

        // Los tanques ocupan el espacio que sobra y se encogen si no caben (tabletas en horizontal)
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
        ) {
            ColumnaTanque(
                nombre = "A",
                litros = estado.a,
                capacidad = nivel.capacidadA,
                capacidadMaxima = capacidadMaxima,
                acierto = resuelto && estado.a == nivel.objetivo,
                textoPasar = "Pasar a B ➡",
                onLlenar = { hacer(AccionTanque.LLENAR_A) },
                onVaciar = { hacer(AccionTanque.VACIAR_A) },
                onPasar = { hacer(AccionTanque.PASAR_A_B) },
                habilitado = !resuelto,
                modifier = Modifier.weight(1f)
            )
            ColumnaTanque(
                nombre = "B",
                litros = estado.b,
                capacidad = nivel.capacidadB,
                capacidadMaxima = capacidadMaxima,
                acierto = resuelto && estado.b == nivel.objetivo,
                textoPasar = "⬅ Pasar a A",
                onLlenar = { hacer(AccionTanque.LLENAR_B) },
                onVaciar = { hacer(AccionTanque.VACIAR_B) },
                onPasar = { hacer(AccionTanque.PASAR_B_A) },
                habilitado = !resuelto,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Pista: al pasar agua, se pasa hasta que uno se vacíe o el otro se llene.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ColumnaTanque(
    nombre: String,
    litros: Int,
    capacidad: Int,
    capacidadMaxima: Int,
    acierto: Boolean,
    textoPasar: String,
    onLlenar: () -> Unit,
    onVaciar: () -> Unit,
    onPasar: () -> Unit,
    habilitado: Boolean,
    modifier: Modifier = Modifier
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text("Tanque $nombre · $capacidad L", fontWeight = FontWeight.Bold, color = NachoGreenDark, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Tanque(litros, capacidad, capacidadMaxima, acierto)
        Text(
            "$litros L",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = if (acierto) NachoGreenDark else ColorAguaOscura,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        BotonesTanque(textoPasar, onLlenar, onVaciar, onPasar, habilitado)
    }
}

@Composable
private fun ColumnScope.Tanque(litros: Int, capacidad: Int, capacidadMaxima: Int, acierto: Boolean) {
    val nivelAgua by animateFloatAsState(litros.toFloat(), tween(450), label = "agua")
    Box(
        modifier = Modifier
            .weight(1f, fill = false)
            .aspectRatio(0.62f, matchHeightConstraintsFirst = true)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Cada tanque mide según su capacidad: el más grande ocupa todo el alto
            val alto = size.height * capacidad / capacidadMaxima
            val ancho = size.width * 0.78f
            val esquina = Offset((size.width - ancho) / 2, size.height - alto)
            val radio = CornerRadius(10.dp.toPx())
            drawRoundRect(Color(0x22FFFFFF), esquina, Size(ancho, alto), radio)
            // Agua
            val altoAgua = alto * nivelAgua / capacidad
            if (altoAgua > 0f) {
                drawRoundRect(
                    Brush.verticalGradient(listOf(ColorAgua, ColorAguaOscura)),
                    topLeft = Offset(esquina.x, size.height - altoAgua),
                    size = Size(ancho, altoAgua),
                    cornerRadius = radio
                )
            }
            // Una raya por cada litro
            for (litro in 1 until capacidad) {
                val y = size.height - alto * litro / capacidad
                drawLine(Color(0x55263238), Offset(esquina.x, y), Offset(esquina.x + ancho * 0.18f, y), strokeWidth = 2f)
            }
            drawRoundRect(
                if (acierto) NachoYellow else Color(0xFF90A4AE),
                esquina,
                Size(ancho, alto),
                radio,
                style = Stroke(width = (if (acierto) 5 else 3).dp.toPx())
            )
        }
    }
}

@Composable
private fun BotonesTanque(
    textoPasar: String,
    onLlenar: () -> Unit,
    onVaciar: () -> Unit,
    onPasar: () -> Unit,
    habilitado: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            BotonJuego(
                texto = "Llenar",
                onClick = onLlenar,
                color = ColorAgua,
                habilitado = habilitado,
                alto = 38.dp,
                tamanoTexto = 13.sp,
                modifier = Modifier.weight(1f)
            )
            BotonJuego(
                texto = "Vaciar",
                onClick = onVaciar,
                color = Color(0xFF8D6E63),
                habilitado = habilitado,
                alto = 38.dp,
                tamanoTexto = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }
        BotonJuego(
            texto = textoPasar,
            onClick = onPasar,
            color = NachoAzul,
            habilitado = habilitado,
            alto = 38.dp,
            tamanoTexto = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
