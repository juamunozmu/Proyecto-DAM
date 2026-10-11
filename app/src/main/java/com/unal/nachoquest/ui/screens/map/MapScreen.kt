package com.unal.nachoquest.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.unal.nachoquest.domain.model.PointOfInterest
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
            Toast.makeText(context, "Se requiere GPS para ver tu ubicaciÃ³n", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        
        // Pedir permiso al abrir la pantalla
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
                    
                    // Ligeramente mÃ¡s flexible para poder alejar un poquito mÃ¡s
                    setScrollableAreaLimitDouble(
                        org.osmdroid.util.BoundingBox(4.650, -74.070, 4.620, -74.100)
                    )
                    minZoomLevel = 13.5
                    maxZoomLevel = 20.0
                    
                    // Capa de Mi UbicaciÃ³n
                    val myLocation = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        myLocation.enableMyLocation()
                    }
                    this.overlays.add(myLocation)
                    locationOverlay = myLocation
                }
            },
            update = { mapView ->
                // Conservar la capa de ubicaciÃ³n (que es MyLocationNewOverlay)
                mapView.overlays.removeAll { it is Marker || it is MapEventsOverlay || it is Polygon }
                
                val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        p?.let { Toast.makeText(context, "Lat: ${it.latitude}, Lon: ${it.longitude}", Toast.LENGTH_LONG).show() }
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
                
                val pinVerde = crearPinDrawable(context, android.graphics.Color.rgb(46, 125, 50), false)
                val pinRojo = crearPinDrawable(context, android.graphics.Color.rgb(220, 20, 60), true)

                var marcadorSeleccionado: Marker? = null

                state.puntosDeInteres.forEach { punto ->
                    val isSelected = state.puntoSeleccionado?.id == punto.id
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(punto.latitud, punto.longitud)
                        icon = if (isSelected) pinRojo else pinVerde
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = punto.nombre
                        setOnMarkerClickListener { _, _ ->
                            viewModel.seleccionarPunto(punto)
                            true
                        }
                    }
                    if (isSelected) {
                        marcadorSeleccionado = marker
                    } else {
                        clusterer.add(marker)
                    }
                }
                mapView.overlays.add(clusterer)

                // El seleccionado va encima y fuera del clusterer para destacar en rojo
                marcadorSeleccionado?.let { selectedMarker ->
                    mapView.overlays.add(selectedMarker)
                    selectedMarker.showInfoWindow()
                }

                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

                Column(modifier = Modifier.align(Alignment.TopCenter)) {
            TopStatusBar(
                nivel = state.nivelUsuario, xp = state.xpUsuario, estrellas = state.estrellasUsuario,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Barra de bÃºsqueda
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.actualizarBusqueda(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).background(Color.White, RoundedCornerShape(12.dp)),
                placeholder = { Text("Buscar edificio (ej: CYT, 401)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // Resultados de bÃºsqueda
            AnimatedVisibility(visible = state.resultadosBusqueda.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(state.resultadosBusqueda) { punto ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.actualizarBusqueda("") // Limpiar bÃºsqueda
                                        viewModel.seleccionarPunto(punto)
                                        mapViewReference?.controller?.animateTo(GeoPoint(punto.latitud, punto.longitud), 18.0, 1000)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = NachoGreen)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(punto.nombre, fontWeight = FontWeight.Bold)
                                    Text(punto.edificio, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // BotÃ³n flotante para centrar la cÃ¡mara en el GPS
        FloatingActionButton(
            onClick = {
                val myLoc = locationOverlay?.myLocation
                if (myLoc != null) {
                    mapViewReference?.controller?.animateTo(myLoc, 18.0, 1000)
                } else {
                    Toast.makeText(context, "Buscando ubicaciÃ³n...", Toast.LENGTH_SHORT).show()
                }
            },
            containerColor = Color.White,
            contentColor = NachoGreen,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (state.puntoSeleccionado != null) 240.dp else 16.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Centrar en mi")
        }

        AnimatedVisibility(
            visible = state.puntoSeleccionado != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            state.puntoSeleccionado?.let { punto ->
                PointOfInterestCard(
                    punto = punto,
                    onDismiss = { viewModel.cerrarDetalle() },
                    onIniciarReto = onIniciarReto
                )
            }
        }
    }
}

@Composable
private fun TopStatusBar(nivel: Int, xp: Int, estrellas: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.9f)).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(NachoGreen).padding(horizontal = 16.dp, vertical = 6.dp)) { Text(text = "Nivel ${nivel}", color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(NachoYellow), contentAlignment = Alignment.Center) { Text("âš¡", fontSize = 12.sp) }; Spacer(modifier = Modifier.width(4.dp)); Text(text = "${xp}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium) }
        Spacer(modifier = Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Star, contentDescription = "Estrellas", tint = NachoYellow, modifier = Modifier.size(24.dp)); Spacer(modifier = Modifier.width(4.dp)); Text(text = "${estrellas}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun PointOfInterestCard(punto: PointOfInterest, onDismiss: () -> Unit, onIniciarReto: (String) -> Unit = {}) {
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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            if (imageModel != null) {
                coil3.compose.AsyncImage(
                    model = imageModel,
                    contentDescription = punto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(NachoGreen.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.LocationOn, contentDescription = null, tint = NachoGreen, modifier = Modifier.size(28.dp)) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column { Text(text = punto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold); Text(text = "${punto.edificio} • ${punto.campus}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = punto.descripcion, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                if (punto.tieneRetoDisponible) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = NachoGreen.copy(alpha = 0.08f)), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = NachoGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column { Text("Reto disponible", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = NachoGreenDark); Text("Responde 5 preguntas sobre la historia del lugar", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (punto.tieneRetoDisponible) { Button(onClick = { onIniciarReto(punto.id) }, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = NachoGreen), shape = RoundedCornerShape(12.dp)) { Text("Comenzar reto", fontWeight = FontWeight.Bold) } }
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.3f), contentColor = Color.DarkGray), shape = RoundedCornerShape(12.dp)) { Text("Cerrar") }
                }
            }
        }
    }
}

private fun crearPinDrawable(context: android.content.Context, color: Int, isSelected: Boolean): Drawable {
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

    if (isSelected) {
        val centerDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(radius, radius, radius * 0.22f, centerDot)
    }

    return BitmapDrawable(context.resources, bitmap)
}