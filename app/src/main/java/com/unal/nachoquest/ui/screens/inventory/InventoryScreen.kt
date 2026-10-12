package com.unal.nachoquest.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.domain.model.CatalogoItems
import com.unal.nachoquest.domain.model.CategoriaItem
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val tabs = listOf(
        "Objetos (${state.objetosDesbloqueadosCount}/${state.totalObjetos})",
        "Insignias (${state.insigniasDesbloqueadasCount}/${state.totalInsignias})",
        "Colección (${state.coleccionablesDesbloqueadosCount}/${state.totalColeccionables})"
    )

    val currentItems: List<ItemInventario> = when (state.selectedTab) {
        0 -> CatalogoItems.itemsObjetos
        1 -> CatalogoItems.itemsInsignias
        2 -> CatalogoItems.itemsColeccionables
        else -> emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Inventario de Misiones",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = NachoGreenDark
                )
                Text(
                    text = "Llaves, medallas y reliquias de la UNAL",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NachoGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Total: ${state.objetosDesbloqueadosCount + state.insigniasDesbloqueadasCount + state.coleccionablesDesbloqueadosCount}",
                    fontWeight = FontWeight.Bold,
                    color = NachoGreenDark,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs de Categorías
        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = Color.White,
            contentColor = NachoGreen,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                    color = NachoGreen
                )
            },
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.seleccionarTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (state.selectedTab == index) NachoGreen else Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grid dinámico de ítems
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(currentItems) { item ->
                val desbloqueado = viewModel.estaDesbloqueado(item)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewModel.mostrarDetalleItem(item) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (desbloqueado) Color.White else Color(0xFFF1F1F1)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (desbloqueado) 4.dp else 0.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (desbloqueado) NachoGreen.copy(alpha = 0.1f)
                                    else Color(0xFFE0E0E0)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (desbloqueado) {
                                Text(item.emojiIcono, fontSize = 32.sp)
                            } else {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Bloqueado",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.nombre,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (desbloqueado) Color.Black else Color.Gray,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (desbloqueado) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NachoGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Desbloqueado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NachoGreenDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "🔒 Bloqueado",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Pista al pie
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = NachoGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Toca cualquier ítem para ver su pista de obtención o su historia en la UNAL.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }

    // Modal de Detalle del Ítem
    state.itemSeleccionadoDetalle?.let { item ->
        val desbloqueado = viewModel.estaDesbloqueado(item)

        AlertDialog(
            onDismissRequest = { viewModel.mostrarDetalleItem(null) },
            confirmButton = {
                Button(
                    onClick = { viewModel.mostrarDetalleItem(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = NachoGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cerrar")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (desbloqueado) item.emojiIcono else "🔒", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(item.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column {
                    if (desbloqueado) {
                        Text(
                            text = item.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (item.nombreLugarOrigen.isNotBlank()) {
                            Text(
                                text = "📍 Obtenido en: ${item.nombreLugarOrigen}",
                                style = MaterialTheme.typography.bodySmall,
                                color = NachoGreenDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (item.nombrePuntoDesbloqueado != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "🔑 Llave de acceso para: ${item.nombrePuntoDesbloqueado}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB78103),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "Este ítem aún no ha sido descubierto en tu aventura universitaria.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NachoYellow.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "💡 Pista para desbloquear:",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF8D6E00)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.requisitoTexto,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}
