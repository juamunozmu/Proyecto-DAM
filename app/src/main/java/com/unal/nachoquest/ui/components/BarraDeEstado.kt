package com.unal.nachoquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unal.nachoquest.ui.theme.NachoGreenDark

/**
 * Franja verde oscura detrás de la barra de estado. Va encima de todas las pantallas, después del
 * contenido. Antes la pintaba el tema (`android:statusBarColor`), pero desde Android 15 la app se
 * dibuja detrás de las barras del sistema y ese color se ignora. Sobre ella van los iconos claros
 * que pide `enableEdgeToEdge(SystemBarStyle.dark(...))`.
 */
@Composable
fun BarraDeEstado() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(NachoGreenDark)
    )
}
