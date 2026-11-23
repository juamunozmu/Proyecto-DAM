package com.unal.nachoquest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unal.nachoquest.domain.model.Capitulo
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.UserProfile
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.screens.quiz.minijuegos.PantallaMinijuego
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoQuestTheme

/**
 * Galería de minijuegos (solo en debug): abre el minijuego de cualquier capítulo sin jugar la
 * historia ni tocar Firebase. Las pistas son gratis. Se abre con:
 *
 *     adb shell am start -n com.unal.nachoquest/.GaleriaMinijuegosActivity
 *
 * o con el extra `capitulo` para ir directo a uno: `--ei capitulo 8`.
 */
class GaleriaMinijuegosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inicial = Historia.capitulos.find { it.numero == intent.getIntExtra("capitulo", 0) }
        setContent {
            NachoQuestTheme {
                var capitulo by remember { mutableStateOf<Capitulo?>(inicial) }
                // Cambia en cada partida para que el minijuego empiece de cero
                var partida by remember { mutableIntStateOf(0) }
                val actual = capitulo
                if (actual == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NachoCream)
                            .statusBarsPadding()
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
            }
        }
    }
}
