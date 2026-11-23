package com.unal.nachoquest.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.BancoRetos
import com.unal.nachoquest.domain.model.Capitulo
import com.unal.nachoquest.domain.model.CatalogoItems
import com.unal.nachoquest.domain.model.Historia
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.domain.model.LineaDialogo
import com.unal.nachoquest.domain.model.Logro
import com.unal.nachoquest.domain.model.Logros
import com.unal.nachoquest.domain.model.Pregunta
import com.unal.nachoquest.domain.model.Puntuacion
import com.unal.nachoquest.domain.model.RetoQuiz
import com.unal.nachoquest.domain.model.RetosLugares
import com.unal.nachoquest.domain.model.TriviaDiaria
import com.unal.nachoquest.domain.model.UserProfile
import com.unal.nachoquest.domain.model.primerNombre
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

enum class FaseQuiz { INTRO, MINIJUEGO, PREGUNTA, RESULTADO, OUTRO }

const val VIDAS_INICIALES = 3
// En los capítulos el minijuego es lo principal: después solo hay 3 preguntas y 2 corazones
private const val PREGUNTAS_CAPITULO = 3
private const val VIDAS_CAPITULO = 2
private const val OPCIONES_QUE_QUITA_EL_COMODIN = 2
const val TIEMPO_POR_PREGUNTA_MS = 20_000L
private const val TICK_MS = 100L
// Tiempo máximo que se espera al perfil antes de armar el reto (sin conexión puede no llegar)
private const val ESPERA_PERFIL_MS = 3_000L

