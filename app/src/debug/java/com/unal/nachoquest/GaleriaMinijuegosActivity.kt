package com.unal.nachoquest

import android.os.Bundle
import android.widget.Toast
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.domain.model.Capitulo
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.UserProfile
import com.unal.nachoquest.ui.components.BarraDeEstado
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.EscenaDialogo
import com.unal.nachoquest.ui.screens.quiz.minijuegos.PantallaMinijuego
import com.unal.nachoquest.ui.theme.CantoCrema
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoQuestTheme

/**
 * Galería de minijuegos (solo en debug): abre el minijuego de cualquier capítulo sin jugar la
 * historia ni tocar Firebase. Las pistas son gratis. Se abre con:
 *
 *     adb shell am start -n com.unal.nachoquest/.GaleriaMinijuegosActivity
 *
 * o con el extra `capitulo` para ir directo a uno: `--ei capitulo 8`. También muestra las escenas
 * de la historia (con «Ana» como jugador), para revisar los diálogos: `--es escena cap9_intro`.
 */
class GaleriaMinijuegosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Igual que MainActivity: borde a borde, con la franja verde detrás de la barra de estado
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        val inicial = Historia.capitulos.find { it.numero == intent.getIntExtra("capitulo", 0) }
        val escenaInicial = intent.getStringExtra("escena")?.takeIf { it in Historia.escenas }
        setContent {
            NachoQuestTheme {
                var capitulo by remember { mutableStateOf<Capitulo?>(inicial) }
                var escena by remember { mutableStateOf(escenaInicial) }
                // Cambia en cada partida para que el minijuego empiece de cero
                var partida by remember { mutableIntStateOf(0) }
                val actual = capitulo
                val escenaActual = escena
                if (escenaActual != null) {
                    BackHandler { escena = null }
                    androidx.compose.runtime.key(escenaActual) {
                        EscenaDialogo(lineas = Historia.escena(escenaActual, "Ana"), onTerminar = { escena = null })
                    }
                } else if (actual == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NachoCream)
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Galería de minijuegos (debug)", style = MaterialTheme.typography.headlineSmall)
                        Historia.capitulos.forEach { cap ->
                            BotonJuego(
                                texto = "${cap.numero}. ${cap.minijuego.titulo}",
                                onClick = {
                                    partida++
                                    capitulo = cap
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        // Escenas de la historia, para revisar los diálogos sin jugar los capítulos
                        Text("Escenas", style = MaterialTheme.typography.headlineSmall)
                        Historia.escenas.keys.forEach { id ->
                            BotonJuego(
                                texto = id,
                                onClick = { escena = id },
                                color = NachoCream,
                                colorTexto = NachoGreenDark,
                                borde = CantoCrema,
                                alto = 40.dp,
                                tamanoTexto = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    BackHandler { capitulo = null }
                    androidx.compose.runtime.key(partida) {
                        PantallaMinijuego(
                            capitulo = actual,
                            perfil = UserProfile(monedas = 999),
                            onPedirPista = { true },
                            onCompletado = { estrellas ->
                                Toast.makeText(this, "Terminado con $estrellas estrellas", Toast.LENGTH_SHORT).show()
                                capitulo = null
                            },
                            onSalir = { capitulo = null }
                        )
                    }
                }
                BarraDeEstado()
            }
        }
    }
}
