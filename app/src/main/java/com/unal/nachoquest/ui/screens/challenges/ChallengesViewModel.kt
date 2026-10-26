package com.unal.nachoquest.ui.screens.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.Capitulo
import com.unal.nachoquest.domain.model.CatalogoItems
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class EstadoCapitulo { COMPLETADO, ACTUAL, BLOQUEADO }

data class CapituloUi(
    val capitulo: Capitulo,
    val estado: EstadoCapitulo,
    val estrellas: Int,
    val itemRequerido: ItemInventario?,
    val itemRecompensa: ItemInventario?,
    val escenasVistas: List<String>
)

data class ChallengesUiState(
    val capitulos: List<CapituloUi> = emptyList(),
    val historiaCompletada: Boolean = false
) {
    val completados: Int get() = capitulos.count { it.estado == EstadoCapitulo.COMPLETADO }
}

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    userRepository: UserRepository
) : ViewModel() {

    val uiState: StateFlow<ChallengesUiState> = userRepository.userProfileFlow.map { perfil ->
        val actual = Historia.capituloActual(perfil.retosCompletados)
        ChallengesUiState(
            capitulos = Historia.capitulos.map { cap ->
                val tieneItem = cap.itemRequeridoId == null || cap.itemRequeridoId in perfil.itemsDesbloqueados
                CapituloUi(
                    capitulo = cap,
                    estado = when {
                        cap.retoId in perfil.retosCompletados -> EstadoCapitulo.COMPLETADO
                        cap == actual && tieneItem -> EstadoCapitulo.ACTUAL
                        else -> EstadoCapitulo.BLOQUEADO
                    },
                    estrellas = perfil.estrellasPorReto[cap.retoId] ?: 0,
                    itemRequerido = cap.itemRequeridoId?.let { CatalogoItems.obtenerPorId(it) },
                    itemRecompensa = CatalogoItems.obtenerPorId(cap.itemRecompensaId),
                    escenasVistas = perfil.escenasVistas
                )
            },
            historiaCompletada = actual == null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChallengesUiState())
}
