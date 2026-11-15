package com.unal.nachoquest.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.theme.CantoCrema
import com.unal.nachoquest.ui.theme.EstiloEtiqueta
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.Papel

/*
 * Piezas de interfaz con aspecto de juego: botones y tarjetas con un "canto" inferior que les da
 * relieve, como fichas. El botón se hunde sobre su canto al presionarlo.
 */

private val CANTO_BOTON = 4.dp
private val RADIO_BOTON = 14.dp
private val GrisDeshabilitado = Color(0xFFBFBDB4)

/** Mezcla el color con negro; se usa para el canto de botones y tarjetas. */
fun Color.oscurecer(factor: Float = 0.7f): Color =
    Color(red = red * factor, green = green * factor, blue = blue * factor, alpha = alpha)

/**
 * Tarjeta con borde y canto inferior. Va antes de los `padding` del contenido.
 *
 * @param pincel degradado para el fondo; si se pasa, reemplaza a [fondo]
 */
fun Modifier.tarjetaJuego(
    fondo: Color = Papel,
    borde: Color = CantoCrema,
    radio: Dp = 18.dp,
    canto: Dp = 4.dp,
    pincel: Brush? = null
): Modifier = this
    .padding(bottom = canto)
    .drawBehind {
        drawRoundRect(
            color = borde,
            topLeft = Offset(0f, canto.toPx()),
            size = size,
            cornerRadius = CornerRadius(radio.toPx())
        )
    }
    .clip(RoundedCornerShape(radio))
    .then(if (pincel != null) Modifier.background(pincel) else Modifier.background(fondo))
    .border(1.5.dp, borde, RoundedCornerShape(radio))

/**
 * Botón principal del juego.
 *
 * @param borde color del borde para botones claros (p. ej. blancos) que necesitan contorno
 * @param prefijo texto corto antes de la etiqueta, con su propio color (la «G» de Google)
 */
@Composable
fun BotonJuego(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = NachoGreen,
    colorTexto: Color = Color.White,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    icono: ImageVector? = null,
    alto: Dp = 50.dp,
    borde: Color? = null,
    tamanoTexto: TextUnit = 16.sp,
    prefijo: String? = null,
    colorPrefijo: Color = colorTexto
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val activo = habilitado && !cargando
    val hundimiento by animateFloatAsState(if (presionado && activo) 1f else 0f, tween(70), label = "hundimiento")

    val fondo = if (habilitado) color else GrisDeshabilitado
    val colorCanto = borde ?: fondo.oscurecer()
    val forma = RoundedCornerShape(RADIO_BOTON)

    Row(
        modifier = modifier
            .padding(bottom = CANTO_BOTON)
            .height(alto)
            .graphicsLayer { translationY = CANTO_BOTON.toPx() * hundimiento }
            .drawBehind {
                // El canto se queda quieto mientras la cara del botón baja sobre él
                drawRoundRect(
                    color = colorCanto,
                    topLeft = Offset(0f, CANTO_BOTON.toPx() * (1f - hundimiento)),
                    size = size,
                    cornerRadius = CornerRadius(RADIO_BOTON.toPx())
                )
            }
            .clip(forma)
            .background(fondo)
            .then(if (borde != null) Modifier.border(1.5.dp, borde, forma) else Modifier)
            .clickable(
                interactionSource = interaccion,
                indication = null,
                enabled = activo,
                role = Role.Button
            ) {
                Sonidos.reproducir(Efecto.TOQUE)
                onClick()
            }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (cargando) {
            CircularProgressIndicator(color = colorTexto, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            if (icono != null) {
                Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            if (prefijo != null) {
                Text(prefijo, color = colorPrefijo, fontWeight = FontWeight.Black, fontSize = tamanoTexto * 1.15f)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = texto,
                color = if (habilitado) colorTexto else Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = tamanoTexto,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Etiqueta pequeña en mayúsculas con la fuente del juego («CAPÍTULO 1», «TRIVIA DEL DÍA»). */
@Composable
fun Etiqueta(texto: String, color: Color, modifier: Modifier = Modifier) {
    Text(text = texto, style = EstiloEtiqueta, color = color, modifier = modifier, maxLines = 1)
}
