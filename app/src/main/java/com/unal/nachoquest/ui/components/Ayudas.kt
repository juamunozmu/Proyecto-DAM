package com.unal.nachoquest.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.domain.model.TipoAyuda
import com.unal.nachoquest.ui.theme.EstiloEtiqueta
import com.unal.nachoquest.ui.theme.NachoYellow

/** Color de las monedas en toda la app. */
val ColorMoneda = Color(0xFFB8860B)

/** Saldo de monedas del jugador, en forma de pastilla. */
@Composable
fun SaldoMonedas(monedas: Int, modifier: Modifier = Modifier, claro: Boolean = false) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (claro) Color.White.copy(alpha = 0.18f) else NachoYellow.copy(alpha = 0.25f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🪙", fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            "$monedas",
            fontWeight = FontWeight.ExtraBold,
            color = if (claro) Color.White else ColorMoneda,
            fontSize = 14.sp
        )
    }
}

/**
 * Botón de una ayuda en medio de un reto. Debajo del nombre dice cómo se pagaría: gratis, con
 * una de la reserva ("x2") o con monedas ("🪙 20"); si no se puede usar muestra [textoInactivo].
 */
@Composable
fun BotonAyuda(
    tipo: TipoAyuda,
    etiqueta: String,
    reserva: Int,
    habilitado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gratis: Boolean = false,
    textoInactivo: String = "USADO"
) {
    val costo = when {
        !habilitado -> textoInactivo
        gratis -> "GRATIS"
        reserva > 0 -> "TIENES $reserva"
        else -> "🪙 ${tipo.precio}"
    }
    val borde = if (habilitado) NachoYellow else Color.LightGray
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (habilitado) NachoYellow.copy(alpha = 0.18f) else Color.LightGray.copy(alpha = 0.25f))
            .border(1.5.dp, borde, RoundedCornerShape(12.dp))
            .clickable(enabled = habilitado) {
                Sonidos.reproducir(Efecto.TOQUE)
                onClick()
            }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "${tipo.emoji} $etiqueta",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (habilitado) Color(0xFF5D4300) else Color.Gray,
            maxLines = 1
        )
        Text(
            costo,
            style = EstiloEtiqueta,
            fontSize = 11.sp,
            color = if (habilitado) ColorMoneda else Color.Gray,
            maxLines = 1
        )
    }
}

/** Avisa con un mensaje corto cada vez que [contador] sube (intento de pagar sin monedas). */
@Composable
fun AvisoSinMonedas(contador: Int) {
    val context = LocalContext.current
    LaunchedEffect(contador) {
        if (contador > 0) {
            Sonidos.reproducir(Efecto.ERROR)
            Toast.makeText(context, "No te alcanzan las monedas. Gánalas superando retos.", Toast.LENGTH_SHORT).show()
        }
    }
}
