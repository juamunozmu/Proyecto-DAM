package com.unal.nachoquest.ui.screens.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unal.nachoquest.domain.model.Expresion
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.ui.components.BotonJuego
import com.unal.nachoquest.ui.components.EscenaDialogo
import com.unal.nachoquest.ui.components.Etiqueta
import com.unal.nachoquest.ui.components.LluviaConfeti
import com.unal.nachoquest.ui.components.Efecto
import com.unal.nachoquest.ui.components.Nacho
import com.unal.nachoquest.ui.components.Sonidos
import com.unal.nachoquest.ui.components.Olvido
import com.unal.nachoquest.ui.components.sacudir
import com.unal.nachoquest.ui.components.tarjetaJuego
import com.unal.nachoquest.ui.screens.quiz.minijuegos.PantallaMinijuego
import com.unal.nachoquest.ui.theme.CantoCrema
import com.unal.nachoquest.ui.theme.EstiloEtiqueta
import com.unal.nachoquest.ui.theme.NachoCream
import com.unal.nachoquest.ui.theme.NachoGreen
import com.unal.nachoquest.ui.theme.NachoGreenDark
import com.unal.nachoquest.ui.theme.NachoNaranja
import com.unal.nachoquest.ui.theme.NachoYellow
import com.unal.nachoquest.ui.theme.Papel
import com.unal.nachoquest.ui.theme.Tinta
import kotlinx.coroutines.delay

private val Rojo = Color(0xFFD32F2F)

@Composable
fun QuizScreen(
    puntoId: String,
    onTerminar: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Evita cerrar dos veces (p. ej. "Saltar" + toque final), lo que sacaría también la pantalla principal
    var cerrado by remember { mutableStateOf(false) }
    val cerrar = {
        if (!cerrado) {
            cerrado = true
            onTerminar()
        }
    }

    LaunchedEffect(puntoId) {
        viewModel.cargarRetoParaPunto(puntoId)
    }

    BackHandler { cerrar() }

    if (state.reto == null) {
        Box(modifier = Modifier.fillMaxSize().background(NachoCream), contentAlignment = Alignment.Center) {
            Nacho(modifier = Modifier.size(96.dp))
        }
        return
    }

    AnimatedContent(
        targetState = state.fase,
        transitionSpec = { fadeIn(tween(400)).togetherWith(fadeOut(tween(300))) },
        label = "faseQuiz"
    ) { fase ->
        when (fase) {
            FaseQuiz.INTRO -> EscenaDialogo(lineas = state.escenaIntro, onTerminar = viewModel::terminarIntro)
            FaseQuiz.MINIJUEGO -> state.capitulo?.let { cap ->
                PantallaMinijuego(capitulo = cap, onCompletado = viewModel::completarMinijuego, onSalir = cerrar)
            }
            FaseQuiz.PREGUNTA -> PantallaPregunta(state, viewModel, onSalir = cerrar)
            FaseQuiz.RESULTADO -> PantallaResultados(
                state = state,
                onContinuar = { if (!viewModel.continuarTrasResultado()) cerrar() },
                onReintentar = viewModel::reintentar,
                onSalir = cerrar
            )
            FaseQuiz.OUTRO -> EscenaDialogo(
                lineas = state.escenaOutro,
                onTerminar = {
                    viewModel.terminarOutro()
                    cerrar()
                }
            )
        }
    }
}

