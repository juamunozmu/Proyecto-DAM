package com.unal.nachoquest.ui.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.domain.model.Capitulo
import com.unal.nachoquest.domain.model.CategoriaLugar
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.domain.model.PointOfInterest
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.components.pulso
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private val Azul = Color(0xFF1565C0)

/** Capa de ubicación que avisa de cada nueva posición (la de osmdroid solo avisa la primera). */
private class CapaUbicacion(
    proveedor: GpsMyLocationProvider,
    mapa: MapView,
    private val alMoverse: (Double, Double) -> Unit
) : MyLocationNewOverlay(proveedor, mapa) {
    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        super.onLocationChanged(location, source)
        location?.let { loc -> mMapView?.post { alMoverse(loc.latitude, loc.longitude) } }
    }
}

/** Lo que define cómo se ven los marcadores: solo se redibujan si esto cambia. */
private data class FirmaMarcadores(
    val seleccionado: String?,
    val objetivo: String?,
    val retos: Set<String>,
    val items: Set<String>,
    val filtro: CategoriaLugar?
)

private class CacheMapa {
    var firma: FirmaMarcadores? = null
    var puntosRuta: List<GeoPoint>? = null
    var lineaRuta: Polyline? = null
    var clusterer: RadiusMarkerClusterer? = null
    val sueltos = mutableListOf<Marker>()
    val pines = mutableMapOf<String, Drawable>()
}

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onIniciarReto: (String) -> Unit = {}
) {
    val state by viewModel.mapState.collectAsState()
    val context = LocalContext.current

    var mapViewReference by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    val cache = remember { CacheMapa() }

    // Solicitar permiso de GPS
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationOverlay?.enableMyLocation()
        } else {
            Toast.makeText(context, "Sin GPS no podemos guiarte por el campus", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Avisos puntuales (p. ej. sin GPS)
    LaunchedEffect(state.aviso) {
        state.aviso?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.descartarAviso()
        }
    }

    // Al trazar una ruta nueva, encuadrarla completa
    LaunchedEffect(state.ruta?.puntos) {
        val puntos = state.ruta?.puntos ?: return@LaunchedEffect
        if (puntos.size >= 2) {
            mapViewReference?.post {
                mapViewReference?.zoomToBoundingBox(BoundingBox.fromGeoPoints(puntos).increaseByScale(1.6f), true)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Zoom con la rueda del ratón (emulador)
                awaitEachGesture {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Scroll) {
                        val scrollDelta = event.changes.first().scrollDelta.y
                        if (scrollDelta > 0) mapViewReference?.controller?.zoomOut()
                        else if (scrollDelta < 0) mapViewReference?.controller?.zoomIn()
                    }
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                Configuration.getInstance().userAgentValue = ctx.packageName
                MapView(ctx).apply {
                    mapViewReference = this
                    setTileSource(TileSourceFactory.MAPNIK)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    setMultiTouchControls(true)
                    controller.setZoom(17.0)
                    controller.setCenter(GeoPoint(4.636, -74.083))
                    setScrollableAreaLimitDouble(BoundingBox(4.650, -74.070, 4.620, -74.100))
                    minZoomLevel = 14.5
                    maxZoomLevel = 20.0

                    val capa = CapaUbicacion(GpsMyLocationProvider(ctx), this) { lat, lon ->
                        viewModel.actualizarUbicacionUsuario(lat, lon)
                    }
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        capa.enableMyLocation()
                    }
                    overlays.add(capa)
                    locationOverlay = capa
                }
            },
            update = { mapView ->
                actualizarRuta(mapView, state.ruta, cache)
                actualizarMarcadores(mapView, state, cache, context, viewModel)
            },
            modifier = Modifier.fillMaxSize()
        )

        // Parte superior: buscador, filtros y misión / navegación
        Column(modifier = Modifier.align(Alignment.TopCenter)) {
            BarraBusqueda(
                texto = state.searchQuery,
                modoSimulacion = state.modoSimulacionGps,
                onTexto = viewModel::actualizarBusqueda,
                onLimpiar = viewModel::limpiarBusqueda,
                onToggleSimulacion = viewModel::toggleModoSimulacion
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoriaLugar.filtros) { categoria ->
                    val activo = state.filtroCategoria == categoria
                    Text(
                        text = "${categoria.emoji} ${categoria.etiqueta}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (activo) Color.White else Color(0xFF333333),
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (activo) Color(categoria.colorArgb) else Color.White)
                            .clickable { viewModel.alternarFiltro(categoria) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Resultados de búsqueda o filtro
            AnimatedVisibility(visible = state.resultadosBusqueda.isNotEmpty()) {
                ListaResultados(
                    resultados = state.resultadosBusqueda,
                    estado = state,
                    onElegir = { punto ->
                        viewModel.limpiarBusqueda()
                        viewModel.seleccionarPunto(punto)
                        mapViewReference?.let { enfocar(it, punto) }
                    }
                )
            }

            val sinPanelesAbiertos = state.resultadosBusqueda.isEmpty() && state.puntoSeleccionado == null && state.llegadaA == null
            AnimatedVisibility(visible = sinPanelesAbiertos && (state.ruta != null || state.calculandoRuta)) {
                BannerNavegacion(state.ruta, state.calculandoRuta, onCancelar = viewModel::cancelarRuta)
            }
            AnimatedVisibility(visible = sinPanelesAbiertos && state.ruta == null && !state.calculandoRuta) {
                BannerObjetivo(
                    capitulo = state.capituloObjetivo,
                    onIr = {
                        state.puntoObjetivo?.let { punto ->
                            viewModel.seleccionarPunto(punto)
                            mapViewReference?.let { enfocar(it, punto) }
                        }
                    }
                )
            }
        }

        // Botón para centrar en el GPS
        FloatingActionButton(
            onClick = {
                val myLoc = locationOverlay?.myLocation
                if (myLoc != null) {
                    viewModel.actualizarUbicacionUsuario(myLoc.latitude, myLoc.longitude)
                    mapViewReference?.controller?.animateTo(myLoc, 18.0, 1000L)
                } else {
                    Toast.makeText(context, "Esperando señal de GPS...", Toast.LENGTH_SHORT).show()
                }
            },
            containerColor = Color.White,
            contentColor = NachoGreen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Centrar en mi ubicación")
        }

        // Ficha del lugar seleccionado
        AnimatedVisibility(
            visible = state.puntoSeleccionado != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            state.puntoSeleccionado?.let { punto ->
                FichaLugar(
                    punto = punto,
                    estadoReto = state.obtenerEstadoReto(punto),
                    distanciaMetros = state.calcularDistanciaMetros(punto),
                    itemRequerido = state.obtenerItemRequeridoInfo(punto),
                    modoSimulacion = state.modoSimulacionGps,
                    onComoLlegar = { viewModel.trazarRuta(punto) },
                    onSimularAqui = { viewModel.simularUbicacionEnPunto(punto) },
                    onDismiss = viewModel::cerrarDetalle,
                    onIniciarReto = onIniciarReto
                )
            }
        }

        // Llegada al destino
        AnimatedVisibility(
            visible = state.llegadaA != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            state.llegadaA?.let { punto ->
                TarjetaLlegada(
                    punto = punto,
                    hayReto = state.obtenerEstadoReto(punto) == EstadoEdificioReto.LISTO_PARA_JUGAR,
                    onIniciarReto = {
                        viewModel.cerrarLlegada()
                        onIniciarReto(punto.id)
                    },
                    onCerrar = viewModel::cerrarLlegada
                )
            }
        }
    }
}

// ---------- Overlays de osmdroid ----------

private const val ZOOM_ENFOQUE = 18.0

/** Centra el mapa en un lugar dejándolo en la mitad superior, para que la ficha no lo tape. */
private fun enfocar(mapView: MapView, punto: PointOfInterest) {
    val metrosPorPixel = TileSystem.GroundResolution(punto.latitud, ZOOM_ENFOQUE)
    val desplazamientoMetros = mapView.height * 0.22 * metrosPorPixel
    val centro = GeoPoint(punto.latitud - desplazamientoMetros / 111_320.0, punto.longitud)
    mapView.controller.animateTo(centro, ZOOM_ENFOQUE, 900L)
}

private fun actualizarRuta(mapView: MapView, ruta: Ruta?, cache: CacheMapa) {
    if (cache.puntosRuta == ruta?.puntos) return
    cache.lineaRuta?.let { mapView.overlays.remove(it) }
    cache.lineaRuta = ruta?.let {
        Polyline(mapView).apply {
            setPoints(it.puntos)
            outlinePaint.color = Azul.toArgb()
            outlinePaint.strokeWidth = 14f
            outlinePaint.strokeCap = Paint.Cap.ROUND
            outlinePaint.strokeJoin = Paint.Join.ROUND
            if (it.aproximada) {
                outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(30f, 20f), 0f)
            }
            setOnClickListener { _, _, _ -> false }
        }.also { linea -> mapView.overlays.add(0, linea) }
    }
    cache.puntosRuta = ruta?.puntos
    mapView.invalidate()
}

