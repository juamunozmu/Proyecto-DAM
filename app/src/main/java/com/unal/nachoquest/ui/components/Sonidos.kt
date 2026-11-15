package com.unal.nachoquest.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

private const val FRECUENCIA_MUESTREO = 22_050
private const val PREFERENCIAS = "ajustes"
private const val CLAVE_SONIDO = "sonido_activado"

/** Efectos de sonido del juego: cada uno es una secuencia corta de notas (frecuencia en Hz, duración en ms). */
enum class Efecto(internal val notas: List<Pair<Double, Int>>, internal val volumen: Double = 0.35) {
    TOQUE(listOf(880.00 to 45), volumen = 0.16),
    ACIERTO(listOf(659.25 to 90, 987.77 to 160)),
    ERROR(listOf(233.08 to 140, 174.61 to 240)),
    VICTORIA(listOf(523.25 to 110, 659.25 to 110, 783.99 to 110, 1046.50 to 320)),
    DERROTA(listOf(392.00 to 180, 311.13 to 180, 261.63 to 380)),
    LOGRO(listOf(783.99 to 90, 1046.50 to 90, 1318.51 to 260)),
    GOL(listOf(523.25 to 80, 783.99 to 80, 1046.50 to 80, 1567.98 to 300)),
    ATAJADA(listOf(196.00 to 120, 146.83 to 260))
}

/**
 * Efectos de sonido sintetizados (no necesitan archivos de audio). Se pueden apagar desde el
 * perfil; la preferencia se guarda en el teléfono.
 */
object Sonidos {

    private var preferencias: android.content.SharedPreferences? = null
    private val pistas = mutableMapOf<Efecto, AudioTrack>()

    var activado: Boolean = true
        private set

    fun iniciar(context: Context) {
        preferencias = context.applicationContext.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
        activado = preferencias?.getBoolean(CLAVE_SONIDO, true) ?: true
    }

    fun cambiarActivado(valor: Boolean) {
        activado = valor
        preferencias?.edit()?.putBoolean(CLAVE_SONIDO, valor)?.apply()
    }

    fun reproducir(efecto: Efecto) {
        if (!activado) return
        try {
            val pista = pistas.getOrPut(efecto) { crearPista(efecto) }
            pista.stop()
            pista.reloadStaticData()
            pista.play()
        } catch (_: RuntimeException) {
            // Si el sistema no entrega una pista de audio, el juego sigue en silencio
            pistas.remove(efecto)?.release()
        }
    }

    private fun crearPista(efecto: Efecto): AudioTrack {
        val datos = efecto.notas.flatMap { (frecuencia, duracionMs) ->
            val muestras = FRECUENCIA_MUESTREO * duracionMs / 1000
            List(muestras) { i ->
                // Ataque corto y caída suave para que suene a campanita y no a pitido
                val t = i.toDouble() / FRECUENCIA_MUESTREO
                val ataque = (i / (FRECUENCIA_MUESTREO * 0.008)).coerceAtMost(1.0)
                val caida = 1.0 - i.toDouble() / muestras
                val onda = sin(2 * PI * frecuencia * t) + 0.3 * sin(4 * PI * frecuencia * t)
                (onda * ataque * caida * efecto.volumen * Short.MAX_VALUE).toInt().toShort()
            }
        }.toShortArray()

        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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
}
