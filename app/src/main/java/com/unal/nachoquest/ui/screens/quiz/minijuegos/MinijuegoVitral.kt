package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

// Los colores de los vidrios: los de los cinco hilos de la memoria
private val ColoresVidrio = listOf(
    Color(0xFFE53935), // rojo
    Color(0xFF1E88E5), // azul
    Color(0xFF43A047), // verde
    Color(0xFFFDD835), // amarillo
    Color(0xFF8E24AA)  // morado
)
private val VidrioApagado = Color(0xFF37474F)
private val ColorPlomo = Color(0xFF263238)

/**
 * Enciende el vitral (capítulo 12): tres vitrales cada vez más grandes (3×3, 4×4 y 5×5). Tocar un
 * vidrio cambia ese y sus vecinos; se cuentan los toques de más sobre el mínimo posible.
 */
@Composable
fun MinijuegoVitral(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var indiceNivel by remember { mutableIntStateOf(0) }
    val nivel = ReglasVitral.NIVELES[indiceNivel]
    val minimo = remember(indiceNivel) { ReglasVitral.minimo(nivel) }
    var vitral by remember(indiceNivel) { mutableStateOf(ReglasVitral.inicial(nivel)) }
    var toques by remember(indiceNivel) { mutableIntStateOf(0) }
    // Toques de más acumulados en los niveles ya superados
    var deMas by remember { mutableIntStateOf(0) }
    var terminado by remember { mutableStateOf(false) }
    val resuelto = ReglasVitral.resuelto(vitral)

    LaunchedEffect(indiceNivel, resuelto) {
        if (!resuelto) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.ACIERTO)
        delay(1300)
        deMas += (toques - minimo).coerceAtLeast(0)
        if (indiceNivel < ReglasVitral.NIVELES.lastIndex) {
            indiceNivel++
        } else if (!terminado) {
            terminado = true
            alTerminar(ReglasVitral.estrellas(deMas))
        }
    }

    // Pista: Nacho toca un vidrio de la mejor solución desde como está el vitral (no se cuenta)
    LaunchedEffect(pistas) {
        if (pistas == 0 || terminado || resuelto) return@LaunchedEffect
        val vidrio = ReglasVitral.solucionMinima(vitral, nivel.lado).firstOrNull() ?: return@LaunchedEffect
        vitral = ReglasVitral.tocar(vitral, nivel.lado, vidrio)
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    fun tocar(vidrio: Int) {
        if (resuelto || terminado) return
        vitral = ReglasVitral.tocar(vitral, nivel.lado, vidrio)
        toques++
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        Sonidos.reproducir(Efecto.TOQUE)
    }

    val encendidos = vitral.count { it }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Vitral ${indiceNivel + 1}/${ReglasVitral.NIVELES.size}")
            Marcador("Toques: $toques")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (resuelto) "☀️ ¡El vitral brilla otra vez!" else "Enciende los ${vitral.size} vidrios · van $encendidos",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = NachoGreenDark,
            textAlign = TextAlign.Center
        )
        Text("Se puede en $minimo toques.", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))

        // El vitral toma el lado menor del espacio que sobra (tabletas en horizontal)
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            contentAlignment = Alignment.Center
        ) {
            val ancho = minOf(maxWidth, maxHeight / 1.5f)
            Vitral(
                vitral = vitral,
                lado = nivel.lado,
                resuelto = resuelto,
                ancho = ancho,
                onTocar = ::tocar
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        BotonJuego(
            texto = "Volver a empezar",
            onClick = { vitral = ReglasVitral.inicial(nivel) },
            color = Color(0xFF8D6E63),
            habilitado = !resuelto && !terminado,
            alto = 38.dp,
            tamanoTexto = 13.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Pista: tocar dos veces el mismo vidrio lo deja como estaba.",
            color = Color.Gray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** El vitral con forma de ventana de arco: los vidrios separados por el plomo. */
@Composable
private fun Vitral(vitral: List<Boolean>, lado: Int, resuelto: Boolean, ancho: Dp, onTocar: (Int) -> Unit) {
    val arco = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomStartPercent = 4, bottomEndPercent = 4)
    val marco = 8.dp
    val separacion = 3.dp
    val ladoVidrio = (ancho - marco * 2 - separacion * (lado - 1)) / lado
    // Debajo del medio punto (media anchura de alto) va el cuadro de vidrios, para que el arco no los corte
    val alto = ancho * 1.5f
    Box(
        modifier = Modifier
            .size(ancho, alto)
            .clip(arco)
            .background(ColorPlomo)
            .border(marco, if (resuelto) NachoYellow else Color(0xFF795548), arco)
            .padding(marco),
        contentAlignment = Alignment.BottomCenter
    ) {
        // El medio punto de arriba brilla cuando todo el vitral está encendido
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        if (resuelto) listOf(NachoYellow, Color(0xFFFFF59D)) else listOf(Color(0xFF455A64), ColorPlomo)
                    )
                )
        )
        Column(verticalArrangement = Arrangement.spacedBy(separacion)) {
            for (fila in 0 until lado) {
                Row(horizontalArrangement = Arrangement.spacedBy(separacion)) {
                    for (columna in 0 until lado) {
                        val vidrio = fila * lado + columna
                        Vidrio(
                            encendido = vitral[vidrio],
                            color = ColoresVidrio[(fila + columna * 2) % ColoresVidrio.size],
                            lado = ladoVidrio,
                            descripcion = "Vidrio ${fila + 1}, ${columna + 1}: ${if (vitral[vidrio]) "encendido" else "apagado"}",
                            onClick = { onTocar(vidrio) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Vidrio(encendido: Boolean, color: Color, lado: Dp, descripcion: String, onClick: () -> Unit) {
    val relleno by animateColorAsState(if (encendido) color else VidrioApagado, tween(300), label = "vidrio")
    Box(
        modifier = Modifier
            .size(lado)
            .clip(RoundedCornerShape(4.dp))
            .background(
                Brush.radialGradient(
                    if (encendido) listOf(Color.White.copy(alpha = 0.55f).compositeOver(relleno), relleno)
                    else listOf(relleno, relleno)
                )
            )
            .semantics { contentDescription = descripcion }
            .clickable(onClick = onClick)
    )
}
