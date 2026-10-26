package com.unal.nachoquest.ui.screens.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.Capitulo
import com.unal.nachoquest.domain.model.CatalogoItems
import com.unal.nachoquest.domain.model.CategoriaLugar
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.domain.model.PointOfInterest
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint
import java.text.Normalizer
import javax.inject.Inject

private const val RADIO_RETO_METROS = 75.0
private const val RADIO_LLEGADA_METROS = 25.0
private const val DESVIO_PARA_RECALCULAR_METROS = 45.0
private const val MS_ENTRE_RECALCULOS = 20_000L
// Velocidad media caminando, para estimar el tiempo si no hay servicio de rutas
private const val VELOCIDAD_PEATON_MS = 1.3

enum class EstadoEdificioReto {
    BLOQUEADO_HISTORIA, // Falta ítem requerido
    FUERA_DE_RANGO,     // Desbloqueado pero a > 75m
    LISTO_PARA_JUGAR,   // En rango (< 75m) o simulación dev activa
    YA_COMPLETADO,      // Reto superado
    SIN_RETO            // Edificio informativo del campus
}

/** Ruta a pie hacia un lugar del campus. */
data class Ruta(
    val destino: PointOfInterest,
    val puntos: List<GeoPoint>,
    val distanciaTotalMetros: Double,
    val duracionSegundos: Double,
    val distanciaRestanteMetros: Double,
    // true si no hubo conexión con el servicio de rutas y se muestra una línea recta
    val aproximada: Boolean
) {
    val minutosRestantes: Int
        get() {
            val velocidad = if (duracionSegundos > 0) distanciaTotalMetros / duracionSegundos else VELOCIDAD_PEATON_MS
            return maxOf(1, (distanciaRestanteMetros / velocidad / 60).toInt())
        }
}

