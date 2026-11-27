package com.unal.nachoquest.ui.screens.quiz.minijuegos

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.OjosBuho
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.TintaSello
import com.unal.nachoquest.ui.theme.NachoAzul
import com.unal.nachoquest.ui.theme.NachoGreenDark
import kotlinx.coroutines.delay

private val ColorPeriodico = Color(0xFFF3EBD8)

/**
 * Fechas en trámite (capítulo 16): se toca un titular y luego el año que le corresponde. Tres
 * rondas de cuatro titulares; cada equivocación cuenta.
 */
@Composable
fun MinijuegoFechas(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var ronda by remember { mutableIntStateOf(0) }
    val titulares = remember(ronda) { ReglasFechas.RONDAS[ronda].shuffled() }
    val anios = remember(ronda) { ReglasFechas.RONDAS[ronda].map { it.anio }.shuffled() }
    // Titular (por su índice en la ronda revuelta) -> año que ya se le devolvió
    val puestos = remember(ronda) { mutableStateMapOf<Int, Int>() }
    var elegido by remember(ronda) { mutableStateOf<Int?>(null) }
    var fallido by remember(ronda) { mutableStateOf<Int?>(null) }
    var errores by remember { mutableIntStateOf(0) }
    var terminado by remember { mutableStateOf(false) }
    val rondaCompleta = puestos.size == titulares.size

    LaunchedEffect(ronda, rondaCompleta) {
        if (!rondaCompleta) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.ACIERTO)
        delay(1300)
        if (ronda < ReglasFechas.RONDAS.lastIndex) {
            ronda++
        } else if (!terminado) {
            terminado = true
            alTerminar(ReglasFechas.estrellas(errores))
        }
    }

    LaunchedEffect(fallido) {
        if (fallido == null) return@LaunchedEffect
        delay(600)
        fallido = null
    }

    fun poner(titular: Int, anio: Int, ayuda: Boolean = false) {
        if (titular in puestos || rondaCompleta) return
        if (ReglasFechas.esCorrecta(titulares[titular], anio)) {
            puestos[titular] = anio
            elegido = null
            Sonidos.reproducir(if (ayuda) Efecto.ACIERTO else Efecto.TOQUE)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } else {
            errores++
            fallido = titular
            Sonidos.reproducir(Efecto.ERROR)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Pista: el Búho le devuelve la fecha a un titular (no cuenta como error)
    LaunchedEffect(pistas) {
        if (pistas == 0 || terminado) return@LaunchedEffect
        val titular = elegido?.takeIf { it !in puestos } ?: titulares.indices.firstOrNull { it !in puestos } ?: return@LaunchedEffect
        poner(titular, titulares[titular].anio, ayuda = true)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Periódico ${ronda + 1}/${ReglasFechas.RONDAS.size}")
            Marcador("Errores: $errores", color = if (errores > 0) TintaSello else NachoGreenDark)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when {
                rondaCompleta -> "🗞️ ¡Todas las noticias tienen fecha!"
                elegido == null -> "Toca un titular y luego su año"
                else -> "¿De qué año es esta noticia?"
            },
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = NachoGreenDark,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Los titulares se desplazan si no caben (tabletas en horizontal)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())
        ) {
            titulares.forEachIndexed { i, titular ->
                TitularPeriodico(
                    texto = titular.texto,
                    anio = puestos[i],
                    elegido = elegido == i,
                    fallido = fallido == i,
                    onClick = { if (i !in puestos && !rondaCompleta) elegido = i }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            anios.forEach { anio ->
                val usado = anio in puestos.values
                BotonJuego(
                    texto = anio.toString(),
                    onClick = { elegido?.let { poner(it, anio) } },
                    color = NachoAzul,
                    habilitado = !usado && elegido != null && !rondaCompleta,
                    alto = 56.dp,
                    tamanoTexto = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TitularPeriodico(texto: String, anio: Int?, elegido: Boolean, fallido: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(6.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (fallido) Color(0xFFFFCDD2) else ColorPeriodico)
            .border(
                width = if (elegido) 3.dp else 1.dp,
                color = if (elegido) OjosBuho else Color(0xFF9E9E9E),
                shape = forma
            )
            .clickable(enabled = anio == null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            texto,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color(0xFF212121),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // La fecha: tapada por el sello hasta que se devuelve
        Box(contentAlignment = Alignment.Center, modifier = Modifier.width(92.dp)) {
            if (anio != null) {
                Text(anio.toString(), fontFamily = FontFamily.Serif, fontWeight = FontWeight.Black, fontSize = 20.sp, color = NachoGreenDark)
            } else {
                Text(
                    "PENDIENTE",
                    color = TintaSello,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .rotate(-12f)
                        .border(2.dp, TintaSello, RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
