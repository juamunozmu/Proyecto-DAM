package com.unal.nachoquest.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.LineaDialogo
import com.unal.nachoquest.domain.model.TriviaDiaria
import com.unal.nachoquest.domain.model.primerNombre
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // Escena que el jugador pidió volver a ver desde el perfil o los capítulos
    private val escenaRepetida = MutableStateFlow<String?>(null)

    private val escena: Flow<List<LineaDialogo>?> = combine(
        userRepository.userProfileFlow,
        userRepository.perfilCargado,
        escenaRepetida
    ) { perfil, cargado, repetida ->
        val id = repetida ?: Historia.ESCENA_PROLOGO.takeIf { cargado && it !in perfil.escenasVistas }
        id?.let { Historia.escena(it, perfil.primerNombre()) }
    }

    /** Escena a mostrar a pantalla completa, o null si no hay ninguna. */
    val escenaActiva: StateFlow<List<LineaDialogo>?> =
        escena.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** true cuando el perfil ya cargó y no hay una escena encima del juego (p. ej. para pedir permisos). */
    val listoParaJugar: StateFlow<Boolean> =
        combine(escena, userRepository.perfilCargado) { escenaActual, cargado -> cargado && escenaActual == null }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** true si la trivia de hoy sigue sin jugarse: la pestaña Retos lo avisa con un punto. */
    val triviaPendiente: StateFlow<Boolean> =
        combine(userRepository.userProfileFlow, userRepository.perfilCargado) { perfil, cargado ->
            cargado && !TriviaDiaria.yaJugoHoy(perfil, TriviaDiaria.diaActual())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun verEscena(escenaId: String) {
        escenaRepetida.value = escenaId
    }

    fun terminarEscena() {
        if (escenaRepetida.value != null) {
            escenaRepetida.value = null
        } else {
            userRepository.marcarEscenaVista(Historia.ESCENA_PROLOGO)
        }
    }
}