private fun actualizarMarcadores(
    mapView: MapView,
    state: MapUiState,
    cache: CacheMapa,
    context: Context,
    viewModel: MapViewModel
) {
    val objetivo = state.puntoObjetivo?.id
    val firma = FirmaMarcadores(state.puntoSeleccionado?.id, objetivo, state.retosCompletados, state.itemsUsuario, state.filtroCategoria)
    if (firma == cache.firma) return
    cache.firma = firma

    cache.clusterer?.let { mapView.overlays.remove(it) }
    cache.sueltos.forEach { mapView.overlays.remove(it) }
    cache.sueltos.clear()

    val clusterer = RadiusMarkerClusterer(context).apply {
        setIcon(crearBurbujaGrupo(context))
        textPaint.textSize = 34f
        textPaint.color = android.graphics.Color.WHITE
        textPaint.isFakeBoldText = true
        mAnchorV = Marker.ANCHOR_CENTER
        mAnchorU = Marker.ANCHOR_CENTER
        setRadius(90)
        // Con zoom cercano se ven todos los edificios; solo se agrupan al alejarse
        setMaxClusteringZoomLevel(17)
    }

    fun pin(clave: String, crear: () -> Drawable) = cache.pines.getOrPut(clave, crear)

    state.puntosDeInteres.forEach { punto ->
        val seleccionado = punto.id == state.puntoSeleccionado?.id
        val esObjetivo = punto.id == objetivo
        val filtrado = state.filtroCategoria == null || punto.categoria == state.filtroCategoria
        // Los edificios internos solo aparecen si se buscan
        if (!seleccionado && !esObjetivo && (!punto.categoria.visibleEnMapa || !filtrado)) return@forEach

        val estado = state.obtenerEstadoReto(punto)
        val categoria = punto.categoria
        val icono = when {
            seleccionado -> pin("sel_${categoria.name}") {
                crearPin(context, android.graphics.Color.rgb(211, 47, 47), categoria.emoji, grande = true)
            }
            esObjetivo -> pin("objetivo") { crearPin(context, android.graphics.Color.rgb(212, 160, 23), "⭐", grande = true) }
            estado == EstadoEdificioReto.YA_COMPLETADO -> pin("completado") {
                crearPin(context, android.graphics.Color.rgb(30, 136, 229), "✅", grande = false)
            }
            estado == EstadoEdificioReto.BLOQUEADO_HISTORIA -> pin("bloqueado") {
                crearPin(context, android.graphics.Color.rgb(230, 81, 0), "🔒", grande = false)
            }
            else -> pin(categoria.name) { crearPin(context, categoria.colorArgb, categoria.emoji, grande = false) }
        }

        val marker = Marker(mapView).apply {
            position = GeoPoint(punto.latitud, punto.longitud)
            this.icon = icono
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = punto.nombre
            setInfoWindow(null)
            setOnMarkerClickListener { _, _ ->
                viewModel.seleccionarPunto(punto)
                true
            }
        }
        // El seleccionado y el objetivo nunca se agrupan, para que siempre se vean
        if (seleccionado || esObjetivo) cache.sueltos.add(marker) else clusterer.add(marker)
    }

    mapView.overlays.add(clusterer)
    mapView.overlays.addAll(cache.sueltos)
    cache.clusterer = clusterer
    mapView.invalidate()
}

