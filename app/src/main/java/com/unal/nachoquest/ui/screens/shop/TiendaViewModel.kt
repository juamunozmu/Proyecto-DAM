package com.unal.nachoquest.ui.screens.shop

import androidx.lifecycle.ViewModel
import com.unal.nachoquest.domain.model.AspectoNacho
import com.unal.nachoquest.domain.model.TipoAyuda
import com.unal.nachoquest.domain.model.UserProfile
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TiendaViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val perfil: StateFlow<UserProfile> = userRepository.userProfileFlow

    // Sube cada vez que una compra falla por falta de monedas, para avisar en la pantalla
    private val _sinSaldo = MutableStateFlow(0)
    val sinSaldo: StateFlow<Int> = _sinSaldo.asStateFlow()

    fun comprarAyuda(tipo: TipoAyuda) {
        if (!userRepository.comprarAyuda(tipo)) _sinSaldo.update { it + 1 }
    }

    /** Compra el aspecto si no lo tiene; si ya es suyo, se lo pone o se lo quita. */
    fun tocarAspecto(aspecto: AspectoNacho) {
        if (aspecto.id in perfil.value.aspectos) {
            userRepository.alternarAspecto(aspecto.id)
        } else if (!userRepository.comprarAspecto(aspecto.id)) {
            _sinSaldo.update { it + 1 }
        }
    }
}
