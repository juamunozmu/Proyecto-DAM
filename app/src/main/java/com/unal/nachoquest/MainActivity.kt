package com.unal.nachoquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.unal.nachoquest.domain.repository.UserRepository
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
        setContent {
            // Nacho lleva en toda la app los aspectos que el jugador equipó en la tienda
            val perfil by userRepository.userProfileFlow.collectAsState()
            CompositionLocalProvider(LocalAspectosNacho provides perfil.aspectosEquipados) {
                NachoQuestTheme {
                    NachoQuestNavHost()
                }
            }
        }
    }
}
