package com.unal.nachoquest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import com.unal.nachoquest.ui.navigation.NachoQuestNavHost
import com.unal.nachoquest.ui.theme.NachoQuestTheme

/**
 * Activity principal de NachoQuest.
 * Punto de entrada de la aplicación Android.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NachoQuestTheme {
                NachoQuestNavHost()
            }
        }
    }
}