/** Pin con forma de gota y un emoji en el centro. */
private fun crearPin(context: Context, color: Int, emoji: String, grande: Boolean): Drawable {
    val density = context.resources.displayMetrics.density
    val width = ((if (grande) 42 else 32) * density).toInt()
    val height = ((if (grande) 54 else 42) * density).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val radius = width / 2f

    val path = Path().apply {
        arcTo(RectF(0f, 0f, width.toFloat(), width.toFloat()), 160f, 220f, false)
        lineTo(radius, height.toFloat())
        close()
    }
    canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    canvas.drawPath(path, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.2f * density
    })
    canvas.drawCircle(radius, radius, radius * 0.68f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = android.graphics.Color.WHITE })

    val texto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = radius * 0.9f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(emoji, radius, radius - (texto.descent() + texto.ascent()) / 2, texto)
    return BitmapDrawable(context.resources, bitmap)
}

/** Burbuja para los grupos de marcadores cercanos. */
private fun crearBurbujaGrupo(context: Context): Bitmap {
    val density = context.resources.displayMetrics.density
    val lado = (40 * density).toInt()
    val bitmap = Bitmap.createBitmap(lado, lado, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawCircle(lado / 2f, lado / 2f, lado / 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = NachoGreen.toArgb() })
    canvas.drawCircle(lado / 2f, lado / 2f, lado / 2f - 2 * density, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
    })
    return bitmap
}