@Composable
private fun PantallaPregunta(state: QuizUiState, viewModel: QuizViewModel, onSalir: () -> Unit) {
    val reto = state.reto ?: return
    val pregunta = state.preguntaActual ?: return
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(state.erroresAnimados) {
        if (state.erroresAnimados > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Sonidos.reproducir(Efecto.ERROR)
        }
    }
    LaunchedEffect(state.aciertosAnimados) {
        if (state.aciertosAnimados > 0) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            Sonidos.reproducir(Efecto.ACIERTO)
        }
    }

    val fraccionTiempo = state.tiempoRestanteMs.toFloat() / TIEMPO_POR_PREGUNTA_MS
    val tiempoCritico = !state.mostrarResultadoPregunta && fraccionTiempo < 0.25f

    val expresionNacho = when {
        state.mostrarResultadoPregunta && state.respuestaSeleccionada == pregunta.indiceCorrecta -> Expresion.FELIZ
        state.mostrarResultadoPregunta -> Expresion.PREOCUPADO
        tiempoCritico -> Expresion.SORPRENDIDO
        else -> Expresion.NORMAL
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F0E0), NachoCream)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Encabezado: salir, capítulo y corazones
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onSalir) {
                Icon(Icons.Default.Close, contentDescription = "Salir del reto", tint = Color.DarkGray)
            }
            Column(modifier = Modifier.weight(1f)) {
                Etiqueta(
                    texto = when {
                        state.esTriviaDiaria -> "PREGUNTA ${state.indicePreguntaActual + 1} DE ${state.totalPreguntas}"
                        state.capitulo != null -> "CAPÍTULO ${state.capitulo.numero} · EL GUARDIÁN PREGUNTA"
                        else -> "RETO DEL CAMPUS"
                    },
                    color = NachoGreen
                )
                Text(
                    text = state.capitulo?.titulo ?: reto.titulo,
                    style = MaterialTheme.typography.titleLarge,
                    color = NachoGreenDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Corazones(vidas = state.vidas, maximo = state.vidasMax)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progreso de preguntas
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(state.totalPreguntas) { i ->
                val color by animateColorAsState(
                    when {
                        i < state.indicePreguntaActual -> NachoGreen
                        i == state.indicePreguntaActual -> NachoYellow
                        else -> Color.LightGray.copy(alpha = 0.6f)
                    },
                    label = "segmento"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Temporizador
        BarraTiempo(fraccion = fraccionTiempo, pausado = state.mostrarResultadoPregunta)

        Spacer(modifier = Modifier.height(16.dp))

        // Nacho pregunta
        Row(verticalAlignment = Alignment.Bottom) {
            Nacho(
                modifier = Modifier.size(72.dp),
                expresion = expresionNacho,
                saltando = state.mostrarResultadoPregunta && expresionNacho == Expresion.FELIZ
            )
            Spacer(modifier = Modifier.width(8.dp))
            AnimatedContent(
                targetState = state.indicePreguntaActual,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                },
                modifier = Modifier.weight(1f),
                label = "pregunta"
            ) { indice ->
                val enunciado = reto.preguntas.getOrNull(indice)?.enunciado.orEmpty()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = enunciado,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonComodin(
                disponible = state.comodinDisponible,
                habilitado = !state.mostrarResultadoPregunta,
                onClick = viewModel::usarComodin
            )
            Spacer(modifier = Modifier.weight(1f))
            androidx.compose.animation.AnimatedVisibility(
                visible = state.racha >= 2,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "🔥 Racha x${state.racha}",
                    style = EstiloEtiqueta,
                    color = NachoNaranja,
                    fontSize = 19.sp
                )
            }
        }

        // Opciones
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.sacudir(state.erroresAnimados)
        ) {
            pregunta.opciones.forEachIndexed { index, textoOpcion ->
                OpcionPregunta(
                    letra = 'A' + index,
                    texto = textoOpcion,
                    estaSeleccionada = state.respuestaSeleccionada == index,
                    mostrarResultado = state.mostrarResultadoPregunta,
                    esCorrecta = index == pregunta.indiceCorrecta,
                    descartada = index in state.opcionesDescartadas,
                    onClick = { viewModel.responder(index) }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Retroalimentación tras responder
        AnimatedVisibility(
            visible = state.mostrarResultadoPregunta,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            val acerto = state.respuestaSeleccionada == pregunta.indiceCorrecta
            val celebracion = remember(state.indicePreguntaActual) {
                listOf("¡Correcto!", "¡Eso es!", "¡Qué nota!", "¡Exacto!").random()
            }
            val esUltima = state.sinVidas || state.indicePreguntaActual == state.totalPreguntas - 1
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (acerto) NachoGreen.copy(alpha = 0.12f) else Rojo.copy(alpha = 0.1f))
                    .padding(14.dp)
            ) {
                Text(
                    text = when {
                        acerto -> celebracion
                        state.seAcaboElTiempo -> "⏰ ¡Se acabó el tiempo!"
                        else -> "No era esa..."
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = if (acerto) NachoGreenDark else Rojo
                )
                if (!acerto) {
                    Text(
                        text = "La respuesta correcta era: ${pregunta.opciones[pregunta.indiceCorrecta]}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                BotonJuego(
                    texto = if (esUltima) "Ver resultado" else "Continuar",
                    onClick = viewModel::avanzarSiguientePregunta,
                    color = if (acerto) NachoGreen else Rojo,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun Corazones(vidas: Int, maximo: Int) {
    Row {
        repeat(maximo) { i ->
            val lleno = i < vidas
            val escala by animateFloatAsState(
                targetValue = if (lleno) 1f else 0.75f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
                label = "corazon"
            )
            Text(
                text = if (lleno) "❤️" else "🖤",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(horizontal = 1.dp)
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                        alpha = if (lleno) 1f else 0.45f
                    }
            )
        }
    }
}

@Composable
private fun BarraTiempo(fraccion: Float, pausado: Boolean) {
    val color by animateColorAsState(
        when {
            pausado -> Color.Gray
            fraccion > 0.5f -> NachoGreen
            fraccion > 0.25f -> NachoYellow
            else -> Rojo
        },
        label = "colorTiempo"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("⏱", fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.LightGray.copy(alpha = 0.4f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${(fraccion * TIEMPO_POR_PREGUNTA_MS / 1000).toInt()}s",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(28.dp)
        )
    }
}

/** Comodín 50:50: Nacho descarta dos opciones incorrectas. Se gasta al usarlo. */
@Composable
private fun BotonComodin(disponible: Boolean, habilitado: Boolean, onClick: () -> Unit) {
    val color = if (disponible) Color(0xFF8D6E00) else Color.Gray
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (disponible) NachoYellow.copy(alpha = 0.22f) else Color.LightGray.copy(alpha = 0.3f))
            .border(1.5.dp, if (disponible) NachoYellow else Color.LightGray, RoundedCornerShape(12.dp))
            .clickable(enabled = disponible && habilitado) {
                Sonidos.reproducir(Efecto.TOQUE)
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("💡", fontSize = 14.sp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(text = if (disponible) "COMODÍN 50:50" else "COMODÍN USADO", style = EstiloEtiqueta, color = color)
    }
}

@Composable
private fun OpcionPregunta(
    letra: Char,
    texto: String,
    estaSeleccionada: Boolean,
    mostrarResultado: Boolean,
    esCorrecta: Boolean,
    descartada: Boolean,
    onClick: () -> Unit
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionada by interaccion.collectIsPressedAsState()

    val resaltarCorrecta = mostrarResultado && esCorrecta
    val marcarError = mostrarResultado && estaSeleccionada && !esCorrecta
    // Una opción descartada por el comodín se atenúa hasta que se revela la respuesta
    val atenuada = descartada && !mostrarResultado

    val escala by animateFloatAsState(
        targetValue = when {
            presionada && !mostrarResultado -> 0.96f
            resaltarCorrecta -> 1.03f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaOpcion"
    )
    val opacidad by animateFloatAsState(if (atenuada) 0.35f else 1f, label = "opacidadOpcion")
    // Colores opacos: el canto de la tarjeta se dibuja detrás y no debe transparentarse
    val fondo by animateColorAsState(
        when {
            resaltarCorrecta -> Color(0xFFE1EDD6)
            marcarError -> Color(0xFFFBE3E1)
            mostrarResultado -> Color(0xFFF6F2E6)
            else -> Papel
        },
        label = "fondoOpcion"
    )
    val borde by animateColorAsState(
        when {
            resaltarCorrecta -> NachoGreen
            marcarError -> Rojo
            else -> CantoCrema
        },
        label = "bordeOpcion"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
                alpha = opacidad
            }
            .tarjetaJuego(fondo = fondo, borde = borde, radio = 14.dp, canto = 3.dp)
            .clickable(
                interactionSource = interaccion,
                indication = null,
                enabled = !mostrarResultado && !descartada,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    when {
                        resaltarCorrecta -> NachoGreen
                        marcarError -> Rojo
                        else -> NachoGreen.copy(alpha = 0.1f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letra.toString(),
                style = EstiloEtiqueta,
                fontSize = 20.sp,
                letterSpacing = 0.sp,
                color = if (resaltarCorrecta || marcarError) Color.White else NachoGreenDark
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            fontWeight = if (resaltarCorrecta) FontWeight.Bold else FontWeight.Normal,
            textDecoration = if (atenuada) TextDecoration.LineThrough else null,
            color = if (mostrarResultado && !resaltarCorrecta && !marcarError) Color.Gray else Tinta
        )
        if (resaltarCorrecta) {
            Icon(Icons.Default.CheckCircle, contentDescription = "Correcto", tint = NachoGreen)
        } else if (marcarError) {
            Icon(Icons.Default.Close, contentDescription = "Incorrecto", tint = Rojo)
        }
    }
}

@Composable
private fun PantallaResultados(
    state: QuizUiState,
    onContinuar: () -> Unit,
    onReintentar: () -> Unit,
    onSalir: () -> Unit
) {
    val aprobado = state.retoAprobado
    LaunchedEffect(Unit) {
        Sonidos.reproducir(if (aprobado) Efecto.VICTORIA else Efecto.DERROTA)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    if (aprobado) listOf(Color(0xFFDDEBC8), NachoCream)
                    else listOf(Color(0xFF55585F), Color(0xFF8A8D94))
                )
            )
    ) {
        if (aprobado) {
            LluviaConfeti(modifier = Modifier.fillMaxSize())
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center) {
                if (!aprobado) {
                    Olvido(modifier = Modifier.size(200.dp))
                }
                Nacho(
                    modifier = Modifier.size(if (aprobado) 140.dp else 110.dp),
                    expresion = if (aprobado) Expresion.FELIZ else Expresion.PREOCUPADO,
                    saltando = aprobado
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextoEntrada(
                texto = when {
                    aprobado && state.esTriviaDiaria && !state.esRepeticion -> "¡Trivia superada!"
                    aprobado && state.esRepeticion -> "¡Repaso superado!"
                    aprobado && state.capitulo?.numero == Historia.capitulos.size -> "¡Venciste al Olvido!"
                    aprobado -> "¡Fragmento recuperado!"
                    else -> "La bruma te alcanzó"
                },
                color = if (aprobado) NachoGreenDark else Color.White
            )

            Text(
                text = "Acertaste ${state.respuestasCorrectas} de ${state.totalPreguntas}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (aprobado) Color.DarkGray else Color.White.copy(alpha = 0.85f)
            )

            if (aprobado) {
                Spacer(modifier = Modifier.height(16.dp))
                EstrellasAnimadas(estrellas = state.estrellas)
                Spacer(modifier = Modifier.height(16.dp))
                ResumenPartida(state)
                RecompensasAnimadas(state)
                AvancesDelJugador(state)
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                state.fraseDerrota?.let {
                    Text(
                        text = "“${it.texto}”",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (aprobado) {
                BotonJuego(
                    texto = if (state.esTriviaDiaria) "Continuar" else "Continuar aventura",
                    onClick = onContinuar,
                    alto = 54.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                BotonJuego(
                    texto = "Reintentar",
                    onClick = onReintentar,
                    color = NachoYellow,
                    colorTexto = NachoGreenDark,
                    alto = 54.dp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                BotonJuego(
                    texto = "Salir",
                    onClick = onSalir,
                    color = Color(0xFF6B6E76),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TextoEntrada(texto: String, color: Color) {
    val escala = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        escala.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    Text(
        text = texto,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.graphicsLayer {
            scaleX = escala.value
            scaleY = escala.value
        }
    )
}

@Composable
private fun EstrellasAnimadas(estrellas: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
        repeat(3) { i ->
            val escala = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(350L + i * 280L)
                escala.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow))
            }
            val ganada = i < estrellas
            Text(
                text = "⭐",
                fontSize = if (i == 1) 52.sp else 40.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = escala.value
                    scaleY = escala.value
                    alpha = if (ganada) 1f else 0.2f
                    rotationZ = (1f - escala.value) * 90f
                }
            )
        }
    }
}

@Composable
private fun ResumenPartida(state: QuizUiState) {
    val xpMostrada = remember { Animatable(0f) }
    LaunchedEffect(state.xpGanada) {
        delay(900)
        xpMostrada.animateTo(state.xpGanada.toFloat(), tween(1100))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .tarjetaJuego(radio = 16.dp)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        DatoResumen(valor = "+${xpMostrada.value.toInt()}", etiqueta = "XP", emoji = "⚡")
        DatoResumen(valor = "x${state.mejorRacha}", etiqueta = "Mejor racha", emoji = "🔥")
        DatoResumen(valor = "${state.vidas}", etiqueta = "Corazones", emoji = "❤️")
    }
    if (state.esTriviaDiaria && state.rachaDias > 0) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (state.rachaDias == 1) "🔥 Racha: 1 día. ¡Vuelve mañana para alargarla!"
            else "🔥 Racha: ${state.rachaDias} días seguidos",
            fontWeight = FontWeight.ExtraBold,
            color = NachoNaranja,
            textAlign = TextAlign.Center
        )
    }
    if (state.esRepeticion) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (state.esTriviaDiaria) "Ya jugaste la trivia de hoy, así que esta vez fue solo práctica. Mañana hay preguntas nuevas."
            else "Modo repaso: ya tenías este fragmento. ¡Sirve para mejorar tus estrellas!",
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DatoResumen(valor: String, etiqueta: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(valor, style = MaterialTheme.typography.titleLarge, color = NachoGreenDark)
        Text(etiqueta, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun ColumnScope.RecompensasAnimadas(state: QuizUiState) {
    val recompensas = listOfNotNull(
        state.itemDesbloqueado?.let { Triple(it, "¡NUEVO ÍTEM CLAVE!", NachoGreen) },
        state.insigniaDesbloqueada?.let { Triple(it, "¡NUEVA INSIGNIA!", Color(0xFFB78103)) },
        state.coleccionableDesbloqueado?.let { Triple(it, "COLECCIONABLE", Color(0xFF512DA8)) }
    )
    recompensas.forEachIndexed { i, (item, etiqueta, color) ->
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(1500L + i * 350L)
            visible = true
        }
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it / 2 } + fadeIn() + scaleIn(initialScale = 0.8f)
        ) {
            TarjetaRecompensa(item, etiqueta, color)
        }
    }
}

/** Subida de nivel y logros desbloqueados con este reto. */
@Composable
private fun ColumnScope.AvancesDelJugador(state: QuizUiState) {
    val avances = buildList {
        state.nivelAlcanzado?.let { add(Triple("⬆️", "¡SUBISTE DE NIVEL!", "Ahora eres nivel $it")) }
        state.logrosNuevos.forEach { add(Triple(it.emoji, "LOGRO DESBLOQUEADO", it.titulo)) }
    }
    avances.forEachIndexed { i, (emoji, etiqueta, texto) ->
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(2300L + i * 350L)
            visible = true
            if (i == 0) Sonidos.reproducir(Efecto.LOGRO)
        }
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it / 2 } + fadeIn() + scaleIn(initialScale = 0.8f)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .tarjetaJuego(fondo = NachoGreenDark, borde = NachoYellow, radio = 16.dp)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 30.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Etiqueta(etiqueta, color = NachoYellow)
                    Text(texto, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TarjetaRecompensa(item: ItemInventario, etiqueta: String, color: Color) {
    Row(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            // Tonos opacos del color de la recompensa: el canto va detrás y no debe transparentarse
            .tarjetaJuego(
                fondo = color.copy(alpha = 0.12f).compositeOver(Papel),
                borde = color.copy(alpha = 0.5f).compositeOver(Papel),
                radio = 16.dp,
                canto = 3.dp
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(item.emojiIcono, fontSize = 32.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Etiqueta(etiqueta, color = color)
            Text(item.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            item.nombrePuntoDesbloqueado?.let {
                Text("🔓 Abre acceso a: $it", style = MaterialTheme.typography.bodySmall, color = NachoGreenDark)
            }
        }
    }
}
