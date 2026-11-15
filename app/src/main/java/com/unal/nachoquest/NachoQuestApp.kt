package com.unal.nachoquest

import android.app.Application
import com.unal.nachoquest.ui.components.Sonidos
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase principal de la aplicación NachoQuest.
 * Utiliza Hilt para la inyección de dependencias.
 */
@HiltAndroidApp
class NachoQuestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Sonidos.iniciar(this)
    }
}
