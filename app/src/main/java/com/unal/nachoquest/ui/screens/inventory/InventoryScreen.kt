package com.unal.nachoquest.ui.screens.inventory

import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.domain.model.CatalogoItems
import com.unal.nachoquest.domain.model.CategoriaItem
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.Etiqueta
import com.unal.nachoquest.ui.components.tarjetaJuego
import com.unal.nachoquest.ui.theme.Bruma
import com.unal.nachoquest.ui.theme.CantoCrema
import com.unal.nachoquest.ui.theme.EstiloEtiqueta
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import com.unal.nachoquest.ui.theme.Papel
import com.unal.nachoquest.ui.theme.Tinta
import com.unal.nachoquest.ui.theme.TintaSuave

private data class CategoriaInventario(
    val nombre: String,
    val emoji: String,
    val items: List<ItemInventario>,
    val conseguidos: Int
)

private val FondoBloqueado = Color(0xFFECE7D8)
private val CantoBloqueado = Color(0xFFD3CCB8)

/**
 * La mochila del jugador: objetos clave de la historia, insignias y coleccionables.
 * Los que faltan aparecen cubiertos por la bruma, con una pista para conseguirlos.
 */
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val categorias = listOf(
        CategoriaInventario("Objetos", "🗝️", CatalogoItems.itemsObjetos, state.objetosDesbloqueadosCount),
        CategoriaInventario("Insignias", "🎖️", CatalogoItems.itemsInsignias, state.insigniasDesbloqueadasCount),
        CategoriaInventario("Colección", "🎁", CatalogoItems.itemsColeccionables, state.coleccionablesDesbloqueadosCount)
    )
    val seleccionada = state.selectedTab.coerceIn(categorias.indices)
    val conseguidos = categorias.sumOf { it.conseguidos }
    val total = categorias.sumOf { it.items.size }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NachoCream)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(NachoGreenDark, NachoGreen)))
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Etiqueta("INVENTARIO", color = NachoYellow)
                Text("Tu mochila", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Llaves, medallas y reliquias que le has quitado al Olvido.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("$conseguidos/$total", style = MaterialTheme.typography.headlineSmall, color = NachoYellow)
                Text("conseguidos", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorias.forEachIndexed { indice, categoria ->
                PestanaCategoria(
                    categoria = categoria,
                    seleccionada = indice == seleccionada,
                    onClick = { viewModel.seleccionarTab(indice) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categorias[seleccionada].items, key = { it.id }) { item ->
                CeldaItem(
                    item = item,
                    desbloqueado = state.tiene(item),
                    onClick = { viewModel.mostrarDetalleItem(item) }
                )
            }
        }
    }

    state.itemSeleccionadoDetalle?.let { item ->
        DetalleItem(
            item = item,
            desbloqueado = state.tiene(item),
            onCerrar = { viewModel.mostrarDetalleItem(null) }
        )
    }
}

@Composable
private fun PestanaCategoria(
    categoria: CategoriaInventario,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .tarjetaJuego(
                fondo = if (seleccionada) NachoGreen else Papel,
                borde = if (seleccionada) NachoGreenDark else CantoCrema,
                radio = 14.dp,
                canto = 3.dp
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(categoria.emoji, fontSize = 18.sp)
        Text(
            categoria.nombre,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (seleccionada) Color.White else Tinta
        )
        Text(
            "${categoria.conseguidos}/${categoria.items.size}",
            style = EstiloEtiqueta,
            color = if (seleccionada) NachoYellow else TintaSuave
        )
    }
}

@Composable
private fun CeldaItem(item: ItemInventario, desbloqueado: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .tarjetaJuego(
                fondo = if (desbloqueado) Papel else FondoBloqueado,
                borde = if (desbloqueado) NachoYellow else CantoBloqueado
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    if (desbloqueado) Brush.radialGradient(listOf(NachoYellow.copy(alpha = 0.4f), NachoYellow.copy(alpha = 0.08f)))
                    else Brush.radialGradient(listOf(Bruma.copy(alpha = 0.35f), Bruma.copy(alpha = 0.12f)))
                ),
            contentAlignment = Alignment.Center
        ) {
            if (desbloqueado) {
                Text(item.emojiIcono, fontSize = 36.sp)
            } else {
                Text("?", style = MaterialTheme.typography.headlineLarge, color = Bruma)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Alto fijo para dos renglones, así todas las celdas miden lo mismo
        Box(modifier = Modifier.height(42.dp), contentAlignment = Alignment.Center) {
            // Los ítems que faltan no revelan su nombre: se descubren jugando
            Text(
                text = if (desbloqueado) item.nombre else "? ? ?",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (desbloqueado) Tinta else TintaSuave,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Etiqueta(
            texto = if (desbloqueado) "✓ CONSEGUIDO" else "VER PISTA",
            color = if (desbloqueado) NachoGreen else TintaSuave
        )
    }
}

@Composable
private fun DetalleItem(item: ItemInventario, desbloqueado: Boolean, onCerrar: () -> Unit) {
    Dialog(onDismissRequest = onCerrar) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NachoCream)
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(if (desbloqueado) NachoYellow.copy(alpha = 0.3f) else Bruma.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                if (desbloqueado) {
                    Text(item.emojiIcono, fontSize = 50.sp)
                } else {
                    Text("?", style = MaterialTheme.typography.headlineLarge, color = Bruma)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Etiqueta(
                texto = when (item.categoria) {
                    CategoriaItem.OBJETO -> "OBJETO CLAVE"
                    CategoriaItem.INSIGNIA -> "INSIGNIA"
                    CategoriaItem.COLECCIONABLE -> "COLECCIONABLE"
                },
                color = NachoGreen
            )
            Text(
                text = if (desbloqueado) item.nombre else "Aún por descubrir",
                style = MaterialTheme.typography.titleLarge,
                color = NachoGreenDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (desbloqueado) {
                Text(
                    text = item.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (item.nombreLugarOrigen.isNotBlank()) {
                    DatoItem(emoji = "📍", titulo = "Lo conseguiste en", valor = item.nombreLugarOrigen)
                }
                item.nombrePuntoDesbloqueado?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    DatoItem(emoji = "🔑", titulo = "Abre el paso a", valor = it)
                }
            } else {
                Text(
                    text = "La bruma todavía lo esconde en algún lugar del campus.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TintaSuave,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                DatoItem(emoji = "💡", titulo = "Pista", valor = item.requisitoTexto)
            }

            Spacer(modifier = Modifier.height(18.dp))
            BotonJuego(texto = "Cerrar", onClick = onCerrar, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun DatoItem(emoji: String, titulo: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NachoYellow.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(titulo, fontSize = 11.sp, color = TintaSuave)
            Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Tinta)
        }
    }
}