data class MapUiState(
    val puntosDeInteres: List<PointOfInterest> = emptyList(),
    val puntoSeleccionado: PointOfInterest? = null,
    val itemsUsuario: Set<String> = emptySet(),
    val retosCompletados: Set<String> = emptySet(),
    val ubicacionUsuario: Pair<Double, Double>? = null,
    val modoSimulacionGps: Boolean = false,
    val searchQuery: String = "",
    val filtroCategoria: CategoriaLugar? = null,
    val resultadosBusqueda: List<PointOfInterest> = emptyList(),
    val ruta: Ruta? = null,
    val calculandoRuta: Boolean = false,
    val llegadaA: PointOfInterest? = null,
    val aviso: String? = null
) {
    /** Capítulo de la historia que el jugador debe jugar ahora (null si ya terminó). */
    val capituloObjetivo: Capitulo?
        get() = Historia.capituloActual(retosCompletados)

    val puntoObjetivo: PointOfInterest?
        get() = capituloObjetivo?.let { cap -> puntosDeInteres.find { it.id == cap.puntoId } }

    fun calcularDistanciaMetros(punto: PointOfInterest): Double? {
        if (modoSimulacionGps) return 0.0
        val ubi = ubicacionUsuario ?: return null
        return distanciaMetros(ubi.first, ubi.second, punto.latitud, punto.longitud)
    }

    fun obtenerEstadoReto(punto: PointOfInterest): EstadoEdificioReto {
        if (!punto.tieneRetoDisponible) return EstadoEdificioReto.SIN_RETO

        if (Historia.retoIdParaPunto(punto.id) in retosCompletados) {
            return EstadoEdificioReto.YA_COMPLETADO
        }

        // Validar si requiere ítem de historia que no se posee
        val reqItem = punto.itemRequeridoId
        if (reqItem != null && !itemsUsuario.contains(reqItem)) {
            return EstadoEdificioReto.BLOQUEADO_HISTORIA
        }

        val dist = calcularDistanciaMetros(punto)
        return if (dist == null || dist > RADIO_RETO_METROS) {
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

    private val roadManager by lazy {
        OSRMRoadManager(context, context.packageName).apply { setMean(OSRMRoadManager.MEAN_BY_FOOT) }
    }
    private var trabajoRuta: Job? = null
    private var ultimoCalculoRuta = 0L

    init {
        viewModelScope.launch {
            userRepository.userProfileFlow.collect { perfil ->
                _mapState.update {
                    it.copy(
                        itemsUsuario = perfil.itemsDesbloqueados.toSet(),
                        retosCompletados = perfil.retosCompletados.toSet()
                    )
                }
            }
        }
        viewModelScope.launch { userRepository.getUserProfile() }
    }

    fun actualizarUbicacionUsuario(lat: Double, lon: Double) {
        if (_mapState.value.modoSimulacionGps) return
        _mapState.update { it.copy(ubicacionUsuario = lat to lon) }
        seguirRuta(lat, lon)
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
        _mapState.update { it.copy(searchQuery = query, filtroCategoria = null).conResultados() }
    }

    fun alternarFiltro(categoria: CategoriaLugar) {
        _mapState.update {
            val nuevo = if (it.filtroCategoria == categoria) null else categoria
            it.copy(filtroCategoria = nuevo, searchQuery = "").conResultados()
        }
    }

    fun limpiarBusqueda() {
        _mapState.update { it.copy(searchQuery = "", filtroCategoria = null, resultadosBusqueda = emptyList()) }
    }

    private fun MapUiState.conResultados(): MapUiState {
        val q = normalizar(searchQuery)
        val resultados = when {
            filtroCategoria != null -> puntosDeInteres.filter { it.categoria == filtroCategoria }
            q.isBlank() -> emptyList()
            else -> puntosDeInteres.filter { punto ->
                normalizar(punto.nombre).contains(q) ||
                    normalizar(punto.edificio).contains(q) ||
                    punto.alias.any { normalizar(it).contains(q) }
            }
        }
        return copy(resultadosBusqueda = resultados.sortedBy { p -> calcularDistanciaMetros(p) ?: Double.MAX_VALUE })
    }

    fun seleccionarPunto(punto: PointOfInterest) {
        _mapState.update { it.copy(puntoSeleccionado = punto) }
        viewModelScope.launch { userRepository.registrarLugarDescubierto(punto.id) }
    }

    fun cerrarDetalle() {
        _mapState.update { it.copy(puntoSeleccionado = null) }
    }

    fun descartarAviso() {
        _mapState.update { it.copy(aviso = null) }
    }

    fun cerrarLlegada() {
        _mapState.update { it.copy(llegadaA = null) }
    }

    // ---------- Rutas a pie ----------

    fun trazarRuta(destino: PointOfInterest) {
        val origen = _mapState.value.ubicacionUsuario
        if (origen == null) {
            _mapState.update { it.copy(aviso = "Aún no tenemos tu ubicación. Activa el GPS y espera unos segundos.") }
            return
        }
        _mapState.update { it.copy(puntoSeleccionado = null, llegadaA = null, calculandoRuta = true) }
        calcularRuta(origen, destino)
    }

    fun cancelarRuta() {
        trabajoRuta?.cancel()
        _mapState.update { it.copy(ruta = null, calculandoRuta = false) }
    }

    private fun calcularRuta(origen: Pair<Double, Double>, destino: PointOfInterest) {
        trabajoRuta?.cancel()
        ultimoCalculoRuta = System.currentTimeMillis()
        trabajoRuta = viewModelScope.launch {
            val desde = GeoPoint(origen.first, origen.second)
            val hasta = GeoPoint(destino.latitud, destino.longitud)
            val road = withContext(Dispatchers.IO) {
                try {
                    roadManager.getRoad(arrayListOf(desde, hasta))
                } catch (e: Exception) {
                    null
                }
            }
            val ruta = if (road != null && road.mStatus == Road.STATUS_OK && road.mRouteHigh.size >= 2) {
                Ruta(
                    destino = destino,
                    puntos = road.mRouteHigh,
                    distanciaTotalMetros = road.mLength * 1000,
                    duracionSegundos = road.mDuration,
                    distanciaRestanteMetros = road.mLength * 1000,
                    aproximada = false
                )
            } else {
                val metros = distanciaMetros(desde.latitude, desde.longitude, hasta.latitude, hasta.longitude)
                Ruta(destino, listOf(desde, hasta), metros, metros / VELOCIDAD_PEATON_MS, metros, aproximada = true)
            }
            _mapState.update { it.copy(ruta = ruta, calculandoRuta = false) }
        }
    }

    /** Actualiza la distancia restante, detecta la llegada y recalcula si el jugador se desvía. */
    private fun seguirRuta(lat: Double, lon: Double) {
        val ruta = _mapState.value.ruta ?: return
        val destino = ruta.destino

        if (distanciaMetros(lat, lon, destino.latitud, destino.longitud) <= RADIO_LLEGADA_METROS) {
            _mapState.update { it.copy(ruta = null, llegadaA = destino, puntoSeleccionado = null) }
            return
        }

        // Punto de la ruta más cercano al jugador
        val (indice, desvio) = ruta.puntos.withIndex()
            .map { (i, p) -> i to distanciaMetros(lat, lon, p.latitude, p.longitude) }
            .minBy { it.second }

        if (!ruta.aproximada && desvio > DESVIO_PARA_RECALCULAR_METROS &&
            System.currentTimeMillis() - ultimoCalculoRuta > MS_ENTRE_RECALCULOS
        ) {
            calcularRuta(lat to lon, destino)
            return
        }

        var restante = desvio
        for (i in indice until ruta.puntos.lastIndex) {
            val a = ruta.puntos[i]
            val b = ruta.puntos[i + 1]
            restante += distanciaMetros(a.latitude, a.longitude, b.latitude, b.longitude)
        }
        if (ruta.aproximada) restante = distanciaMetros(lat, lon, destino.latitud, destino.longitud)
        _mapState.update { it.copy(ruta = ruta.copy(distanciaRestanteMetros = restante)) }
    }

    // ---------- Datos ----------

    private fun puntosIniciales(): List<PointOfInterest> {
        val puntos = mutableListOf<PointOfInterest>()
        try {
            val jsonString = context.assets.open("edificios_unal.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            val jsonArray = JSONObject(jsonString).getJSONArray("puntos")

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)

                val aliasArray = item.getJSONArray("alias")
                val aliasList = List(aliasArray.length()) { j -> aliasArray.getString(j) }

                puntos.add(
                    PointOfInterest(
                        id = item.getString("id"),
                        nombre = item.getString("nombre"),
                        edificio = item.getString("edificio"),
                        descripcion = item.optString("descripcion"),
                        latitud = item.getDouble("latitud"),
                        longitud = item.getDouble("longitud"),
                        categoria = CategoriaLugar.desde(item.optString("categoria")),
                        tieneRetoDisponible = item.optBoolean("tieneRetoDisponible", false),
                        alias = aliasList
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
                    longitud = -74.08274,
                    categoria = CategoriaLugar.PLAZA
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

fun distanciaMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2)
    return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

fun formatearDistancia(metros: Double): String =
    if (metros >= 1000) String.format("%.1f km", metros / 1000) else "${metros.toInt()} m"

private fun normalizar(texto: String): String =
    Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").trim()
