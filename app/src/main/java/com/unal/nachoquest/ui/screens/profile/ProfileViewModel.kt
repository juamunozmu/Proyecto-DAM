package com.unal.nachoquest.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.Logro
import com.unal.nachoquest.domain.model.Logros
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val email: String = "",
    val nivel: Int = 1,
    val xpEnNivel: Int = 0,
    val xpPorNivel: Int = UserRepository.XP_POR_NIVEL,
    val retosCompletados: Int = 0,
    val lugaresDescubiertos: Int = 0,
    val insigniasObtenidas: Int = 0,
    val estrellas: Int = 0,
    val capitulosCompletados: Int = 0,
    val totalCapitulos: Int = Historia.capitulos.size,
    val logros: List<Logro> = emptyList(),
    val historiaCompletada: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val profileState: StateFlow<ProfileUiState> = combine(
        userRepository.userProfileFlow,
        userRepository.perfilCargado
    ) { perfil, cargado ->
        val capitulos = Historia.capitulos.count { it.retoId in perfil.retosCompletados }
        ProfileUiState(
            cargando = !cargado,
            nombre = perfil.nombre,
            email = perfil.email,
            nivel = perfil.nivel,
            xpEnNivel = perfil.xp % UserRepository.XP_POR_NIVEL,
            retosCompletados = perfil.retosCompletados.size,
            lugaresDescubiertos = perfil.lugaresDescubiertos.size,
            insigniasObtenidas = perfil.insignias.size,
            estrellas = perfil.estrellas,
            capitulosCompletados = capitulos,
            logros = Logros.calcular(perfil),
            historiaCompletada = capitulos == Historia.capitulos.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    init {
        viewModelScope.launch { userRepository.getUserProfile() }
    }

    fun cerrarSesion() {
        userRepository.limpiarSesion()
    }
}
