package com.unal.nachoquest.ui.screens.shop

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.domain.model.AspectoNacho
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.domain.model.RetosLugares
import com.unal.nachoquest.domain.model.Tienda
import com.unal.nachoquest.domain.model.TipoAyuda
import com.unal.nachoquest.domain.model.UserProfile
import com.unal.nachoquest.ui.components.AvisoSinMonedas
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.ColorMoneda
import com.unal.nachoquest.ui.components.Etiqueta
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.components.SaldoMonedas
import com.unal.nachoquest.ui.components.tarjetaJuego
import com.unal.nachoquest.ui.theme.CantoCrema
import com.unal.nachoquest.ui.theme.NachoAzul
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoYellow
import com.unal.nachoquest.ui.theme.Papel
import com.unal.nachoquest.ui.theme.Tinta

@Composable
fun TiendaScreen(
    onVolver: () -> Unit,
    viewModel: TiendaViewModel = hiltViewModel()
) {
    val perfil by viewModel.perfil.collectAsState()
    val sinSaldo by viewModel.sinSaldo.collectAsState()
    AvisoSinMonedas(sinSaldo)
    BackHandler(onBack = onVolver)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NachoCream)
            .verticalScroll(rememberScrollState())
    ) {
        Cabecera(perfil, onVolver)

        Column(modifier = Modifier.padding(16.dp)) {
            Seccion("AYUDAS", "Se guardan y se usan en cualquier reto. Si no tienes, también se pueden pagar en el momento.")
            TipoAyuda.entries.forEach { tipo ->
                FilaAyuda(
                    tipo = tipo,
                    reserva = Tienda.reserva(perfil, tipo),
                    alcanza = perfil.monedas >= tipo.precio,
                    onComprar = { viewModel.comprarAyuda(tipo) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Seccion("ASPECTOS DE NACHO", "Accesorios que Nacho lleva en toda la app. Solo uno por parte del cuerpo.")
            Tienda.aspectos.chunked(2).forEach { par ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 5.dp)) {
                    par.forEach { aspecto ->
                        TarjetaAspecto(
                            aspecto = aspecto,
                            comprado = aspecto.id in perfil.aspectos,
                            equipado = aspecto.id in perfil.aspectosEquipados,
                            alcanza = perfil.monedas >= aspecto.precio,
                            onClick = { viewModel.tocarAspecto(aspecto) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (par.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            ComoGanarMonedas()
            Spacer(modifier = Modifier.navigationBarsPadding().height(12.dp))
        }
    }
}

@Composable
private fun Cabecera(perfil: UserProfile, onVolver: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(NachoGreenDark, NachoGreen)))
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        IconButton(onClick = onVolver, modifier = Modifier.align(Alignment.TopStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("Tienda de Nacho", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Nacho(modifier = Modifier.size(76.dp), expresion = Expresion.FELIZ)
            }
            Spacer(modifier = Modifier.height(8.dp))
            SaldoMonedas(perfil.monedas, claro = true)
        }
    }
}

@Composable
private fun Seccion(titulo: String, descripcion: String) {
    Etiqueta(titulo, color = NachoGreen)
    Text(descripcion, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun FilaAyuda(tipo: TipoAyuda, reserva: Int, alcanza: Boolean, onComprar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .tarjetaJuego(radio = 14.dp, canto = 3.dp)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(tipo.emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(tipo.nombre, fontWeight = FontWeight.Bold, color = Tinta)
            Text(tipo.descripcion, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            Text(
                if (reserva > 0) "Tienes $reserva" else "No tienes",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (reserva > 0) NachoGreenDark else Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        BotonJuego(
            texto = "🪙 ${tipo.precio}",
            onClick = onComprar,
            color = if (alcanza) NachoYellow else Color(0xFFE0E0E0),
            colorTexto = if (alcanza) NachoGreenDark else Color.Gray,
            alto = 40.dp,
            tamanoTexto = 14.sp
        )
    }
}

@Composable
private fun TarjetaAspecto(
    aspecto: AspectoNacho,
    comprado: Boolean,
    equipado: Boolean,
    alcanza: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .tarjetaJuego(
                fondo = Papel,
                borde = if (equipado) NachoYellow else CantoCrema,
                radio = 16.dp
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Vista previa: Nacho solo con este accesorio
        Nacho(modifier = Modifier.size(64.dp), aspectos = listOf(aspecto.id))
        Text(
            "${aspecto.emoji} ${aspecto.nombre}",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Tinta,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(6.dp))
        BotonJuego(
            texto = when {
                equipado -> "Quitar"
                comprado -> "Ponérselo"
                else -> "🪙 ${aspecto.precio}"
            },
            onClick = onClick,
            color = when {
                equipado -> Color.White
                comprado -> NachoAzul
                alcanza -> NachoYellow
                else -> Color(0xFFE0E0E0)
            },
            colorTexto = when {
                equipado -> NachoGreenDark
                comprado -> Color.White
                alcanza -> NachoGreenDark
                else -> Color.Gray
            },
            borde = if (equipado) CantoCrema else null,
            alto = 38.dp,
            tamanoTexto = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ComoGanarMonedas() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .tarjetaJuego(fondo = Color(0xFFFFF4D6), borde = NachoYellow, radio = 14.dp, canto = 3.dp)
            .padding(14.dp)
    ) {
        Text("¿Cómo gano monedas?", fontWeight = FontWeight.Bold, color = ColorMoneda)
        Spacer(modifier = Modifier.height(4.dp))
        listOf(
            "📖 Capítulos de la historia: de 20 a 50 la primera vez",
            "❓ Retos de lugar: ${RetosLugares.MONEDAS_RECOMPENSA} cada uno (hay ${RetosLugares.retos.size})",
            "📅 Trivia del día: ${Tienda.MONEDAS_TRIVIA} cada día que la superes"
        ).forEach {
            Text(it, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
        }
    }
}