// ---------- Componentes de la interfaz ----------

@Composable
private fun BarraBusqueda(
    texto: String,
    modoSimulacion: Boolean,
    onTexto: (String) -> Unit,
    onLimpiar: () -> Unit,
    onToggleSimulacion: () -> Unit
) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTexto,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
            .shadow(4.dp, RoundedCornerShape(28.dp)),
        placeholder = { Text("¿A dónde vas? (ej: 401, cafetería)", maxLines = 1) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
        trailingIcon = {
            if (texto.isNotEmpty()) {
                IconButton(onClick = onLimpiar) { Icon(Icons.Default.Close, contentDescription = "Limpiar") }
            } else {
                // Modo de pruebas: permite jugar los retos sin estar en el campus
                Text(
                    text = if (modoSimulacion) "DEV" else "🛰️",
                    fontSize = if (modoSimulacion) 11.sp else 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (modoSimulacion) Color.White else Color.Gray,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (modoSimulacion) NachoGreenDark else Color.Transparent)
                        .clickable(onClick = onToggleSimulacion)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = NachoGreen
        )
    )
}

@Composable
private fun ListaResultados(
    resultados: List<PointOfInterest>,
    estado: MapUiState,
    onElegir: (PointOfInterest) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .heightIn(max = 300.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        items(resultados, key = { it.id }) { punto ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onElegir(punto) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(punto.categoria.colorArgb).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(punto.categoria.emoji, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(punto.nombre, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E), maxLines = 1)
                    Text(textoEdificio(punto), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                estado.calcularDistanciaMetros(punto)?.takeIf { !estado.modoSimulacionGps }?.let {
                    Text(formatearDistancia(it), fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private fun textoEdificio(punto: PointOfInterest): String =
    if (punto.edificio.any { it.isDigit() }) "Edificio ${punto.edificio} · ${punto.categoria.etiqueta}"
    else punto.categoria.etiqueta

@Composable
private fun BannerNavegacion(ruta: Ruta?, calculando: Boolean, onCancelar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Azul)
            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (calculando || ruta == null) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Buscando el mejor camino...", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        } else {
            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${formatearDistancia(ruta.distanciaRestanteMetros)} · ${ruta.minutosRestantes} min a pie",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                )
                Text("Hacia ${ruta.destino.nombre}", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 1)
                if (ruta.aproximada) {
                    Text("Sin conexión: se muestra la dirección en línea recta", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        }
        IconButton(onClick = onCancelar) {
            Icon(Icons.Default.Close, contentDescription = "Terminar navegación", tint = Color.White)
        }
    }
}

@Composable
private fun BannerObjetivo(capitulo: Capitulo?, onIr: () -> Unit) {
    if (capitulo == null) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(NachoGreenDark)
            .clickable(onClick = onIr)
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Nacho(modifier = Modifier.size(40.dp), expresion = Expresion.DECIDIDO)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "MISIÓN · CAPÍTULO ${capitulo.numero}",
                color = NachoYellow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(capitulo.lugar, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, fontSize = 14.sp)
        }
        Box(
            modifier = Modifier
                .pulso()
                .clip(RoundedCornerShape(10.dp))
                .background(NachoYellow)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("Ver ➜", color = NachoGreenDark, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun FichaLugar(
    punto: PointOfInterest,
    estadoReto: EstadoEdificioReto,
    distanciaMetros: Double?,
    itemRequerido: ItemInventario?,
    modoSimulacion: Boolean,
    onComoLlegar: () -> Unit,
    onSimularAqui: () -> Unit,
    onDismiss: () -> Unit,
    onIniciarReto: (String) -> Unit
) {
    val categoria = punto.categoria
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .shadow(10.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(categoria.colorArgb).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(categoria.emoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(punto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E1E1E))
                val distancia = distanciaMetros?.takeIf { !modoSimulacion }?.let { " · a ${formatearDistancia(it)}" }.orEmpty()
                Text(textoEdificio(punto) + distancia, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.Gray)
            }
        }

        if (punto.descripcion.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(punto.descripcion, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
        }

        // Estado del capítulo de la historia en este lugar
        if (punto.tieneRetoDisponible && estadoReto != EstadoEdificioReto.SIN_RETO) {
            Spacer(modifier = Modifier.height(10.dp))
            val (fondo, colorTexto, texto) = when (estadoReto) {
                EstadoEdificioReto.BLOQUEADO_HISTORIA -> Triple(
                    Color(0xFFFFF3E0), Color(0xFFE65100),
                    "🔒 Capítulo ${punto.capituloHistoria} bloqueado · Necesitas: ${itemRequerido?.nombre ?: "un ítem"}"
                )
                EstadoEdificioReto.FUERA_DE_RANGO -> Triple(
                    Color(0xFFE3F2FD), Azul,
                    "📖 Aquí está el capítulo ${punto.capituloHistoria ?: ""}. Acércate a menos de 75 m para jugarlo."
                )
                EstadoEdificioReto.LISTO_PARA_JUGAR -> Triple(
                    NachoGreen.copy(alpha = 0.1f), NachoGreenDark,
                    if (modoSimulacion) "⚡ Modo de pruebas: reto disponible" else "⚡ ¡Estás aquí! El reto está disponible"
                )
                else -> Triple(
                    NachoYellow.copy(alpha = 0.2f), Color(0xFF8D6E00),
                    "✅ Capítulo completado. Puedes repetirlo para mejorar tus estrellas."
                )
            }
            Text(
                texto,
                color = colorTexto,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(fondo)
                    .padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            val puedeJugar = estadoReto == EstadoEdificioReto.LISTO_PARA_JUGAR || estadoReto == EstadoEdificioReto.YA_COMPLETADO
            if (puedeJugar) {
                Button(
                    onClick = { onIniciarReto(punto.id) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NachoGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (estadoReto == EstadoEdificioReto.YA_COMPLETADO) "Repetir" else "Jugar", fontWeight = FontWeight.Bold, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onComoLlegar,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cómo llegar", fontWeight = FontWeight.Bold, color = Azul, maxLines = 1)
                }
            } else {
                Button(
                    onClick = onComoLlegar,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cómo llegar", fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }

        // Atajo para probar los retos sin ir al campus
        if (estadoReto == EstadoEdificioReto.FUERA_DE_RANGO) {
            Text(
                "⚡ Simular que estoy aquí (modo de pruebas)",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSimularAqui)
                    .padding(6.dp)
            )
        }
    }
}

@Composable
private fun TarjetaLlegada(
    punto: PointOfInterest,
    hayReto: Boolean,
    onIniciarReto: () -> Unit,
    onCerrar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .shadow(10.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Nacho(modifier = Modifier.size(80.dp), expresion = Expresion.FELIZ, saltando = true)
        Text("¡Llegaste!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = NachoGreenDark)
        Text(punto.nombre, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (hayReto) {
                Button(
                    onClick = onIniciarReto,
                    colors = ButtonDefaults.buttonColors(containerColor = NachoGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Jugar el capítulo", fontWeight = FontWeight.Bold)
                }
            }
            OutlinedButton(
                onClick = onCerrar,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Text("Listo", color = NachoGreenDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}
