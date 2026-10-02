package com.unal.nachoquest.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val nombre: String = "Cargando...",
    val email: String = "",
    val nivel: Int = 1,
    val xp: Int = 0,
    val xpParaSiguienteNivel: Int = 100,
    val retosCompletados: Int = 0,
    val lugaresVisitados: Int = 0,
    val insigniasObtenidas: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileUiState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    fun cargarPerfil() {
        viewModelScope.launch {
            val perfil = userRepository.getUserProfile()
            if (perfil != null) {
                _profileState.update { 
                    it.copy(
                        nombre = perfil.nombre,
                        email = perfil.email,
                        nivel = perfil.nivel,
                        xp = perfil.xp,
                        xpParaSiguienteNivel = perfil.nivel * 100,
                        retosCompletados = perfil.retosCompletados.size
                    )
                }
            }
        }
    }
}