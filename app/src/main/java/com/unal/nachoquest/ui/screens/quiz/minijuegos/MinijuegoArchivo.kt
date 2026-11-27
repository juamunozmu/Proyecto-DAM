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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.OjosBuho
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import kotlinx.coroutines.delay

private val ColorCaja = Color(0xFFBCAAA4)
private val ColorCajaAbierta = Color(0xFFFFF8E1)
private val ColorCajaDescartada = Color(0xFF8D8D8D)
private const val COLUMNAS_MAXIMAS = 8

/**
 * Busca el folio (capítulo 15): tres estantes del Archivo con 7, 15 y 31 cajas en orden. Se
 * cuentan las aperturas de más sobre las que necesita la búsqueda binaria.
 */
@Composable
fun MinijuegoArchivo(pistas: Int, alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var indiceEstante by remember { mutableIntStateOf(0) }
    val cajas = ReglasArchivo.CAJAS_POR_ESTANTE[indiceEstante]
    val estante = remember(indiceEstante) { ReglasArchivo.estante(cajas) }
    val abiertas = remember(indiceEstante) { mutableStateListOf<Int>() }
    // La caja que señala el Búho cuando se pide una pista
    var senalada by remember(indiceEstante) { mutableStateOf<Int?>(null) }
    var deMas by remember { mutableIntStateOf(0) }
    var terminado by remember { mutableStateOf(false) }
    val rango = ReglasArchivo.rangoPosible(estante, abiertas)
    val encontrado = abiertas.any { it == estante.objetivo }
    val ultima = abiertas.lastOrNull()

    LaunchedEffect(indiceEstante, encontrado) {
        if (!encontrado) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        Sonidos.reproducir(Efecto.ACIERTO)
        delay(1300)
        deMas += (abiertas.size - ReglasArchivo.optimo(cajas)).coerceAtLeast(0)
        if (indiceEstante < ReglasArchivo.CAJAS_POR_ESTANTE.lastIndex) {
            indiceEstante++
        } else if (!terminado) {
            terminado = true
            alTerminar(ReglasArchivo.estrellas(deMas))
        }
    }

    // Pista: el Búho señala la caja del medio de las que quedan (abrirla sí cuenta)
    LaunchedEffect(pistas) {
        if (pistas == 0 || encontrado || terminado) return@LaunchedEffect
        senalada = ReglasArchivo.delMedio(rango)
        Sonidos.reproducir(Efecto.ACIERTO)
    }

    fun abrir(caja: Int) {
        if (encontrado || terminado || caja in abiertas) return
        abiertas.add(caja)
        senalada = null
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (caja != estante.objetivo) Sonidos.reproducir(Efecto.TOQUE)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Estante ${indiceEstante + 1}/${ReglasArchivo.CAJAS_POR_ESTANTE.size}")
            Marcador("Cajas abiertas: ${abiertas.size}")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (encontrado) "📄 ¡Folio ${estante.folioBuscado} encontrado!" else "Busca el folio N.º ${estante.folioBuscado}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = NachoGreenDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = when {
                encontrado -> "Lo encontraste abriendo ${abiertas.size} cajas."
                ultima == null -> "Las cajas están en orden. Abre una y sabrás si el folio está antes o después."
                ReglasArchivo.comparar(estante, ultima) == Comparacion.ANTES ->
                    "La caja ${estante.numeros[ultima]} es mayor: el folio está ANTES ⬅"
                else -> "La caja ${estante.numeros[ultima]} es menor: el folio está DESPUÉS ➡"
            },
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            minLines = 2
        )
        Text(
            "Con buena estrategia bastan ${ReglasArchivo.optimo(cajas)} aperturas.",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Las cajas, leídas de izquierda a derecha y de arriba abajo; toman el lado que quepa
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val columnas = minOf(cajas, COLUMNAS_MAXIMAS)
            val filas = (cajas + columnas - 1) / columnas
            val separacion = 6.dp
            val lado = minOf(
                (maxWidth - separacion * (columnas - 1)) / columnas,
                (maxHeight - separacion * (filas - 1)) / filas,
                72.dp
            )
            Column(verticalArrangement = Arrangement.spacedBy(separacion)) {
                for (fila in 0 until filas) {
                    Row(horizontalArrangement = Arrangement.spacedBy(separacion)) {
                        for (columna in 0 until columnas) {
                            val caja = fila * columnas + columna
                            if (caja < cajas) {
                                Caja(
                                    numero = estante.numeros[caja],
                                    abierta = caja in abiertas,
                                    esElFolio = encontrado && caja == estante.objetivo,
                                    descartada = !encontrado && caja !in rango && caja !in abiertas,
                                    senalada = senalada == caja,
                                    lado = lado,
                                    onClick = { abrir(caja) }
                                )
                            }
                        }
                    }
                }
            }
        }
        Text(
            "Consejo del Búho: abre siempre la caja del medio de las que quedan.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Caja(
    numero: Int,
    abierta: Boolean,
    esElFolio: Boolean,
    descartada: Boolean,
    senalada: Boolean,
    lado: Dp,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(6.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(lado)
            .clip(forma)
            .background(
                when {
                    esElFolio -> NachoYellow
                    abierta -> ColorCajaAbierta
                    descartada -> ColorCajaDescartada
                    else -> ColorCaja
                }
            )
            .border(
                width = if (senalada) 3.dp else 1.dp,
                color = if (senalada) OjosBuho else Color(0xFF5D4037),
                shape = forma
            )
            .semantics {
                contentDescription = when {
                    abierta -> "Caja $numero"
                    descartada -> "Caja descartada"
                    else -> "Caja cerrada"
                }
            }
            .clickable(enabled = !abierta, onClick = onClick)
            .padding(2.dp)
    ) {
        Text(
            text = when {
                abierta -> numero.toString()
                senalada -> "🦉"
                descartada -> "·"
                else -> "🗄️"
            },
            fontSize = if (abierta) (lado.value * 0.28f).sp else (lado.value * 0.36f).sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3E2723),
            maxLines = 1
        )
    }
}
