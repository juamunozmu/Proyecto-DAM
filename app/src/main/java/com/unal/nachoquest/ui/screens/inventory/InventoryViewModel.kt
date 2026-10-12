package com.unal.nachoquest.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.CatalogoItems
import com.unal.nachoquest.domain.model.CategoriaItem
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val selectedTab: Int = 0,
    val itemsDesbloqueadosIds: Set<String> = emptySet(),
    val insigniasIds: Set<String> = emptySet(),
    val itemSeleccionadoDetalle: ItemInventario? = null,
    val totalObjetos: Int = CatalogoItems.itemsObjetos.size,
    val totalInsignias: Int = CatalogoItems.itemsInsignias.size,
    val totalColeccionables: Int = CatalogoItems.itemsColeccionables.size
) {
    val objetosDesbloqueadosCount: Int
        get() = CatalogoItems.itemsObjetos.count { itemsDesbloqueadosIds.contains(it.id) }

    val insigniasDesbloqueadasCount: Int
        get() = CatalogoItems.itemsInsignias.count { insigniasIds.contains(it.id) }

    val coleccionablesDesbloqueadosCount: Int
        get() = CatalogoItems.itemsColeccionables.count { itemsDesbloqueadosIds.contains(it.id) }
}

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        observarPerfil()
    }

    private fun observarPerfil() {
        viewModelScope.launch {
            userRepository.userProfileFlow.collect { profile ->
                _uiState.update {
                    it.copy(
                        itemsDesbloqueadosIds = profile.itemsDesbloqueados.toSet(),
                        insigniasIds = profile.insignias.toSet()
                    )
                }
            }
        }
        viewModelScope.launch {
            userRepository.getUserProfile()
        }
    }

    fun seleccionarTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun mostrarDetalleItem(item: ItemInventario?) {
        _uiState.update { it.copy(itemSeleccionadoDetalle = item) }
    }

    fun estaDesbloqueado(item: ItemInventario): Boolean {
        val state = _uiState.value
        return when (item.categoria) {
            CategoriaItem.OBJETO -> state.itemsDesbloqueadosIds.contains(item.id)
            CategoriaItem.INSIGNIA -> state.insigniasIds.contains(item.id)
            CategoriaItem.COLECCIONABLE -> state.itemsDesbloqueadosIds.contains(item.id)
        }
    }
}