data class QuizUiState(
    val fase: FaseQuiz = FaseQuiz.INTRO,
    val reto: RetoQuiz? = null,
    val capitulo: Capitulo? = null,
    val escenaIntro: List<LineaDialogo> = emptyList(),
    val escenaOutro: List<LineaDialogo> = emptyList(),
    val indicePreguntaActual: Int = 0,
    val respuestaSeleccionada: Int? = null,
    val mostrarResultadoPregunta: Boolean = false,
    val seAcaboElTiempo: Boolean = false,
    val respuestasCorrectas: Int = 0,
    val vidas: Int = VIDAS_INICIALES,
    val vidasMax: Int = VIDAS_INICIALES,
    val estrellasMinijuego: Int? = null,
    val racha: Int = 0,
    val mejorRacha: Int = 0,
    val tiempoRestanteMs: Long = TIEMPO_POR_PREGUNTA_MS,
    val bonoVelocidad: Int = 0,
    // Contadores que la UI usa para disparar animaciones (sacudida, vibración...)
    val erroresAnimados: Int = 0,
    val aciertosAnimados: Int = 0,
    // Resultado final
    val retoAprobado: Boolean = false,
    val esRepeticion: Boolean = false,
    val estrellas: Int = 0,
    val xpGanada: Int = 0,
    val itemDesbloqueado: ItemInventario? = null,
    val insigniaDesbloqueada: ItemInventario? = null,
    val coleccionableDesbloqueado: ItemInventario? = null,
    val fraseDerrota: LineaDialogo? = null,
    // Trivia del día: racha de días con la que queda el jugador al superarla
    val esTriviaDiaria: Boolean = false,
    val rachaDias: Int = 0,
    // Comodín 50:50: una vez por reto, Nacho descarta dos opciones incorrectas
    val comodinDisponible: Boolean = true,
    val opcionesDescartadas: Set<Int> = emptySet(),
    // Lo que el reto le dejó al jugador además de la XP
    val nivelAlcanzado: Int? = null,
    val logrosNuevos: List<Logro> = emptyList()
) {
    val preguntaActual: Pregunta?
        get() = reto?.preguntas?.getOrNull(indicePreguntaActual)

    val totalPreguntas: Int
        get() = reto?.preguntas?.size ?: 0

    val sinVidas: Boolean
        get() = vidas <= 0
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var temporizador: Job? = null
    private var puntoIdCargado: String? = null
    private var diaTrivia: Long = 0

    fun cargarRetoParaPunto(puntoId: String) {
        if (puntoIdCargado == puntoId) return
        puntoIdCargado = puntoId

        viewModelScope.launch {
            // El perfil dice si es una repetición y qué escenas ya se vieron
            withTimeoutOrNull(ESPERA_PERFIL_MS) { userRepository.perfilCargado.first { it } }
            prepararReto(puntoId, userRepository.userProfileFlow.value)
        }
    }

    private fun prepararReto(puntoId: String, perfil: UserProfile) {
        if (puntoId == TriviaDiaria.PUNTO_ID) {
            prepararTriviaDiaria(perfil)
            return
        }
        val nombre = perfil.primerNombre()
        val retoBase = BancoRetos.retoParaPunto(puntoId)
        // Se mezclan las opciones para que la respuesta correcta no esté siempre en el mismo lugar
        val capitulo = Historia.capituloPorReto(retoBase.id)
        // Capítulos y retos de lugar sortean unas pocas preguntas de su banco en cada partida
        val sorteaPreguntas = capitulo != null || RetosLugares.tieneReto(puntoId)
        val preguntas = if (sorteaPreguntas) retoBase.preguntas.shuffled().take(PREGUNTAS_CAPITULO) else retoBase.preguntas
        val reto = retoBase.copy(preguntas = preguntas.map { it.conOpcionesMezcladas() })
        val vidas = if (sorteaPreguntas) VIDAS_CAPITULO else VIDAS_INICIALES

        // La intro solo se muestra la primera vez; al repetir se va directo a las preguntas
        val intro = capitulo
            ?.takeIf { it.escenaIntro !in perfil.escenasVistas }
            ?.let { Historia.escena(it.escenaIntro, nombre) }
            .orEmpty()

        val outro = capitulo?.let {
            val lineas = Historia.escena(it.escenaOutro, nombre)
            if (it.numero == Historia.capitulos.size) lineas + Historia.escena(Historia.ESCENA_EPILOGO, nombre) else lineas
        }.orEmpty()

        val fase = when {
            intro.isNotEmpty() -> FaseQuiz.INTRO
            capitulo != null -> FaseQuiz.MINIJUEGO
            else -> FaseQuiz.PREGUNTA
        }
        _uiState.value = QuizUiState(
            fase = fase,
            reto = reto,
            capitulo = capitulo,
            escenaIntro = intro,
            escenaOutro = outro,
            vidas = vidas,
            vidasMax = vidas,
            esRepeticion = reto.id in perfil.retosCompletados
        )
        if (fase == FaseQuiz.PREGUNTA) iniciarTemporizador()
    }

    private fun prepararTriviaDiaria(perfil: UserProfile) {
        val hoy = TriviaDiaria.diaActual()
        val yaJugada = TriviaDiaria.yaJugoHoy(perfil, hoy)
        // El bono de XP sale de la racha con la que se quedará al superarla
        val reto = TriviaDiaria.retoDelDia(hoy, TriviaDiaria.rachaTrasJugar(perfil, hoy))
        diaTrivia = hoy
        _uiState.value = QuizUiState(
            fase = FaseQuiz.PREGUNTA,
            reto = reto.copy(preguntas = reto.preguntas.map { it.conOpcionesMezcladas() }),
            esRepeticion = yaJugada,
            esTriviaDiaria = true,
            rachaDias = TriviaDiaria.rachaVigente(perfil, hoy)
        )
        iniciarTemporizador()
    }

    fun terminarIntro() {
        val state = _uiState.value
        if (state.fase != FaseQuiz.INTRO) return
        state.capitulo?.let { cap -> userRepository.marcarEscenaVista(cap.escenaIntro) }
        if (state.capitulo != null) {
            _uiState.update { it.copy(fase = FaseQuiz.MINIJUEGO) }
        } else {
            _uiState.update { it.copy(fase = FaseQuiz.PREGUNTA) }
            iniciarTemporizador()
        }
    }

    fun completarMinijuego(estrellas: Int) {
        if (_uiState.value.fase != FaseQuiz.MINIJUEGO) return
        _uiState.update { it.copy(fase = FaseQuiz.PREGUNTA, estrellasMinijuego = estrellas) }
        iniciarTemporizador()
    }

    /** Descarta dos opciones incorrectas de la pregunta actual. Solo se puede usar una vez por reto. */
    fun usarComodin() {
        val state = _uiState.value
        val pregunta = state.preguntaActual ?: return
        if (state.fase != FaseQuiz.PREGUNTA || state.mostrarResultadoPregunta || !state.comodinDisponible) return
        val descartadas = pregunta.opciones.indices
            .filter { it != pregunta.indiceCorrecta }
            .shuffled()
            .take(OPCIONES_QUE_QUITA_EL_COMODIN)
            .toSet()
        _uiState.update { it.copy(comodinDisponible = false, opcionesDescartadas = descartadas) }
    }

    /** Responder es inmediato: tocar una opción la confirma. */
    fun responder(indiceRespuesta: Int) {
        val state = _uiState.value
        if (state.fase != FaseQuiz.PREGUNTA || state.mostrarResultadoPregunta) return
        if (indiceRespuesta in state.opcionesDescartadas) return
        temporizador?.cancel()

        val esCorrecta = indiceRespuesta == state.preguntaActual?.indiceCorrecta
        _uiState.update {
            if (esCorrecta) {
                val racha = it.racha + 1
                it.copy(
                    respuestaSeleccionada = indiceRespuesta,
                    mostrarResultadoPregunta = true,
                    respuestasCorrectas = it.respuestasCorrectas + 1,
                    racha = racha,
                    mejorRacha = maxOf(it.mejorRacha, racha),
                    // Medio punto de XP por cada segundo que sobró
                    bonoVelocidad = it.bonoVelocidad + (it.tiempoRestanteMs / 2000L).toInt(),
                    aciertosAnimados = it.aciertosAnimados + 1
                )
            } else {
                it.copy(
                    respuestaSeleccionada = indiceRespuesta,
                    mostrarResultadoPregunta = true,
                    vidas = it.vidas - 1,
                    racha = 0,
                    erroresAnimados = it.erroresAnimados + 1
                )
            }
        }
    }

    fun avanzarSiguientePregunta() {
        val state = _uiState.value
        val reto = state.reto ?: return
        if (!state.mostrarResultadoPregunta) return

        if (!state.sinVidas && state.indicePreguntaActual < reto.preguntas.size - 1) {
            _uiState.update {
                it.copy(
                    indicePreguntaActual = it.indicePreguntaActual + 1,
                    respuestaSeleccionada = null,
                    mostrarResultadoPregunta = false,
                    opcionesDescartadas = emptySet(),
                    seAcaboElTiempo = false,
                    tiempoRestanteMs = TIEMPO_POR_PREGUNTA_MS
                )
            }
            iniciarTemporizador()
        } else {
            finalizarReto()
        }
    }

    fun reintentar() {
        val reto = _uiState.value.reto ?: return
        temporizador?.cancel()
        _uiState.update {
            QuizUiState(
                fase = FaseQuiz.PREGUNTA,
                reto = reto.copy(preguntas = reto.preguntas.map { p -> p.conOpcionesMezcladas() }),
                capitulo = it.capitulo,
                escenaOutro = it.escenaOutro,
                vidas = it.vidasMax,
                vidasMax = it.vidasMax,
                // El minijuego ya está superado: al reintentar solo se repiten las preguntas
                estrellasMinijuego = it.estrellasMinijuego,
                esRepeticion = it.esRepeticion,
                esTriviaDiaria = it.esTriviaDiaria,
                rachaDias = it.rachaDias
            )
        }
        iniciarTemporizador()
    }

    /**
     * Pasa a la escena de cierre si es la primera vez que se aprueba un capítulo.
     * Devuelve false si no hay escena y la pantalla debe cerrarse directamente.
     */
    fun continuarTrasResultado(): Boolean {
        val state = _uiState.value
        val mostrarOutro = state.retoAprobado && !state.esRepeticion && state.escenaOutro.isNotEmpty()
        if (mostrarOutro) _uiState.update { it.copy(fase = FaseQuiz.OUTRO) }
        return mostrarOutro
    }

    fun terminarOutro() {
        val cap = _uiState.value.capitulo ?: return
        userRepository.marcarEscenaVista(cap.escenaOutro)
        if (cap.numero == Historia.capitulos.size) userRepository.marcarEscenaVista(Historia.ESCENA_EPILOGO)
    }

    private fun iniciarTemporizador() {
        temporizador?.cancel()
        temporizador = viewModelScope.launch {
            while (_uiState.value.tiempoRestanteMs > 0) {
                delay(TICK_MS)
                _uiState.update { it.copy(tiempoRestanteMs = (it.tiempoRestanteMs - TICK_MS).coerceAtLeast(0)) }
            }
            // Se acabó el tiempo: cuenta como respuesta incorrecta
            _uiState.update {
                if (it.mostrarResultadoPregunta) it
                else it.copy(
                    mostrarResultadoPregunta = true,
                    seAcaboElTiempo = true,
                    vidas = it.vidas - 1,
                    racha = 0,
                    erroresAnimados = it.erroresAnimados + 1
                )
            }
        }
    }

    private fun finalizarReto() {
        temporizador?.cancel()
        val state = _uiState.value
        val reto = state.reto ?: return

        val resultado = Puntuacion.calcular(
            totalPreguntas = state.totalPreguntas,
            respuestasCorrectas = state.respuestasCorrectas,
            vidasRestantes = state.vidas,
            estrellasMinijuego = state.estrellasMinijuego,
            bonoVelocidad = state.bonoVelocidad,
            recompensaXp = reto.recompensaXp,
            esRepeticion = state.esRepeticion
        )
        val aprobado = resultado.aprobado

        val primeraVez = aprobado && !state.esRepeticion
        val item = reto.itemRecompensaId?.takeIf { primeraVez }?.let { CatalogoItems.obtenerPorId(it) }
        val insignia = reto.insigniaRecompensaId?.takeIf { primeraVez }?.let { CatalogoItems.obtenerPorId(it) }
        val coleccionable = reto.coleccionableRecompensaId?.takeIf { primeraVez }?.let { CatalogoItems.obtenerPorId(it) }

        _uiState.update {
            it.copy(
                fase = FaseQuiz.RESULTADO,
                retoAprobado = aprobado,
                estrellas = resultado.estrellas,
                xpGanada = resultado.xp,
                itemDesbloqueado = item,
                insigniaDesbloqueada = insignia,
                coleccionableDesbloqueado = coleccionable,
                fraseDerrota = if (aprobado) null else Historia.frasesDerrota.random()
            )
        }

        if (!aprobado) return
        val perfilAntes = userRepository.userProfileFlow.value
        if (state.esTriviaDiaria) {
            val racha = userRepository.registrarTriviaDiaria(
                hoy = diaTrivia,
                xpGanada = resultado.xp,
                perfecta = state.respuestasCorrectas == state.totalPreguntas
            )
            _uiState.update { it.copy(rachaDias = racha) }
        } else {
            userRepository.registrarRetoAprobado(
                retoId = reto.id,
                estrellas = resultado.estrellas,
                xpGanada = resultado.xp,
                itemsGanados = listOfNotNull(item?.id, coleccionable?.id),
                insigniasGanadas = listOfNotNull(insignia?.id)
            )
        }
        // El repositorio actualiza el perfil al instante, así que ya se puede comparar
        val perfilDespues = userRepository.userProfileFlow.value
        _uiState.update {
            it.copy(
                nivelAlcanzado = perfilDespues.nivel.takeIf { nivel -> nivel > perfilAntes.nivel },
                logrosNuevos = Logros.nuevos(perfilAntes, perfilDespues)
            )
        }
    }
}

private fun Pregunta.conOpcionesMezcladas(): Pregunta {
    val correcta = opciones[indiceCorrecta]
    val mezcladas = opciones.shuffled()
    return copy(opciones = mezcladas, indiceCorrecta = mezcladas.indexOf(correcta))
}
