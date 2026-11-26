package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.theme.NachoAzul
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private enum class FaseCarrera { PREPARADOS, CORRIENDO, GANADA, PERDIDA }

private val ColorPista = Color(0xFFC0583A)
private val ColorZona = Color(0x66FDD835)
private val ColorEnredo = Color(0xFF8E24AA)

/**
 * Los relevos (capítulo 13): un 4×100 contra el Enredo. Si el Enredo llega primero, se vuelve a
 * correr; las estrellas dependen de cuántas carreras hicieron falta.
 */
@Composable
fun MinijuegoRelevos(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var intento by remember { mutableIntStateOf(1) }
    var fase by remember(intento) { mutableStateOf(FaseCarrera.PREPARADOS) }
    var cuentaAtras by remember(intento) { mutableIntStateOf(3) }
    var posicion by remember(intento) { mutableFloatStateOf(0f) }
    var velocidad by remember(intento) { mutableFloatStateOf(0f) }
    var corredor by remember(intento) { mutableIntStateOf(0) }
    var piePrevio by remember(intento) { mutableStateOf<Pie?>(null) }
    var segundos by remember(intento) { mutableFloatStateOf(0f) }
    // Segundos que el corredor se queda quieto recogiendo el testigo caído
    var recogiendo by remember(intento) { mutableFloatStateOf(0f) }
    // Los cordones amarrados (las pistas) valen para todas las carreras
    var perdidosEnredo by remember { mutableFloatStateOf(0f) }
    var mensaje by remember(intento) { mutableStateOf<String?>(null) }
    var terminado by remember { mutableStateOf(false) }
    // Tras perder, el botón de otra carrera tarda un poco en salir: está donde estaban los pies y
    // un toque rápido de más reiniciaría la carrera sin dejar ver quién ganó
    var puedeRepetir by remember(intento) { mutableStateOf(false) }

    // Cuenta atrás y carrera
    LaunchedEffect(intento) {
        for (n in 3 downTo 1) {
            cuentaAtras = n
            Sonidos.reproducir(Efecto.TOQUE)
            delay(700)
        }
        fase = FaseCarrera.CORRIENDO
        Sonidos.reproducir(Efecto.ACIERTO)
        var anterior = 0L
        while (fase == FaseCarrera.CORRIENDO) {
            withFrameNanos { ahora ->
                if (anterior != 0L) {
                    // Si la app estuvo en pausa, el salto de tiempo no debe adelantar a nadie
                    val dt = ((ahora - anterior) / 1_000_000_000f).coerceAtMost(0.05f)
                    segundos += dt
                    if (recogiendo > 0f) {
                        recogiendo -= dt
                    } else {
                        velocidad = ReglasRelevos.frenar(velocidad, dt)
                        posicion = (posicion + velocidad * dt).coerceAtMost(ReglasRelevos.META)
                    }
                    if (ReglasRelevos.seCayo(posicion, corredor)) {
                        corredor++
                        velocidad = 0f
                        piePrevio = null
                        recogiendo = ReglasRelevos.CASTIGO_CAIDA
                        mensaje = "¡Se cayó el testigo! Pásalo dentro de la zona amarilla."
                        Sonidos.reproducir(Efecto.ERROR)
                    }
                    when {
                        posicion >= ReglasRelevos.META -> fase = FaseCarrera.GANADA
                        ReglasRelevos.posicionEnredo(segundos, perdidosEnredo) >= ReglasRelevos.META ->
                            fase = FaseCarrera.PERDIDA
                    }
                }
                anterior = ahora
            }
        }
        if (fase == FaseCarrera.GANADA) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Sonidos.reproducir(Efecto.VICTORIA)
            delay(1200)
            if (!terminado) {
                terminado = true
                alTerminar(ReglasRelevos.estrellas(intento))
            }
        } else {
            Sonidos.reproducir(Efecto.DERROTA)
            delay(1000)
            puedeRepetir = true
        }
    }

    // Pista: Nacho le amarra los cordones al Enredo, que pierde un rato
    LaunchedEffect(pistas) {
        if (pistas == 0 || terminado) return@LaunchedEffect
        perdidosEnredo += ReglasRelevos.CORDONES_AMARRADOS
        mensaje = "¡Nacho le amarró los cordones al Enredo!"
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    fun pisar(pie: Pie) {
        if (fase != FaseCarrera.CORRIENDO || recogiendo > 0f) return
        val tropiezo = pie == piePrevio
        velocidad = ReglasRelevos.pisar(velocidad, pie, piePrevio)
        piePrevio = pie
        if (tropiezo) {
            mensaje = "¡Tropiezo! Alterna los pies."
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun entregar() {
        if (fase != FaseCarrera.CORRIENDO || recogiendo > 0f) return
        when (val entrega = ReglasRelevos.entregar(posicion, corredor, velocidad)) {
            Entrega.FueraDeZona -> mensaje = "Todavía no: el testigo se pasa en la zona amarilla."
            is Entrega.Valida -> {
                corredor++
                velocidad = entrega.velocidadSalida
                piePrevio = null
                val porcentaje = (entrega.velocidadSalida / ReglasRelevos.VELOCIDAD_MAXIMA * 100).roundToInt()
                mensaje = if (porcentaje >= 75) "¡Entrega perfecta!" else "Entrega lista. Más al final de la zona, sale con más impulso."
                Sonidos.reproducir(Efecto.ACIERTO)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }

    val posicionEnredo = ReglasRelevos.posicionEnredo(segundos, perdidosEnredo)
    val enZona = ReglasRelevos.enZona(posicion, corredor)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Corredor ${corredor + 1}/${ReglasRelevos.TRAMOS}")
            Marcador("Carrera $intento")
            Marcador("${"%.1f".format(segundos)} s")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = when (fase) {
                FaseCarrera.PREPARADOS -> "En sus marcas... $cuentaAtras"
                FaseCarrera.CORRIENDO -> if (enZona) "🟨 ¡Zona de entrega! Pasa el testigo" else "¡Corre! Alterna los pies"
                FaseCarrera.GANADA -> "🏁 ¡Le ganaste al Enredo!"
                FaseCarrera.PERDIDA -> "🧶 El Enredo llegó primero"
            },
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = NachoGreenDark,
            textAlign = TextAlign.Center
        )
        Text(
            mensaje ?: "Gana el que complete primero los 400 metros.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(36.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Pista(posicion = posicion, posicionEnredo = posicionEnredo)

        Spacer(modifier = Modifier.weight(1f))

        if (fase == FaseCarrera.PERDIDA) {
            BotonJuego(
                texto = "Correr otra vez",
                onClick = { intento++ },
                color = NachoGreen,
                habilitado = puedeRepetir,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val corriendo = fase == FaseCarrera.CORRIENDO
            BotonJuego(
                texto = "🤝 Pasar el testigo",
                onClick = ::entregar,
                // Se ve apagado fuera de la zona, pero se puede tocar: así se explica cuándo entregar
                color = if (enZona) NachoYellow else NachoYellow.copy(alpha = 0.4f),
                colorTexto = NachoGreenDark,
                habilitado = corriendo && corredor < ReglasRelevos.TRAMOS - 1,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // El pie que toca dar se ve más fuerte
                Pie.entries.forEach { pie ->
                    val toca = piePrevio != pie
                    BotonJuego(
                        texto = if (pie == Pie.IZQUIERDO) "👟 Izquierdo" else "Derecho 👟",
                        onClick = { pisar(pie) },
                        color = if (toca) NachoAzul else NachoAzul.copy(alpha = 0.45f),
                        habilitado = corriendo,
                        alto = 72.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** La pista de 400 m con dos carriles: el del jugador abajo y el del Enredo arriba. */
@Composable
private fun Pista(posicion: Float, posicionEnredo: Float) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(130.dp)) {
        val tamanoFicha = 34.dp
        Canvas(modifier = Modifier.fillMaxSize()) {
            val margen = tamanoFicha.toPx() / 2
            val largo = size.width - margen * 2
            fun x(metros: Float) = margen + largo * metros / ReglasRelevos.META
            drawRoundRect(ColorPista, Offset(0f, size.height * 0.15f), Size(size.width, size.height * 0.7f), CornerRadius(12.dp.toPx()))
            // Zonas de entrega de cada tramo
            for (corredor in 0 until ReglasRelevos.TRAMOS - 1) {
                val inicio = x(ReglasRelevos.inicioZona(corredor))
                val fin = x(ReglasRelevos.finTramo(corredor))
                drawRect(ColorZona, Offset(inicio, size.height * 0.15f), Size(fin - inicio, size.height * 0.7f))
            }
            // Raya entre los carriles y marcas cada 100 m
            drawLine(Color.White, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 2.dp.toPx())
            for (tramo in 0..ReglasRelevos.TRAMOS) {
                val xt = x(tramo * ReglasRelevos.LARGO_TRAMO)
                drawLine(
                    if (tramo == ReglasRelevos.TRAMOS) NachoYellow else Color.White,
                    Offset(xt, size.height * 0.15f),
                    Offset(xt, size.height * 0.85f),
                    strokeWidth = (if (tramo == ReglasRelevos.TRAMOS) 5 else 2).dp.toPx()
                )
            }
        }
        val recorrido = maxWidth - tamanoFicha
        Ficha("🧶", posicionEnredo, recorrido, tamanoFicha, y = 130.dp * 0.3f - tamanoFicha / 2, color = ColorEnredo)
        Ficha("🏃", posicion, recorrido, tamanoFicha, y = 130.dp * 0.7f - tamanoFicha / 2, color = Color.White, mirarDerecha = true)
    }
}

@Composable
private fun Ficha(emoji: String, metros: Float, recorrido: Dp, tamano: Dp, y: Dp, color: Color, mirarDerecha: Boolean = false) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .offset(x = recorrido * (metros / ReglasRelevos.META), y = y)
            .width(tamano)
            .height(tamano)
    ) {
        Text(
            emoji,
            fontSize = 24.sp,
            color = color,
            // Los emojis de corredor miran a la izquierda: se voltea para que corra hacia la meta
            modifier = if (mirarDerecha) Modifier.graphicsLayer(scaleX = -1f) else Modifier
        )
    }
}
