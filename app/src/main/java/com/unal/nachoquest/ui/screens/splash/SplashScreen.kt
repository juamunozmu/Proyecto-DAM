package com.unal.nachoquest.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoQuestTheme
import com.unal.nachoquest.ui.theme.NachoYellow

/**
 * Pantalla de bienvenida de NachoQuest.
 * Muestra el logo, slogan y botones de acceso.
 */
@Composable
fun SplashScreen(
    onComenzar: () -> Unit,
    onIniciarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NachoGreen)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Título
        Text(
            text = "NachoQuest™",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Slogan
        Text(
            text = "Explora. Descubre.\nVive la UNAL.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Personaje placeholder (Nacho)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NachoGreenDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "UN",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón Comenzar
        Button(
            onClick = onComenzar,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NachoYellow,
                contentColor = NachoGreenDark
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Comenzar",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón Iniciar sesión
        OutlinedButton(
            onClick = onIniciarSesion,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Iniciar sesión",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    NachoQuestTheme {
        SplashScreen(onComenzar = {}, onIniciarSesion = {})
    }
}
