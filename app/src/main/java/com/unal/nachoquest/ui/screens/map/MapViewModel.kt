package com.unal.nachoquest.ui.screens.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.Capitulo
import com.unal.nachoquest.domain.model.CatalogoItems
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.domain.model.PointOfInterest
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

enum class EstadoEdificioReto {
    BLOQUEADO_HISTORIA, // Falta ítem requerido
    FUERA_DE_RANGO,     // Desbloqueado pero a > 75m
    LISTO_PARA_JUGAR,   // En rango (< 75m) o simulación dev activa
    YA_COMPLETADO,      // Reto superado
    SIN_RETO            // Edificio informativo del campus
}

data class MapUiState(
    val puntosDeInteres: List<PointOfInterest> = emptyList(),
    val puntoSeleccionado: PointOfInterest? = null,
    val nivelUsuario: Int = 1,
    val xpUsuario: Int = 0,
    val estrellasUsuario: Int = 0,
    val itemsUsuario: Set<String> = emptySet(),
    val retosCompletados: Set<String> = emptySet(),
    val ubicacionUsuario: Pair<Double, Double>? = null,
    val modoSimulacionGps: Boolean = false,
    val searchQuery: String = "",
    val resultadosBusqueda: List<PointOfInterest> = emptyList()
) {
    /** Capítulo de la historia que el jugador debe jugar ahora (null si ya terminó). */
    val capituloObjetivo: Capitulo?
        get() = Historia.capituloActual(retosCompletados)

    val puntoObjetivo: PointOfInterest?
        get() = capituloObjetivo?.let { cap -> puntosDeInteres.find { it.id == cap.puntoId } }

    fun calcularDistanciaMetros(punto: PointOfInterest): Double? {
        if (modoSimulacionGps) return 0.0
        val ubi = ubicacionUsuario ?: return null
        val r = 6371000.0
        val dLat = Math.toRadians(punto.latitud - ubi.first)
        val dLon = Math.toRadians(punto.longitud - ubi.second)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(ubi.first)) * Math.cos(Math.toRadians(punto.latitud)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    fun obtenerEstadoReto(punto: PointOfInterest): EstadoEdificioReto {
        if (!punto.tieneRetoDisponible) return EstadoEdificioReto.SIN_RETO

        val retoId = Historia.retoIdParaPunto(punto.id)

        if (retosCompletados.contains(retoId)) {
            return EstadoEdificioReto.YA_COMPLETADO
        }

        // Validar si requiere ítem de historia que no se posee
        val reqItem = punto.itemRequeridoId
        if (reqItem != null && !itemsUsuario.contains(reqItem)) {
            return EstadoEdificioReto.BLOQUEADO_HISTORIA
        }

        if (modoSimulacionGps) {
            return EstadoEdificioReto.LISTO_PARA_JUGAR
        }

        val dist = calcularDistanciaMetros(punto)
        return if (dist == null || dist > 75.0) {
            EstadoEdificioReto.FUERA_DE_RANGO
        } else {
            EstadoEdificioReto.LISTO_PARA_JUGAR
        }
    }

    fun obtenerItemRequeridoInfo(punto: PointOfInterest): ItemInventario? {
        val reqId = punto.itemRequeridoId ?: return null
        return CatalogoItems.obtenerPorId(reqId)
    }
}

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
        observarPerfilUsuario()
    }

    private fun observarPerfilUsuario() {
        viewModelScope.launch {
            userRepository.userProfileFlow.collect { perfil ->
                _mapState.update {
                    it.copy(
                        nivelUsuario = perfil.nivel,
                        xpUsuario = perfil.xp,
                        estrellasUsuario = perfil.estrellas,
                        itemsUsuario = perfil.itemsDesbloqueados.toSet(),
                        retosCompletados = perfil.retosCompletados.toSet()
                    )
                }
            }
        }
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
                        estrellasUsuario = perfil.estrellas,
                        itemsUsuario = perfil.itemsDesbloqueados.toSet(),
                        retosCompletados = perfil.retosCompletados.toSet()
                    )
                }
            }
        }
    }

    fun actualizarUbicacionUsuario(lat: Double, lon: Double) {
        _mapState.update { it.copy(ubicacionUsuario = Pair(lat, lon)) }
    }

    fun toggleModoSimulacion() {
        _mapState.update { it.copy(modoSimulacionGps = !it.modoSimulacionGps) }
    }

    fun simularUbicacionEnPunto(punto: PointOfInterest) {
        _mapState.update {
            it.copy(
                ubicacionUsuario = Pair(punto.latitud, punto.longitud),
                modoSimulacionGps = true
            )
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
        viewModelScope.launch { userRepository.registrarLugarDescubierto(punto.id) }
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
            puntos.add(
                PointOfInterest(
                    id = "poi_plaza_che",
                    nombre = "Plaza Central Santander (Plaza Che)",
                    edificio = "Plaza Central",
                    descripcion = "El corazón neurálgico y punto de encuentro más icónico de la UNAL.",
                    latitud = 4.63545,
                    longitud = -74.08274
                )
            )
        }

        // Marcar los puntos que son capítulos de la historia y el ítem que exige cada uno
        return puntos.map { punto ->
            val capitulo = Historia.capituloPorPunto(punto.id) ?: return@map punto
            punto.copy(
                tieneRetoDisponible = true,
                capituloHistoria = capitulo.numero,
                itemRequeridoId = capitulo.itemRequeridoId
            )
        }
    }
}
