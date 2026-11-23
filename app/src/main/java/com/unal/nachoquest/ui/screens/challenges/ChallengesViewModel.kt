package com.unal.nachoquest.ui.screens.challenges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.Capitulo
import com.unal.nachoquest.domain.model.CatalogoItems
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.domain.model.JugadorRanking
import com.unal.nachoquest.domain.model.RetoLugar
import com.unal.nachoquest.domain.model.RetosLugares
import com.unal.nachoquest.domain.model.TriviaDiaria
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

data class TriviaUi(
    val jugadaHoy: Boolean = false,
    val rachaDias: Int = 0,
    val xpDisponible: Int = TriviaDiaria.XP_BASE
)

data class RankingUiState(
    val visible: Boolean = false,
    val cargando: Boolean = false,
    val jugadores: List<JugadorRanking> = emptyList(),
    val error: Boolean = false
)

data class RetoLugarUi(
    val reto: RetoLugar,
    val completado: Boolean,
    val estrellas: Int
)

data class ChallengesUiState(
    val capitulos: List<CapituloUi> = emptyList(),
    val historiaCompletada: Boolean = false,
    val trivia: TriviaUi = TriviaUi(),
    val retosLugar: List<RetoLugarUi> = emptyList()
) {
    val completados: Int get() = capitulos.count { it.estado == EstadoCapitulo.COMPLETADO }
    val retosLugarCompletados: Int get() = retosLugar.count { it.completado }
}

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _ranking = MutableStateFlow(RankingUiState())
    val ranking: StateFlow<RankingUiState> = _ranking.asStateFlow()

    fun abrirRanking() {
        _ranking.value = RankingUiState(visible = true, cargando = true)
        viewModelScope.launch {
            _ranking.value = try {
                RankingUiState(visible = true, jugadores = userRepository.obtenerRanking())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RankingUiState(visible = true, error = true)
            }
        }
    }

    fun cerrarRanking() {
        _ranking.value = RankingUiState()
    }

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
            historiaCompletada = actual == null,
            // Primero los pendientes, para que se vea qué falta por explorar
            retosLugar = RetosLugares.retos
                .map { reto ->
                    RetoLugarUi(
                        reto = reto,
                        completado = reto.retoId in perfil.retosCompletados,
                        estrellas = perfil.estrellasPorReto[reto.retoId] ?: 0
                    )
                }
                .sortedBy { it.completado },
            trivia = TriviaDiaria.diaActual().let { hoy ->
                TriviaUi(
                    jugadaHoy = TriviaDiaria.yaJugoHoy(perfil, hoy),
                    rachaDias = TriviaDiaria.rachaVigente(perfil, hoy),
                    xpDisponible = TriviaDiaria.XP_BASE +
                        TriviaDiaria.bonoRacha(TriviaDiaria.rachaTrasJugar(perfil, hoy))
                )
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChallengesUiState())
}
