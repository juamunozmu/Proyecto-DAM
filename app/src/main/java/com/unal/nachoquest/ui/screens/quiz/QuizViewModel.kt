package com.unal.nachoquest.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.unal.nachoquest.domain.model.Pregunta
import com.unal.nachoquest.domain.model.RetoQuiz

data class QuizUiState(
    val reto: RetoQuiz? = null,
    val indicePreguntaActual: Int = 0,
    val respuestaSeleccionada: Int? = null,
    val mostrarResultadoPregunta: Boolean = false,
    val respuestasCorrectas: Int = 0,
    val quizTerminado: Boolean = false,
    val xpGanada: Int = 0
) {
    val preguntaActual: Pregunta?
        get() = reto?.preguntas?.getOrNull(indicePreguntaActual)
}

@HiltViewModel
class QuizViewModel @Inject constructor(private val userRepository: com.unal.nachoquest.domain.repository.UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // Base de datos de prueba de preguntas para la Biblioteca Central
    private val retoMock = RetoQuiz(
        id = "reto_biblio",
        puntoInteresId = "poi_1",
        titulo = "Trivias de la Biblioteca",
        descripcion = "Demuestra qué tanto sabes sobre la Biblioteca Central.",
        preguntas = listOf(
            Pregunta("q1", "¿En qué año se inauguró el edificio de la Biblioteca Central?", listOf("1970", "1973", "1968", "1985"), 1),
            Pregunta("q2", "¿Quién fue el arquitecto principal?", listOf("Rogelio Salmona", "Le Corbusier", "Fernando Martínez", "Karl Brunner"), 0),
            Pregunta("q3", "¿Cuál es el apodo popular de este edificio?", listOf("El Búho", "El Bloque", "El Caracol", "La Torre"), 0),
            Pregunta("q4", "¿De qué material principal está construida la fachada?", listOf("Ladrillo a la vista", "Concreto", "Vidrio", "Piedra"), 0),
            Pregunta("q5", "¿Cuántos pisos tiene la zona principal de lectura?", listOf("2", "3", "4", "5"), 2)
        )
    )

    fun cargarRetoParaPunto(puntoId: String) {
        // En Parcial 3 esto se leerá de Firebase o Room
        _uiState.update { 
            QuizUiState(reto = retoMock)
        }
    }

    fun seleccionarRespuesta(indiceRespuesta: Int) {
        if (_uiState.value.mostrarResultadoPregunta || _uiState.value.quizTerminado) return

        _uiState.update { it.copy(respuestaSeleccionada = indiceRespuesta) }
    }

    fun confirmarRespuesta() {
        val state = _uiState.value
        if (state.respuestaSeleccionada == null || state.mostrarResultadoPregunta) return

        val esCorrecta = state.respuestaSeleccionada == state.preguntaActual?.indiceCorrecta
        
        _uiState.update { 
            it.copy(
                mostrarResultadoPregunta = true,
                respuestasCorrectas = if (esCorrecta) it.respuestasCorrectas + 1 else it.respuestasCorrectas
            )
        }
    }

    fun avanzarSiguientePregunta() {
        val state = _uiState.value
        val reto = state.reto ?: return

        if (state.indicePreguntaActual < reto.preguntas.size - 1) {
            _uiState.update { 
                it.copy(
                    indicePreguntaActual = it.indicePreguntaActual + 1,
                    respuestaSeleccionada = null,
                    mostrarResultadoPregunta = false
                )
            }
                } else {
            // Terminar quiz y calcular recompensas
            val xpFinal = (state.respuestasCorrectas.toFloat() / reto.preguntas.size * reto.recompensaXp).toInt()
            _uiState.update {
                it.copy(
                    quizTerminado = true,
                    xpGanada = xpFinal
                )
            }
            
            // Guardar en Firebase
            viewModelScope.launch {
                userRepository.updateXP(xpGanada = xpFinal, retoId = reto.id)
            }
        }
    }
}