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
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject

data class MapUiState(
    val puntosDeInteres: List<PointOfInterest> = emptyList(),
    val puntoSeleccionado: PointOfInterest? = null,
    val nivelUsuario: Int = 1,
    val xpUsuario: Int = 0,
    val estrellasUsuario: Int = 0,
    val searchQuery: String = "",
    val resultadosBusqueda: List<PointOfInterest> = emptyList()
)

@HiltViewModel
class MapViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

        fun actualizarBusqueda(query: String) {
        _mapState.update { state ->
            val puntos = if (query.isBlank()) {
                emptyList()
            } else {
                state.puntosDeInteres.filter { punto ->
                    val q = query.lowercase()
                    punto.nombre.lowercase().contains(q) || 
                    punto.edificio.lowercase().contains(q) ||
                    punto.alias.any { it.lowercase().contains(q) }
                }
            }
            state.copy(searchQuery = query, resultadosBusqueda = puntos)
        }
    }

    fun seleccionarPunto(punto: PointOfInterest) {
        _mapState.update { it.copy(puntoSeleccionado = punto) }
    }

    fun cerrarDetalle() {
        _mapState.update { it.copy(puntoSeleccionado = null) }
    }

        private fun puntosIniciales(): List<PointOfInterest> {
        val puntos = mutableListOf<PointOfInterest>()
        try {
            val inputStream = context.assets.open("edificios_unal.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            
            val jsonString = String(buffer, Charsets.UTF_8)
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("puntos")
            
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                
                val aliasArray = item.getJSONArray("alias")
                val aliasList = mutableListOf<String>()
                for (j in 0 until aliasArray.length()) {
                    aliasList.add(aliasArray.getString(j))
                }
                
                puntos.add(
                    PointOfInterest(
                        id = item.getString("id"),
                        nombre = item.getString("nombre"),
                        edificio = item.getString("edificio"),
                        descripcion = item.getString("descripcion"),
                        latitud = item.getDouble("latitud"),
                        longitud = item.getDouble("longitud"),
                        tieneRetoDisponible = if (item.has("tieneRetoDisponible")) item.getBoolean("tieneRetoDisponible") else false,
                        alias = aliasList,
                        imagenUrl = if (item.has("imagenUrl") && !item.isNull("imagenUrl")) item.getString("imagenUrl") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Si falla la lectura, dejar algunos por defecto
            puntos.add(
                PointOfInterest(
                    id = "cyt",
                    nombre = "Ciencia y Tecnología",
                    edificio = "401",
                    descripcion = "Error al cargar la base de datos completa.",
                    latitud = 4.6366,
                    longitud = -74.0818
                )
            )
        }
        
        // Forzar que la biblioteca y la plaza che tengan retos (para la Entrega 2)
        val finalPuntos = puntos.map { 
            if (it.nombre.contains("Biblioteca Central") || it.nombre.contains("Che")) {
                it.copy(tieneRetoDisponible = true)
            } else {
                it
            }
        }
        
        return finalPuntos
    }
}