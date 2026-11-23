package com.unal.nachoquest.ui.screens.quiz.minijuegos

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.components.Olvido
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.sacudir
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max

private val FondoLaberinto = Color(0xFF2B3138)
private val Piso = Color(0xFFE9E1CC)
private val PisoRecordado = Color(0xFFB9B3A3)
private val Pared = Color(0xFF3E4A3D)
private val Bruma = Color(0xFF59606A)

/**
 * Laberinto en la bruma: Nacho solo ve las celdas que tiene alrededor; las que ya recorrió quedan
 * a la vista. Se mueve con las flechas o deslizando el dedo sobre el laberinto. La salida, donde
 * espera el último Eco, siempre se ve.
 */
@Composable
fun MinijuegoLaberinto(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val laberinto = remember { ReglasLaberinto.generar() }
    val minimo = remember { laberinto.caminoASalida(laberinto.entrada).size }
    var posicion by remember { mutableIntStateOf(laberinto.entrada) }
    var pasos by remember { mutableIntStateOf(0) }
    var choques by remember { mutableIntStateOf(0) }
    val visitadas = remember { mutableStateListOf(laberinto.entrada) }
    // Celdas que iluminó el cencerro (la pista)
    var rastro by remember { mutableStateOf(emptyList<Int>()) }
    val llego = posicion == laberinto.salida

    LaunchedEffect(pistas) {
        if (pistas == 0 || llego) return@LaunchedEffect
        rastro = laberinto.caminoASalida(posicion).take(ReglasLaberinto.PASOS_POR_PISTA)
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    LaunchedEffect(llego) {
        if (!llego) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.ACIERTO)
        delay(900)
        alTerminar(ReglasLaberinto.estrellas(pasos, minimo))
    }

    fun mover(paso: Paso) {
        if (posicion == laberinto.salida) return
        val destino = laberinto.mover(posicion, paso)
        if (destino == null) {
            choques++
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            return
        }
        posicion = destino
        pasos++
        if (destino !in visitadas) visitadas.add(destino)
        rastro = rastro - destino
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        Sonidos.reproducir(Efecto.TOQUE)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Pasos: $pasos")
            Marcador(if (llego) "🔔 ¡Encontraste al Eco!" else "Choques: $choques")
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Cuadrado que se encoge si no cabe con la cruceta (pantallas bajas)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f, fill = false)
                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                .sacudir(choques)
                .clip(RoundedCornerShape(14.dp))
                .background(FondoLaberinto)
                .pointerInput(Unit) {
                    var arrastre = Offset.Zero
                    detectDragGestures(
                        onDragStart = { arrastre = Offset.Zero },
                        onDragEnd = {
                            val umbral = 24.dp.toPx()
                            when {
                                max(abs(arrastre.x), abs(arrastre.y)) < umbral -> Unit
                                abs(arrastre.x) > abs(arrastre.y) -> mover(if (arrastre.x > 0) Paso.DERECHA else Paso.IZQUIERDA)
                                else -> mover(if (arrastre.y > 0) Paso.ABAJO else Paso.ARRIBA)
                            }
                        }
                    ) { cambio, desplazamiento ->
                        cambio.consume()
                        arrastre += desplazamiento
                    }
                }
        ) {
            val lado = laberinto.lado
            val celda = maxWidth / lado
            val filaActual = laberinto.fila(posicion)
            val columnaActual = laberinto.columna(posicion)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val t = size.width / lado
                val grosor = t * 0.12f
                for (i in 0 until lado * lado) {
                    val f = laberinto.fila(i)
                    val c = laberinto.columna(i)
                    val origen = Offset(c * t, f * t)
                    val cerca = max(abs(f - filaActual), abs(c - columnaActual)) <= ReglasLaberinto.ALCANCE_VISTA
                    val recordada = i in visitadas
                    if (!cerca && !recordada && i != laberinto.salida) {
                        drawRect(Bruma, origen, Size(t, t))
                        // El sonido del cencerro atraviesa la bruma: la pista se ve aunque la celda no
                        if (i in rastro) {
                            drawCircle(NachoYellow, radius = t * 0.16f, center = Offset(origen.x + t / 2, origen.y + t / 2))
                        }
                        continue
                    }
                    drawRect(if (cerca || i == laberinto.salida) Piso else PisoRecordado, origen, Size(t, t))
                    if (i in rastro) {
                        drawCircle(NachoYellow, radius = t * 0.16f, center = Offset(origen.x + t / 2, origen.y + t / 2))
                    }
                    // Paredes de la celda
                    if (!laberinto.puedeIr(i, Paso.ARRIBA)) drawLine(Pared, origen, Offset(origen.x + t, origen.y), grosor)
                    if (!laberinto.puedeIr(i, Paso.ABAJO)) drawLine(Pared, Offset(origen.x, origen.y + t), Offset(origen.x + t, origen.y + t), grosor)
                    if (!laberinto.puedeIr(i, Paso.IZQUIERDA)) drawLine(Pared, origen, Offset(origen.x, origen.y + t), grosor)
                    if (!laberinto.puedeIr(i, Paso.DERECHA)) drawLine(Pared, Offset(origen.x + t, origen.y), Offset(origen.x + t, origen.y + t), grosor)
                }
            }

            // El último Eco espera en la salida
            Box(
                modifier = Modifier
                    .offset(x = celda * laberinto.columna(laberinto.salida), y = celda * laberinto.fila(laberinto.salida))
                    .size(celda),
                contentAlignment = Alignment.Center
            ) {
                Olvido(modifier = Modifier.size(celda * 0.9f))
            }

            val x by animateDpAsState(celda * columnaActual, tween(140), label = "nachoX")
            val y by animateDpAsState(celda * filaActual, tween(140), label = "nachoY")
            Nacho(
                modifier = Modifier
                    .offset(x = x, y = y)
                    .size(celda),
                expresion = if (llego) Expresion.FELIZ else Expresion.DECIDIDO,
                saltando = llego
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (llego) "¡El último Eco te escuchó!" else "Desliza el dedo sobre el laberinto o usa las flechas.",
            color = if (llego) NachoGreenDark else Color.Gray,
            fontWeight = if (llego) FontWeight.ExtraBold else FontWeight.Normal,
            fontSize = if (llego) 17.sp else 13.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Cruceta
        val tamano = 54.dp
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BotonJuego(texto = "▲", onClick = { mover(Paso.ARRIBA) }, habilitado = !llego, alto = tamano, modifier = Modifier.width(tamano * 1.4f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                BotonJuego(texto = "◀", onClick = { mover(Paso.IZQUIERDA) }, habilitado = !llego, alto = tamano, modifier = Modifier.width(tamano * 1.4f))
                BotonJuego(texto = "▼", onClick = { mover(Paso.ABAJO) }, habilitado = !llego, alto = tamano, modifier = Modifier.width(tamano * 1.4f))
                BotonJuego(texto = "▶", onClick = { mover(Paso.DERECHA) }, habilitado = !llego, alto = tamano, modifier = Modifier.width(tamano * 1.4f))
            }
        }
    }
}
