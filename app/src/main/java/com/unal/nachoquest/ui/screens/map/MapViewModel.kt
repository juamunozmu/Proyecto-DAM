package com.unal.nachoquest.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.PointOfInterest
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val puntosDeInteres: List<PointOfInterest> = emptyList(),
    val puntoSeleccionado: PointOfInterest? = null,
    val nivelUsuario: Int = 1,
    val xpUsuario: Int = 0,
    val estrellasUsuario: Int = 0
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _mapState = MutableStateFlow(
        MapUiState(puntosDeInteres = puntosIniciales())
    )
    val mapState: StateFlow<MapUiState> = _mapState.asStateFlow()

    init {
        cargarDatosUsuario()
    }

    fun cargarDatosUsuario() {
        viewModelScope.launch {
            val perfil = userRepository.getUserProfile()
            if (perfil != null) {
                _mapState.update { 
                    it.copy(
                        nivelUsuario = perfil.nivel,
                        xpUsuario = perfil.xp,
                        estrellasUsuario = perfil.estrellas
                    )
                }
            }
        }
    }

    fun seleccionarPunto(punto: PointOfInterest) {
        _mapState.update { it.copy(puntoSeleccionado = punto) }
    }

    fun cerrarDetalle() {
        _mapState.update { it.copy(puntoSeleccionado = null) }
    }

    private fun puntosIniciales(): List<PointOfInterest> = listOf(
        PointOfInterest(
            id = "biblioteca-central",
            nombre = "Biblioteca Central",
            edificio = "Edificio 102",
            descripcion = "La Biblioteca Central resguarda parte fundamental del conocimiento que ha construido la comunidad universitaria.",
            latitud = 4.6360,
            longitud = -74.0830,
            tieneRetoDisponible = true
        ),
        PointOfInterest(
            id = "plaza-che",
            nombre = "Plaza Che Guevara",
            edificio = "Espacio abierto",
            descripcion = "La Plaza Che es el corazón del campus y punto de encuentro por excelencia de la comunidad estudiantil.",
            latitud = 4.63545150,
            longitud = -74.08274173,
            tieneRetoDisponible = true
        )
    )
}