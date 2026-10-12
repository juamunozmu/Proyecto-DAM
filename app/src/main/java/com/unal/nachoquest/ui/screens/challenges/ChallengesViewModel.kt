package com.unal.nachoquest.ui.screens.challenges

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

data class HitoHistoria(
    val capitulo: Int,
    val puntoId: String,
    val nombreLugar: String,
    val tituloMision: String,
    val descripcion: String,
    val retoId: String,
    val itemRequeridoId: String?,
    val nombreItemRequerido: String?,
    val itemRecompensaId: String,
    val nombreItemRecompensa: String,
    val emojiRecompensa: String,
    val xpRecompensa: Int
)

data class ChallengesUiState(
    val retosCompletados: Set<String> = emptySet(),
    val itemsDesbloqueados: Set<String> = emptySet()
) {
    fun estaCompletado(hito: HitoHistoria): Boolean = retosCompletados.contains(hito.retoId)

    fun estaDesbloqueado(hito: HitoHistoria): Boolean {
        if (hito.itemRequeridoId == null) return true
        return itemsDesbloqueados.contains(hito.itemRequeridoId)
    }
}

@HiltViewModel
class ChallengesViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengesUiState())
    val uiState: StateFlow<ChallengesUiState> = _uiState.asStateFlow()

    val misionesHistoria = listOf(
        HitoHistoria(
            capitulo = 1,
            puntoId = "poi_plaza_che",
            nombreLugar = "Plaza Central Santander (Plaza Che)",
            tituloMision = "El Despertar del Estudiante",
            descripcion = "Demuestra que conoces el epicentro histórico y político de la Ciudad Blanca.",
            retoId = "reto_plaza_che",
            itemRequeridoId = null,
            nombreItemRequerido = null,
            itemRecompensaId = "carnet_antiguo",
            nombreItemRecompensa = "Carné Histórico UNAL",
            emojiRecompensa = "🪪",
            xpRecompensa = 100
        ),
        HitoHistoria(
            capitulo = 2,
            puntoId = "edif_102",
            nombreLugar = "Biblioteca Central Gabriel García Márquez",
            tituloMision = "Los Secretos del Búho",
            descripcion = "Utiliza tu Carné Histórico para ingresar a la biblioteca y descifrar su legado.",
            retoId = "reto_biblio",
            itemRequeridoId = "carnet_antiguo",
            nombreItemRequerido = "Carné Histórico UNAL",
            itemRecompensaId = "regla_takeuchi",
            nombreItemRecompensa = "Regla de Takeuchi",
            emojiRecompensa = "📐",
            xpRecompensa = 120
        ),
        HitoHistoria(
            capitulo = 3,
            puntoId = "edif_401",
            nombreLugar = "Edificio Julio Garavito Armero (CYT)",
            tituloMision = "El Vuelo Científico de Garavito",
            descripcion = "Aplica los saberes matemáticos para resolver el enigma del gran atrio de ingeniería.",
            retoId = "reto_cyt",
            itemRequeridoId = "regla_takeuchi",
            nombreItemRequerido = "Regla de Takeuchi",
            itemRecompensaId = "partitura_maestra",
            nombreItemRecompensa = "Partitura Maestra de Greiff",
            emojiRecompensa = "🎼",
            xpRecompensa = 150
        ),
        HitoHistoria(
            capitulo = 4,
            puntoId = "edif_104",
            nombreLugar = "Auditorio León de Greiff",
            tituloMision = "La Armonía del Patrimonio",
            descripcion = "Entrega la Partitura Maestra en este Monumento Nacional y supera el examen acústico.",
            retoId = "reto_greiff",
            itemRequeridoId = "partitura_maestra",
            nombreItemRequerido = "Partitura Maestra de Greiff",
            itemRecompensaId = "brujula_unal",
            nombreItemRecompensa = "Brújula de Fritz Karsen",
            emojiRecompensa = "🧭",
            xpRecompensa = 200
        )
    )

    init {
        viewModelScope.launch {
            userRepository.userProfileFlow.collect { profile ->
                _uiState.update {
                    it.copy(
                        retosCompletados = profile.retosCompletados.toSet(),
                        itemsDesbloqueados = profile.itemsDesbloqueados.toSet()
                    )
                }
            }
        }
    }
}
