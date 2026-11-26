package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.sacudir
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.sin

// Alto / ancho de cada fotograma (proporción 4:3, como la pantalla de un televisor viejo)
private const val PROPORCION_FOTOGRAMA = 0.75f
private const val COLUMNAS_PILA = 3
private val ESPACIO = 8.dp
private val ANCHO_MAXIMO_LINEA = 110.dp
private val ColorCinta = Color(0xFF1C1C1C)

/**
 * Arma la película (capítulo 9): el Enredo cortó tres cortos y revolvió sus fotogramas. Hay que
 * tocarlos en orden para pegarlos en la línea de tiempo. Al completar cada corto, se proyecta.
 */
@Composable
fun MinijuegoMontaje(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var ronda by remember { mutableIntStateOf(0) }
    val pelicula = ReglasMontaje.PELICULAS[ronda]
    val pila = remember(ronda) { ReglasMontaje.revolver(pelicula.fotogramas) }
    var colocados by remember(ronda) { mutableIntStateOf(0) }
    var errores by remember { mutableIntStateOf(0) }
    // Cuántas veces se ha tocado mal cada fotograma: cada vez que sube, ese fotograma se sacude
    val sacudidas = remember(ronda) { mutableStateMapOf<Int, Int>() }
    // Fotograma iluminado mientras se proyecta el corto terminado (-1: ninguno)
    var proyectando by remember(ronda) { mutableIntStateOf(-1) }
    var terminado by remember { mutableStateOf(false) }
    val completa = colocados == pelicula.fotogramas

    LaunchedEffect(ronda, completa) {
        if (!completa) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.ACIERTO)
        // Se proyecta dos veces, como un folioscopio
        repeat(2) {
            for (i in 0 until pelicula.fotogramas) {
                proyectando = i
                delay(170)
            }
        }
        proyectando = -1
        delay(450)
        if (ronda < ReglasMontaje.PELICULAS.lastIndex) {
            ronda++
        } else if (!terminado) {
            terminado = true
            alTerminar(ReglasMontaje.estrellas(errores))
        }
    }

    // Pista: Nacho pega el fotograma que sigue
    LaunchedEffect(pistas) {
        if (pistas == 0 || terminado || colocados >= pelicula.fotogramas) return@LaunchedEffect
        colocados++
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    fun tocar(fotograma: Int) {
        if (completa || fotograma < colocados) return
        if (ReglasMontaje.esElSiguiente(fotograma, colocados)) {
            colocados++
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            Sonidos.reproducir(Efecto.TOQUE)
        } else {
            errores++
            sacudidas[fotograma] = (sacudidas[fotograma] ?: 0) + 1
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Sonidos.reproducir(Efecto.ERROR)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Película ${ronda + 1}/${ReglasMontaje.PELICULAS.size}")
            Marcador("Errores: $errores")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("🎬 ${pelicula.titulo}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = NachoGreenDark)
        Spacer(modifier = Modifier.height(10.dp))

        Etiqueta("LÍNEA DE TIEMPO")
        BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val n = pelicula.fotogramas
            val ancho = minOf((maxWidth - ESPACIO * (n - 1)) / n, ANCHO_MAXIMO_LINEA)
            Row(horizontalArrangement = Arrangement.spacedBy(ESPACIO)) {
                for (i in 0 until n) {
                    if (i < colocados) {
                        Fotograma(pelicula, i, ancho, resaltado = proyectando == i)
                    } else {
                        Hueco(numero = i + 1, ancho = ancho)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Etiqueta(if (completa) "▶ PROYECTANDO..." else "FOTOGRAMAS REVUELTOS")

        // La pila cabe a lo ancho y a lo alto (tabletas en horizontal); los fotogramas ya pegados
        // dejan su lugar vacío para que los demás no cambien de sitio
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            contentAlignment = Alignment.TopCenter
        ) {
            val filas = ceil(pila.size / COLUMNAS_PILA.toFloat()).toInt()
            val ancho = minOf(
                (maxWidth - ESPACIO * (COLUMNAS_PILA - 1)) / COLUMNAS_PILA,
                (maxHeight - ESPACIO * (filas - 1)) / filas / PROPORCION_FOTOGRAMA
            )
            Column(verticalArrangement = Arrangement.spacedBy(ESPACIO)) {
                pila.chunked(COLUMNAS_PILA).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(ESPACIO)) {
                        fila.forEach { fotograma ->
                            if (fotograma >= colocados) {
                                Fotograma(
                                    pelicula = pelicula,
                                    indice = fotograma,
                                    ancho = ancho,
                                    modifier = Modifier
                                        .sacudir(sacudidas[fotograma] ?: 0)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            enabled = !completa
                                        ) { tocar(fotograma) }
                                )
                            } else {
                                Spacer(modifier = Modifier.size(ancho, ancho * PROPORCION_FOTOGRAMA))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Pista: busca qué cambia de un fotograma al siguiente.",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        texto,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Gray,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

/** Espacio vacío de la línea de tiempo, con el número del fotograma que va ahí. */
@Composable
private fun Hueco(numero: Int, ancho: Dp) {
    Box(
        modifier = Modifier.size(ancho, ancho * PROPORCION_FOTOGRAMA),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = Color(0xFFB9B4A0),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
            )
        }
        Text("$numero", color = Color(0xFFB9B4A0), fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

/** Un fotograma de película: la escena con el borde de cinta y sus perforaciones. */
@Composable
private fun Fotograma(
    pelicula: PeliculaMontaje,
    indice: Int,
    ancho: Dp,
    modifier: Modifier = Modifier,
    resaltado: Boolean = false
) {
    val forma = RoundedCornerShape(6.dp)
    Canvas(
        modifier = modifier
            .size(ancho, ancho * PROPORCION_FOTOGRAMA)
            .clip(forma)
            .then(if (resaltado) Modifier.border(3.dp, NachoYellow, forma) else Modifier)
    ) {
        drawRect(ColorCinta)
        // Perforaciones de la cinta, arriba y abajo
        val franja = size.height * 0.12f
        val hueco = Size(size.width * 0.06f, franja * 0.5f)
        var x = size.width * 0.04f
        while (x < size.width) {
            listOf(franja * 0.25f, size.height - franja * 0.75f).forEach { y ->
                drawRoundRect(Color(0xFFE9E4D2), Offset(x, y), hueco, CornerRadius(2f))
            }
            x += size.width * 0.13f
        }
        val margen = size.width * 0.03f
        val escena = Size(size.width - margen * 2, size.height - franja * 2)
        translate(margen, franja) {
            clipRect(0f, 0f, escena.width, escena.height) {
                val t = indice / (pelicula.fotogramas - 1).toFloat()
                when (pelicula) {
                    PeliculaMontaje.AMANECER -> dibujarAmanecer(t, escena)
                    PeliculaMontaje.SEMILLA -> dibujarSemilla(indice, escena)
                    PeliculaMontaje.GOL -> dibujarGol(t, escena)
                }
            }
        }
    }
}

/** El sol sale detrás de los edificios blancos y el cielo pasa de noche a día. */
private fun DrawScope.dibujarAmanecer(t: Float, area: Size) {
    val (w, h) = area
    drawRect(
        Brush.verticalGradient(
            listOf(
                lerp(Color(0xFF141B33), Color(0xFF6EC6FF), t),
                lerp(Color(0xFF3A2A4A), Color(0xFFCFEFFF), t)
            )
        ),
        size = area
    )
    // Estrellas que se apagan
    if (t < 0.5f) {
        val brillo = 1f - t * 2f
        listOf(0.12f to 0.15f, 0.3f to 0.3f, 0.55f to 0.12f, 0.8f to 0.25f, 0.68f to 0.4f).forEach { (ex, ey) ->
            drawCircle(Color.White.copy(alpha = brillo), radius = h * 0.025f, center = Offset(w * ex, h * ey))
        }
    }
    // El sol sube desde detrás del horizonte
    val ySol = h * (0.95f - 0.7f * t)
    drawCircle(lerp(Color(0xFFFF7A3D), Color(0xFFFFD54F), t), radius = h * 0.14f, center = Offset(w * 0.72f, ySol))
    // Edificios blancos de la Ciudad Blanca y el pasto
    val suelo = h * 0.72f
    listOf(0.05f to 0.35f, 0.22f to 0.5f, 0.42f to 0.28f).forEach { (inicio, alto) ->
        drawRect(
            lerp(Color(0xFF6B6F80), Color(0xFFF4F1E8), t),
            topLeft = Offset(w * inicio, suelo - h * alto),
            size = Size(w * 0.16f, h * alto)
        )
    }
    drawRect(lerp(Color(0xFF1F3A1F), Color(0xFF6FBF5A), t), topLeft = Offset(0f, suelo), size = Size(w, h - suelo))
}

/** Una semilla que se vuelve flor, en cinco etapas. */
private fun DrawScope.dibujarSemilla(etapa: Int, area: Size) {
    val (w, h) = area
    drawRect(Color(0xFFDDF1FF), size = area)
    val tallo = Color(0xFF3E8E41)
    val base = Offset(w * 0.5f, h * 0.7f)
    // Matera y tierra
    val matera = Path().apply {
        moveTo(w * 0.3f, h * 0.7f)
        lineTo(w * 0.7f, h * 0.7f)
        lineTo(w * 0.63f, h * 0.98f)
        lineTo(w * 0.37f, h * 0.98f)
        close()
    }
    drawPath(matera, Color(0xFFC2673E))
    drawRect(Color(0xFF6D4C41), topLeft = Offset(w * 0.3f, h * 0.66f), size = Size(w * 0.4f, h * 0.06f))

    if (etapa == 0) {
        drawOval(Color(0xFF8D6E63), topLeft = Offset(base.x - w * 0.03f, base.y - h * 0.09f), size = Size(w * 0.06f, h * 0.06f))
        return
    }
    val alturas = listOf(0f, 0.14f, 0.28f, 0.42f, 0.5f)
    val tope = Offset(base.x, base.y - h * 0.04f - h * alturas[etapa])
    drawLine(tallo, Offset(base.x, base.y - h * 0.04f), tope, strokeWidth = w * 0.025f, cap = StrokeCap.Round)
    // Hojas: dos al principio, cuatro cuando la planta ya está grande
    val hojas = if (etapa >= 3) 2 else 1
    for (par in 0 until hojas) {
        val y = base.y - h * 0.04f - h * alturas[etapa] * (0.45f + par * 0.3f)
        val tam = Size(w * (0.06f + etapa * 0.015f), h * 0.07f)
        drawOval(tallo, topLeft = Offset(base.x - tam.width, y - tam.height / 2), size = tam)
        drawOval(tallo, topLeft = Offset(base.x, y - tam.height / 2), size = tam)
    }
    when (etapa) {
        3 -> drawCircle(Color(0xFF7CB342), radius = w * 0.035f, center = tope)
        4 -> {
            for (p in 0 until 6) {
                val angulo = p * Math.PI / 3
                val centro = Offset(tope.x + (kotlin.math.cos(angulo) * w * 0.05f).toFloat(), tope.y + (sin(angulo) * w * 0.05f).toFloat())
                drawCircle(Color(0xFFF06292), radius = w * 0.04f, center = centro)
            }
            drawCircle(Color(0xFFFFD54F), radius = w * 0.035f, center = tope)
        }
    }
}

/** El balón avanza desde la izquierda, sube y entra al arco. */
private fun DrawScope.dibujarGol(t: Float, area: Size) {
    val (w, h) = area
    drawRect(Color(0xFF4CAF50), size = area)
    // Franjas del pasto y línea del área
    for (i in 0 until 4) {
        drawRect(Color(0xFF43A047), topLeft = Offset(w * i * 0.25f, 0f), size = Size(w * 0.125f, h))
    }
    // Arco a la derecha, con su red
    val arco = Offset(w * 0.8f, h * 0.3f)
    val tamArco = Size(w * 0.17f, h * 0.45f)
    for (k in 1..4) {
        val y = arco.y + tamArco.height * k / 5
        drawLine(Color.White.copy(alpha = 0.5f), Offset(arco.x, y), Offset(arco.x + tamArco.width, y), strokeWidth = 1.5f)
    }
    drawRect(Color.White, topLeft = arco, size = tamArco, style = Stroke(width = w * 0.015f))
    // El balón: avanza siempre hacia la derecha y hace una curva hacia arriba
    val x = w * (0.1f + 0.78f * t)
    val y = h * (0.78f - 0.35f * sin(t * Math.PI.toFloat() * 0.9f))
    val radio = h * 0.1f
    drawCircle(Color.White, radius = radio, center = Offset(x, y))
    drawCircle(Color(0xFF263238), radius = radio * 0.4f, center = Offset(x, y))
    if (t >= 1f) {
        // ¡Gol!: la red se infla
        drawCircle(Color(0x66FFFFFF), radius = radio * 2.2f, center = Offset(x, y))
    }
}
