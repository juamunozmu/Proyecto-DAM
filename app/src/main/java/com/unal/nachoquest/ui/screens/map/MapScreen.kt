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
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
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
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onIniciarReto: (String) -> Unit = {}
) {
    val state by viewModel.mapState.collectAsState()
    val context = LocalContext.current

    var mapViewReference by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    // Solicitar permiso de GPS
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationOverlay?.enableMyLocation()
        } else {
            Toast.makeText(context, "Se requiere GPS para calcular la proximidad a los edificios", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Scroll) {
                        val scrollDelta = event.changes.first().scrollDelta.y
                        if (scrollDelta > 0) {
                            mapViewReference?.controller?.zoomOut()
                        } else if (scrollDelta < 0) {
                            mapViewReference?.controller?.zoomIn()
                        }
                    }
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewReference = this
                    setTileSource(TileSourceFactory.MAPNIK)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
                    setMultiTouchControls(true)

                    val unalCenter = GeoPoint(4.636, -74.083)
                    controller.setZoom(17.0)
                    controller.setCenter(unalCenter)

                    setScrollableAreaLimitDouble(
                        org.osmdroid.util.BoundingBox(4.650, -74.070, 4.620, -74.100)
                    )
                    minZoomLevel = 13.5
                    maxZoomLevel = 20.0

                    // Capa de Ubicación GPS
                    val myLocation = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        myLocation.enableMyLocation()
                    }
                    myLocation.runOnFirstFix {
                        val loc = myLocation.myLocation
                        if (loc != null) {
                            (ctx as? android.app.Activity)?.runOnUiThread {
                                viewModel.actualizarUbicacionUsuario(loc.latitude, loc.longitude)
                            }
                        }
                    }
                    this.overlays.add(myLocation)
                    locationOverlay = myLocation
                }
            },
            update = { mapView ->
                mapView.overlays.removeAll { it is Marker || it is MapEventsOverlay || it is Polygon }

                val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                })
                mapView.overlays.add(0, mapEventsOverlay)

                val clusterer = RadiusMarkerClusterer(context).apply {
                    val bitmap = (ContextCompat.getDrawable(context, android.R.drawable.presence_online) as BitmapDrawable).bitmap
                    setIcon(bitmap)
                    textPaint.textSize = 36f
                    textPaint.color = android.graphics.Color.WHITE
                    mAnchorV = Marker.ANCHOR_CENTER
                    mAnchorU = Marker.ANCHOR_CENTER
                }

                val pinRojo = crearPinDrawable(context, android.graphics.Color.rgb(220, 20, 60), true)
                val pinVerde = crearPinDrawable(context, android.graphics.Color.rgb(46, 125, 50), false)
                val pinBloqueado = crearPinDrawable(context, android.graphics.Color.rgb(230, 81, 0), false) // Naranja/Ámbar
                val pinCompletado = crearPinDrawable(context, android.graphics.Color.rgb(30, 136, 229), false) // Azul
                val pinGeneral = crearPinDrawable(context, android.graphics.Color.rgb(120, 144, 156), false) // Gris azulado
                val pinObjetivo = crearPinDrawable(context, android.graphics.Color.rgb(212, 160, 23), true, conEstrella = true) // Dorado

                var marcadorSeleccionado: Marker? = null
                val idObjetivo = state.puntoObjetivo?.id
                val marcadoresSueltos = mutableListOf<Marker>()

                state.puntosDeInteres.forEach { punto ->
                    val isSelected = state.puntoSeleccionado?.id == punto.id
                    val estado = state.obtenerEstadoReto(punto)

                    val esObjetivo = punto.id == idObjetivo
                    val iconoPin = when {
                        isSelected -> pinRojo
                        esObjetivo -> pinObjetivo
                        estado == EstadoEdificioReto.BLOQUEADO_HISTORIA -> pinBloqueado
                        estado == EstadoEdificioReto.YA_COMPLETADO -> pinCompletado
                        estado == EstadoEdificioReto.LISTO_PARA_JUGAR || estado == EstadoEdificioReto.FUERA_DE_RANGO -> pinVerde
                        else -> pinGeneral
                    }

                    val marker = Marker(mapView).apply {
                        position = GeoPoint(punto.latitud, punto.longitud)
                        icon = iconoPin
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = punto.nombre
                        setOnMarkerClickListener { _, _ ->
                            viewModel.seleccionarPunto(punto)
                            true
                        }
                    }

                    when {
                        isSelected -> marcadorSeleccionado = marker
                        // El objetivo de la historia nunca se agrupa, para que siempre se vea
                        esObjetivo -> marcadoresSueltos.add(marker)
                        else -> clusterer.add(marker)
                    }
                }
                mapView.overlays.add(clusterer)
                mapView.overlays.addAll(marcadoresSueltos)

                marcadorSeleccionado?.let { selectedMarker ->
                    mapView.overlays.add(selectedMarker)
                    selectedMarker.showInfoWindow()
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // Barra superior con estatus, XP y Switch de Simulación Dev
        Column(modifier = Modifier.align(Alignment.TopCenter)) {
            TopStatusBar(
                nivel = state.nivelUsuario,
                xp = state.xpUsuario,
                estrellas = state.estrellasUsuario,
                modoSimulacion = state.modoSimulacionGps,
                onToggleSimulacion = { viewModel.toggleModoSimulacion() },
                modifier = Modifier.fillMaxWidth()
            )

            // Barra de búsqueda universal
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)),
                placeholder = { Text("Buscar edificio (ej: CYT, Biblioteca, 401)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Desplegable de resultados de búsqueda
            AnimatedVisibility(visible = state.resultadosBusqueda.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(state.resultadosBusqueda) { punto ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.actualizarBusqueda("")
                                        viewModel.seleccionarPunto(punto)
                                        mapViewReference?.controller?.animateTo(
                                            GeoPoint(punto.latitud, punto.longitud),
                                            18.0,
                                            1000
                                        )
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = NachoGreen)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(punto.nombre, fontWeight = FontWeight.Bold)
                                    Text("${punto.edificio} • ${punto.campus}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            // Banner con el objetivo actual de la historia
            AnimatedVisibility(visible = state.searchQuery.isBlank() && state.puntoSeleccionado == null) {
                BannerObjetivo(
                    capitulo = state.capituloObjetivo,
                    onIr = {
                        state.puntoObjetivo?.let { punto ->
                            viewModel.seleccionarPunto(punto)
                            mapViewReference?.controller?.animateTo(GeoPoint(punto.latitud, punto.longitud), 18.5, 1200)
                        }
                    }
                )
            }
        }

        // Botón flotante para centrar la cámara en el GPS
        FloatingActionButton(
            onClick = {
                val myLoc = locationOverlay?.myLocation
                if (myLoc != null) {
                    viewModel.actualizarUbicacionUsuario(myLoc.latitude, myLoc.longitude)
                    mapViewReference?.controller?.animateTo(myLoc, 18.0, 1000)
                } else {
                    Toast.makeText(context, "Esperando señal de GPS...", Toast.LENGTH_SHORT).show()
                }
            },
            containerColor = Color.White,
            contentColor = NachoGreen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (state.puntoSeleccionado != null) 310.dp else 16.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Centrar en mi ubicación")
        }

        // Tarjeta inferior de Detalle del Edificio seleccionado
        AnimatedVisibility(
            visible = state.puntoSeleccionado != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            state.puntoSeleccionado?.let { punto ->
                val estado = state.obtenerEstadoReto(punto)
                val distancia = state.calcularDistanciaMetros(punto)
                val itemReq = state.obtenerItemRequeridoInfo(punto)

                PointOfInterestCard(
                    punto = punto,
                    estadoReto = estado,
                    distanciaMetros = distancia,
                    itemRequerido = itemReq,
                    modoSimulacion = state.modoSimulacionGps,
                    onSimularAqui = { viewModel.simularUbicacionEnPunto(punto) },
                    onDismiss = { viewModel.cerrarDetalle() },
                    onIniciarReto = onIniciarReto
                )
            }
        }
    }
}

@Composable
private fun TopStatusBar(
    nivel: Int,
    xp: Int,
    estrellas: Int,
    modoSimulacion: Boolean,
    onToggleSimulacion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.95f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(NachoGreen)
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "Nivel $nivel",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(NachoYellow),
                contentAlignment = Alignment.Center
            ) {
                Text("⚡", fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "$xp", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = "Estrellas", tint = NachoYellow, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "$estrellas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Toggle Dev GPS Simulation
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (modoSimulacion) NachoGreenDark else Color(0xFFEEEEEE))
                .clickable { onToggleSimulacion() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (modoSimulacion) "⚡ Dev GPS: ON" else "🛰️ GPS Real",
                    color = if (modoSimulacion) Color.White else Color.DarkGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PointOfInterestCard(
    punto: PointOfInterest,
    estadoReto: EstadoEdificioReto,
    distanciaMetros: Double?,
    itemRequerido: ItemInventario?,
    modoSimulacion: Boolean,
    onSimularAqui: () -> Unit,
    onDismiss: () -> Unit,
    onIniciarReto: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val imageModel: Any? = remember(punto.imagenUrl) {
        val raw = punto.imagenUrl?.trim()
        if (raw.isNullOrBlank()) null
        else if (raw.startsWith("http://") || raw.startsWith("https://")) raw
        else {
            val cleanName = raw.substringBeforeLast(".")
            val resId = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
            if (resId != 0) resId
            else "file:///android_asset/$raw"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            if (imageModel != null) {
                coil3.compose.AsyncImage(
                    model = imageModel,
                    contentDescription = punto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(18.dp)) {
                // Insignia de Capítulo de Historia
                if (punto.capituloHistoria != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NachoGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "📖 CAPÍTULO ${punto.capituloHistoria} DE HISTORIA",
                            fontWeight = FontWeight.Bold,
                            color = NachoGreenDark,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NachoGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = NachoGreen, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = punto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "${punto.edificio} • ${punto.campus}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = punto.descripcion, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)

                // Banner Gamificado de Proximidad y Requisitos de Historia
                if (punto.tieneRetoDisponible) {
                    Spacer(modifier = Modifier.height(14.dp))

                    when (estadoReto) {
                        EstadoEdificioReto.BLOQUEADO_HISTORIA -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "🔒 Acceso Bloqueado por Historia",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFFE65100)
                                        )
                                        Text(
                                            "Requiere: ${itemRequerido?.nombre ?: "Ítem de misión"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black
                                        )
                                        itemRequerido?.requisitoTexto?.let {
                                            Text(
                                                "💡 Pista: $it",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        EstadoEdificioReto.FUERA_DE_RANGO -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.NearMe, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "📍 Fuera de Proximidad Física",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF1565C0)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val distTexto = if (distanciaMetros != null) {
                                        if (distanciaMetros > 1000) String.format("%.1f km", distanciaMetros / 1000)
                                        else "${distanciaMetros.toInt()} m"
                                    } else "GPS buscando..."
                                    Text(
                                        text = "Estás a $distTexto del edificio. Acércate a menos de 75 m para desbloquear el reto.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = onSimularAqui,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("⚡ Simular estar aquí (Modo Dev)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        EstadoEdificioReto.LISTO_PARA_JUGAR -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NachoGreen.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = NachoGreen, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "⚡ ¡En Rango! Reto Disponible",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = NachoGreenDark
                                        )
                                        Text(
                                            if (modoSimulacion) "Modo Dev activo: Acceso concedido"
                                            else "Estás en el campus: Responde 5 preguntas del lugar",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }

                        EstadoEdificioReto.YA_COMPLETADO -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NachoYellow.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFB78103), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "✅ Reto Completado",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFFB78103)
                                        )
                                        Text(
                                            "Ya has obtenido los ítems de este capítulo. Puedes volver a jugar para repasar.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }

                        EstadoEdificioReto.SIN_RETO -> {}
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de Acción
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (punto.tieneRetoDisponible) {
                        when (estadoReto) {
                            EstadoEdificioReto.LISTO_PARA_JUGAR -> {
                                Button(
                                    onClick = { onIniciarReto(punto.id) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NachoGreen),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Comenzar reto", fontWeight = FontWeight.Bold)
                                }
                            }
                            EstadoEdificioReto.YA_COMPLETADO -> {
                                Button(
                                    onClick = { onIniciarReto(punto.id) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Repetir reto", fontWeight = FontWeight.Bold)
                                }
                            }
                            EstadoEdificioReto.BLOQUEADO_HISTORIA -> {
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFE0E0E0)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("🔒 Bloqueado", color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                            }
                            EstadoEdificioReto.FUERA_DE_RANGO -> {
                                Button(
                                    onClick = onSimularAqui,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("⚡ Simular", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                            EstadoEdificioReto.SIN_RETO -> {}
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f), contentColor = Color.DarkGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

private fun crearPinDrawable(context: Context, color: Int, isSelected: Boolean, conEstrella: Boolean = false): Drawable {
    val density = context.resources.displayMetrics.density
    val width = if (isSelected) (36 * density).toInt() else (26 * density).toInt()
    val height = if (isSelected) (48 * density).toInt() else (36 * density).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = if (isSelected) 3f * density else 2f * density
    }

    val radius = width / 2f
    val path = Path().apply {
        arcTo(RectF(0f, 0f, width.toFloat(), width.toFloat()), 180f, 180f, false)
        lineTo(radius, height.toFloat())
        close()
    }

    canvas.drawPath(path, paint)
    canvas.drawPath(path, strokePaint)

    val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(radius, radius, radius * 0.42f, innerPaint)

    if (conEstrella) {
        // Estrella de 5 puntas en el centro del pin
        val estrella = Path()
        val exterior = radius * 0.36f
        val interior = exterior * 0.45f
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) exterior else interior
            val angulo = Math.toRadians(-90.0 + i * 36.0)
            val x = radius + (r * Math.cos(angulo)).toFloat()
            val y = radius + (r * Math.sin(angulo)).toFloat()
            if (i == 0) estrella.moveTo(x, y) else estrella.lineTo(x, y)
        }
        estrella.close()
        canvas.drawPath(estrella, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
    } else if (isSelected) {
        val centerDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(radius, radius, radius * 0.22f, centerDot)
    }

    return BitmapDrawable(context.resources, bitmap)
}
@Composable
private fun BannerObjetivo(capitulo: Capitulo?, onIr: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (capitulo == null) NachoYellow else NachoGreenDark)
            .clickable(enabled = capitulo != null, onClick = onIr)
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Nacho(
            modifier = Modifier.size(44.dp),
            expresion = if (capitulo == null) Expresion.FELIZ else Expresion.DECIDIDO
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (capitulo == null) {
            Column {
                Text("👑 ¡Historia completada!", fontWeight = FontWeight.ExtraBold, color = NachoGreenDark)
                Text("Sigue explorando el campus y mejorando tus estrellas.", fontSize = 12.sp, color = NachoGreenDark)
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "MISIÓN · CAPÍTULO ${capitulo.numero}",
                    color = NachoYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(capitulo.titulo, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("Ve a: ${capitulo.lugar}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
            }
            Box(
                modifier = Modifier
                    .pulso()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NachoYellow)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Ir ➜", color = NachoGreenDark, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
