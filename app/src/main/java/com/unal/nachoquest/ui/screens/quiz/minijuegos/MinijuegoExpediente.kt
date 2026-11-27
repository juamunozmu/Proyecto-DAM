package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.TintaSello
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import kotlinx.coroutines.delay

private val ColorHoja = Color(0xFFFFFDF5)
private const val COLUMNAS = 3

/**
 * El último sello (capítulo 18): 24 sellos caen sobre las nueve páginas del Expediente. Se toca
 * cada página sellada para aprobarla antes de que el sello se seque.
 */
@Composable
fun MinijuegoExpediente(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var empezado by remember { mutableStateOf(false) }
    // Página sellada -> segundos que le quedan antes de secarse
    val selladas = remember { mutableStateMapOf<Int, Float>() }
    // Páginas recién aprobadas, para mostrar el «APROBADO» un momento
    val aprobadas = remember { mutableStateMapOf<Int, Float>() }
    var sellosLanzados by remember { mutableIntStateOf(0) }
    var hastaElSiguiente by remember { mutableFloatStateOf(0.6f) }
    var errores by remember { mutableIntStateOf(0) }
    var terminado by remember { mutableStateOf(false) }
    val todoLanzado = sellosLanzados >= ReglasExpediente.SELLOS

    LaunchedEffect(empezado) {
        if (!empezado) return@LaunchedEffect
        var anterior = 0L
        while (!terminado) {
            withFrameNanos { ahora ->
                if (anterior != 0L) {
                    val dt = ((ahora - anterior) / 1_000_000_000f).coerceAtMost(0.05f)
                    // Los sellos que se secan sin aprobar cuentan como error
                    for ((pagina, queda) in selladas.toMap()) {
                        if (queda - dt <= 0f) {
                            selladas.remove(pagina)
                            errores++
                            Sonidos.reproducir(Efecto.ERROR)
                        } else {
                            selladas[pagina] = queda - dt
                        }
                    }
                    for ((pagina, queda) in aprobadas.toMap()) {
                        if (queda - dt <= 0f) aprobadas.remove(pagina) else aprobadas[pagina] = queda - dt
                    }
                    if (sellosLanzados < ReglasExpediente.SELLOS) {
                        hastaElSiguiente -= dt
                        if (hastaElSiguiente <= 0f) {
                            ReglasExpediente.paginaLibre(selladas.keys)?.let { pagina ->
                                selladas[pagina] = ReglasExpediente.duracion(sellosLanzados)
                                aprobadas.remove(pagina)
                                Sonidos.reproducir(Efecto.TOQUE)
                            }
                            hastaElSiguiente = ReglasExpediente.intervalo(sellosLanzados)
                            sellosLanzados++
                        }
                    }
                }
                anterior = ahora
            }
            if (sellosLanzados >= ReglasExpediente.SELLOS && selladas.isEmpty()) {
                terminado = true
            }
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.VICTORIA)
        delay(900)
        alTerminar(ReglasExpediente.estrellas(errores))
    }

    fun aprobar(pagina: Int) {
        if (pagina !in selladas || terminado) return
        selladas.remove(pagina)
        aprobadas[pagina] = 0.5f
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    // Pista: el Búho aprueba de un aletazo todas las páginas selladas
    LaunchedEffect(pistas) {
        if (pistas == 0 || !empezado || terminado) return@LaunchedEffect
        selladas.keys.toList().forEach { aprobar(it) }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Sellos: ${minOf(sellosLanzados, ReglasExpediente.SELLOS)}/${ReglasExpediente.SELLOS}")
            Marcador("Se secaron: $errores", color = if (errores > 0) TintaSello else NachoGreenDark)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = when {
                terminado -> "✅ ¡Todo el Expediente aprobado!"
                !empezado -> "El Sello va a sellar el Expediente por última vez"
                todoLanzado -> "¡Ya no le quedan sellos! Aprueba las últimas páginas"
                else -> "Toca las páginas selladas antes de que se sequen"
            },
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = NachoGreenDark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Las nueve páginas toman el lado que quepa (tabletas en horizontal)
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            if (!empezado) {
                BotonJuego(
                    texto = "Abrir el Expediente",
                    prefijo = "📂",
                    onClick = { empezado = true },
                    color = NachoGreen
                )
            } else {
                val separacion = 10.dp
                val lado = minOf(
                    (maxWidth - separacion * (COLUMNAS - 1)) / COLUMNAS,
                    (maxHeight - separacion * (COLUMNAS - 1)) / COLUMNAS
                )
                Column(verticalArrangement = Arrangement.spacedBy(separacion)) {
                    for (fila in 0 until COLUMNAS) {
                        Row(horizontalArrangement = Arrangement.spacedBy(separacion)) {
                            for (columna in 0 until COLUMNAS) {
                                val pagina = fila * COLUMNAS + columna
                                Pagina(
                                    numero = pagina + 1,
                                    queda = selladas[pagina],
                                    aprobada = pagina in aprobadas,
                                    lado = lado,
                                    onClick = { aprobar(pagina) }
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Una página aprobada ya no se pierde: queda guardada en la memoria.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Pagina(numero: Int, queda: Float?, aprobada: Boolean, lado: Dp, onClick: () -> Unit) {
    val forma = RoundedCornerShape(8.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(lado)
            .clip(forma)
            .background(ColorHoja)
            .border(if (queda != null) 3.dp else 1.dp, if (queda != null) TintaSello else Color(0xFFBDBDBD), forma)
            .semantics { contentDescription = if (queda != null) "Página $numero sellada" else "Página $numero" }
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Text("Página $numero", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.align(Alignment.TopStart))
        when {
            queda != null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "PENDIENTE",
                    color = TintaSello,
                    fontWeight = FontWeight.Black,
                    fontSize = (lado.value * 0.12f).sp,
                    maxLines = 1,
                    modifier = Modifier
                        .rotate(-14f)
                        .border(2.dp, TintaSello, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Lo que le falta al sello para secarse
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFFFCDD2))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((queda / ReglasExpediente.DURACION_PRIMERO).coerceIn(0f, 1f))
                            .height(5.dp)
                            .background(TintaSello)
                    )
                }
            }
            aprobada -> Text("✅", fontSize = (lado.value * 0.3f).sp)
            else -> Text("📄", fontSize = (lado.value * 0.3f).sp)
        }
    }
}
