package com.unal.nachoquest.ui.screens.quiz.minijuegos

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.components.sacudir
import com.unal.nachoquest.ui.theme.NachoGreenDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private const val LARGO_OBJETIVO = 6
private const val FRECUENCIA_MUESTREO = 44100
private const val DURACION_NOTA_MS = 380

private data class Nota(val nombre: String, val frecuencia: Double, val color: Color)

private val NOTAS = listOf(
    Nota("Do", 523.25, Color(0xFF43A047)),
    Nota("Mi", 659.25, Color(0xFFFDD835)),
    Nota("Sol", 783.99, Color(0xFF1E88E5)),
    Nota("Do'", 1046.50, Color(0xFFE53935))
)

/**
 * Sintetizador mínimo: una pista estática por nota con una onda senoidal suavizada.
 */
private class Sintetizador {
    private val pistas: List<AudioTrack> = NOTAS.map { crearPista(it.frecuencia) }

    private fun crearPista(frecuencia: Double): AudioTrack {
        val muestras = FRECUENCIA_MUESTREO * DURACION_NOTA_MS / 1000
        val datos = ShortArray(muestras) { i ->
            // Envolvente: ataque corto y caída suave, para que suene como una campana y no como un pitido
            val t = i.toDouble() / FRECUENCIA_MUESTREO
            val ataque = (i / (FRECUENCIA_MUESTREO * 0.01)).coerceAtMost(1.0)
            val caida = 1.0 - i.toDouble() / muestras
            val onda = sin(2 * PI * frecuencia * t) + 0.3 * sin(4 * PI * frecuencia * t)
            (onda * ataque * caida * caida * 0.45 * Short.MAX_VALUE).toInt().toShort()
        }
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(FRECUENCIA_MUESTREO)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(datos.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
            .apply { write(datos, 0, datos.size) }
    }

    fun tocar(indice: Int) {
        val pista = pistas[indice]
        try {
            pista.stop()
            pista.reloadStaticData()
            pista.play()
        } catch (_: IllegalStateException) {
            // Si el sistema liberó la pista, simplemente no suena
        }
    }

    fun liberar() = pistas.forEach { it.release() }
}

/**
 * Simon musical: Nacho toca una melodía y el jugador la repite. Cada ronda suma una nota.
 */
@Composable
fun MinijuegoSinfonia(alTerminar: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val sintetizador = remember { Sintetizador() }
    DisposableEffect(Unit) { onDispose { sintetizador.liberar() } }

    val melodia = remember { List(LARGO_OBJETIVO) { NOTAS.indices.random() } }
    var ronda by remember { mutableIntStateOf(1) }          // cuántas notas hay que repetir
    var progreso by remember { mutableIntStateOf(0) }       // cuántas lleva bien en esta ronda
    var reproduciendo by remember { mutableStateOf(true) }
    var iluminada by remember { mutableStateOf<Int?>(null) }
    var errores by remember { mutableIntStateOf(0) }
    var terminado by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("Escucha...") }
    var repetirRonda by remember { mutableIntStateOf(0) }

    // Reproduce la melodía de la ronda actual
    LaunchedEffect(ronda, repetirRonda) {
        if (terminado) return@LaunchedEffect
        reproduciendo = true
        mensaje = "Escucha..."
        delay(700)
        for (i in 0 until ronda) {
            iluminada = melodia[i]
            sintetizador.tocar(melodia[i])
            delay(420)
            iluminada = null
            delay(160)
        }
        progreso = 0
        reproduciendo = false
        mensaje = "¡Tu turno!"
    }

    fun tocarNota(indice: Int) {
        if (reproduciendo || terminado) return
        sintetizador.tocar(indice)
        scope.launch {
            iluminada = indice
            delay(200)
            if (iluminada == indice) iluminada = null
        }
        if (indice == melodia[progreso]) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            progreso++
            if (progreso == ronda) {
                if (ronda == LARGO_OBJETIVO) {
                    terminado = true
                    mensaje = "¡El auditorio vuelve a sonar!"
                    scope.launch {
                        delay(900)
                        alTerminar(
                            when {
                                errores == 0 -> 3
                                errores <= 2 -> 2
                                else -> 1
                            }
                        )
                    }
                } else {
                    mensaje = "¡Bien! Una nota más..."
                    reproduciendo = true
                    scope.launch {
                        delay(500)
                        ronda++
                    }
                }
            }
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            errores++
            mensaje = "El Olvido desafinó la melodía... ¡otra vez!"
            reproduciendo = true
            repetirRonda++
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Marcador("Notas: $ronda/$LARGO_OBJETIVO")
            Marcador("Errores: $errores", color = if (errores > 0) Color(0xFFC62828) else NachoGreenDark)
        }
        Spacer(modifier = Modifier.height(14.dp))

        Text(
            mensaje,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = if (reproduciendo) Color(0xFF6D4C41) else NachoGreenDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Progreso de la ronda
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(ronda) { i ->
                Text(if (i < progreso) "♪" else "·", fontSize = 22.sp, color = NachoGreenDark)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .sacudir(errores)
        ) {
            for (fila in 0..1) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (col in 0..1) {
                        val i = fila * 2 + col
                        val nota = NOTAS[i]
                        val encendida = iluminada == i
                        val brillo by animateFloatAsState(if (encendida) 1f else 0f, tween(90), label = "brillo")
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.15f)
                                .graphicsLayer {
                                    scaleX = 1f + brillo * 0.05f
                                    scaleY = 1f + brillo * 0.05f
                                }
                                .clip(RoundedCornerShape(22.dp))
                                .background(nota.color.copy(alpha = 0.45f + brillo * 0.55f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { tocarNota(i) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎵", fontSize = 28.sp, modifier = Modifier.graphicsLayer { alpha = 0.5f + brillo * 0.5f })
                                Text(nota.nombre, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text("Sube el volumen 🔊", color = Color.Gray, fontSize = 12.sp)
    }
}
