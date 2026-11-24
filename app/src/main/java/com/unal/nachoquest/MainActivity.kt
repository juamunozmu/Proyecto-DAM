package com.unal.nachoquest

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.unal.nachoquest.domain.repository.UserRepository
import com.unal.nachoquest.ui.components.BarraDeEstado
import com.unal.nachoquest.ui.components.LocalAspectosNacho
import com.unal.nachoquest.ui.navigation.NachoQuestNavHost
import com.unal.nachoquest.ui.theme.NachoQuestTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Activity principal de NachoQuest.
 * Punto de entrada de la aplicación Android.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Desde Android 15 (targetSdk 35) la app siempre se dibuja detrás de las barras del sistema.
        // Se activa también en las versiones anteriores para que todas se vean igual. Los iconos de
        // la barra de estado van claros, sobre la franja verde de BarraDeEstado.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            // Nacho lleva en toda la app los aspectos que el jugador equipó en la tienda
            val perfil by userRepository.userProfileFlow.collectAsState()
            CompositionLocalProvider(LocalAspectosNacho provides perfil.aspectosEquipados) {
                NachoQuestTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NachoQuestNavHost()
                        BarraDeEstado()
                    }
                }
            }
        }
    }
}

