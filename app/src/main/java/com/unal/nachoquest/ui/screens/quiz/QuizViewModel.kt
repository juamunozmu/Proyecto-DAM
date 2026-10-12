package com.unal.nachoquest.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unal.nachoquest.domain.model.CatalogoItems
import com.unal.nachoquest.domain.model.ItemInventario
import com.unal.nachoquest.domain.model.Pregunta
import com.unal.nachoquest.domain.model.RetoQuiz
import com.unal.nachoquest.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val reto: RetoQuiz? = null,
    val indicePreguntaActual: Int = 0,
    val respuestaSeleccionada: Int? = null,
    val mostrarResultadoPregunta: Boolean = false,
    val respuestasCorrectas: Int = 0,
    val quizTerminado: Boolean = false,
    val xpGanada: Int = 0,
    val itemDesbloqueado: ItemInventario? = null,
    val insigniaDesbloqueada: ItemInventario? = null,
    val coleccionableDesbloqueado: ItemInventario? = null,
    val retoAprobado: Boolean = false
) {
    val preguntaActual: Pregunta?
        get() = reto?.preguntas?.getOrNull(indicePreguntaActual)
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    fun cargarRetoParaPunto(puntoId: String) {
        val reto = obtenerRetoParaPunto(puntoId)
        _uiState.value = QuizUiState(reto = reto)
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
            // El reto se aprueba si se acierta al menos el 60% (3 de 5)
            val aprobado = state.respuestasCorrectas >= (reto.preguntas.size * 0.6).toInt()
            val xpFinal = if (aprobado) {
                reto.recompensaXp
            } else {
                (reto.recompensaXp * 0.3).toInt() // Puntos de consolación si no aprueba
            }

            val itemObj = if (aprobado && reto.itemRecompensaId != null) {
                CatalogoItems.obtenerPorId(reto.itemRecompensaId)
            } else null

            val insigniaObj = if (aprobado && reto.insigniaRecompensaId != null) {
                CatalogoItems.obtenerPorId(reto.insigniaRecompensaId)
            } else null

            val colObj = if (aprobado && reto.coleccionableRecompensaId != null) {
                CatalogoItems.obtenerPorId(reto.coleccionableRecompensaId)
            } else null

            _uiState.update {
                it.copy(
                    quizTerminado = true,
                    xpGanada = xpFinal,
                    retoAprobado = aprobado,
                    itemDesbloqueado = itemObj,
                    insigniaDesbloqueada = insigniaObj,
                    coleccionableDesbloqueado = colObj
                )
            }

            // Registrar en el repositorio si aprobó
            viewModelScope.launch {
                val itemsGanados = mutableListOf<String>()
                val insigniasGanadas = mutableListOf<String>()

                if (aprobado) {
                    reto.itemRecompensaId?.let { itemsGanados.add(it) }
                    reto.coleccionableRecompensaId?.let { itemsGanados.add(it) }
                    reto.insigniaRecompensaId?.let { insigniasGanadas.add(it) }
                }

                userRepository.registrarRetoCompletado(
                    retoId = reto.id,
                    xpGanada = xpFinal,
                    itemsGanados = itemsGanados,
                    insigniasGanadas = insigniasGanadas
                )
            }
        }
    }

    private fun obtenerRetoParaPunto(puntoId: String): RetoQuiz {
        return when {
            puntoId == "poi_plaza_che" || puntoId.contains("che", ignoreCase = true) -> {
                RetoQuiz(
                    id = "reto_plaza_che",
                    puntoInteresId = puntoId,
                    titulo = "Capítulo 1: Misterios de la Plaza Che",
                    descripcion = "Descubre la historia del corazón estudiantil y político de la UNAL.",
                    preguntas = listOf(
                        Pregunta(
                            id = "che_1",
                            enunciado = "¿A qué prócer de la independencia corresponde la estatua de bronce de la plaza central?",
                            opciones = listOf("Simón Bolívar", "Francisco de Paula Santander", "Antonio Nariño", "Camilo Torres"),
                            indiceCorrecta = 1
                        ),
                        Pregunta(
                            id = "che_2",
                            enunciado = "¿En qué año se pintó por primera vez el icónico mural del Che en el auditorio?",
                            opciones = listOf("1968", "1974", "1984", "1999"),
                            indiceCorrecta = 2
                        ),
                        Pregunta(
                            id = "che_3",
                            enunciado = "¿Qué emblemático auditorio bordea el costado sur de la Plaza Central?",
                            opciones = listOf("Auditorio León de Greiff", "Aula Máxima", "Auditorio Virginia Gutiérrez", "Concha Acústica"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "che_4",
                            enunciado = "¿Cuál es el nombre patrimonial oficial de esta plaza según el plano urbanístico?",
                            opciones = listOf("Plaza Santander", "Plaza Camilo Torres", "Plaza de las Banderas", "Plaza de Mayo"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "che_5",
                            enunciado = "¿Qué edificio acompaña el costado oriental de la plaza?",
                            opciones = listOf("Edificio de Sociología y Ciencias Humanas", "Zootecnia", "Facultad de Artes", "Veterinaria"),
                            indiceCorrecta = 0
                        )
                    ),
                    recompensaXp = 100,
                    recompensaMonedas = 20,
                    itemRecompensaId = "carnet_antiguo",
                    insigniaRecompensaId = "insignia_che",
                    coleccionableRecompensaId = "col_afrecho"
                )
            }
            puntoId == "edif_102" || puntoId.contains("biblio", ignoreCase = true) -> {
                RetoQuiz(
                    id = "reto_biblio",
                    puntoInteresId = puntoId,
                    titulo = "Capítulo 2: Secretos de la Biblioteca",
                    descripcion = "Explora los pasillos y la historia del guardián del saber de la UNAL.",
                    preguntas = listOf(
                        Pregunta(
                            id = "bib_1",
                            enunciado = "¿En qué año se inauguró el edificio actual de la Biblioteca Central?",
                            opciones = listOf("1968", "1973", "1980", "1991"),
                            indiceCorrecta = 1
                        ),
                        Pregunta(
                            id = "bib_2",
                            enunciado = "¿Qué premio Nobel de Literatura colombiano da nombre a esta biblioteca?",
                            opciones = listOf("Gabriel García Márquez", "Álvaro Mutis", "Jorge Isaacs", "José Eustasio Rivera"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "bib_3",
                            enunciado = "¿Con qué apodo popular conocen los estudiantes la fachada de este edificio?",
                            opciones = listOf("El Búho", "El Panal", "El Búnker", "La Pirámide"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "bib_4",
                            enunciado = "¿De qué material característico está hecha su emblemática fachada?",
                            opciones = listOf("Ladrillo a la vista", "Concreto pulido", "Vidrio templado", "Mármol"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "bib_5",
                            enunciado = "¿Quién fue el arquitecto que concibió el diseño de la biblioteca?",
                            opciones = listOf("Alberto Estrada", "Le Corbusier", "Rogelio Salmona", "Leopoldo Rother"),
                            indiceCorrecta = 0
                        )
                    ),
                    recompensaXp = 120,
                    recompensaMonedas = 25,
                    itemRecompensaId = "regla_takeuchi",
                    insigniaRecompensaId = "insignia_biblioteca",
                    coleccionableRecompensaId = "col_periodico"
                )
            }
            puntoId == "edif_401" || puntoId == "edif_454" || puntoId.contains("tecnolog", ignoreCase = true) || puntoId.contains("cyt", ignoreCase = true) -> {
                RetoQuiz(
                    id = "reto_cyt",
                    puntoInteresId = puntoId,
                    titulo = "Capítulo 3: Desafío de Ciencia y Tecnología",
                    descripcion = "Demuestra tu ingenio en el centro neurálgico de la ingeniería y las ciencias.",
                    preguntas = listOf(
                        Pregunta(
                            id = "cyt_1",
                            enunciado = "¿Qué célebre astrónomo y matemático colombiano da nombre al Edificio 401?",
                            opciones = listOf("Julio Garavito Armero", "Francisco José de Caldas", "José Celestino Mutis", "Agustín Codazzi"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "cyt_2",
                            enunciado = "¿En qué cuerpo celeste existe un cráter bautizado en honor a Julio Garavito?",
                            opciones = listOf("La Luna", "Marte", "Mercurio", "Júpiter"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "cyt_3",
                            enunciado = "¿Qué elemento estructural moderno destaca en la arquitectura del CYT?",
                            opciones = listOf("Gran atrio con estructura espacial de acero y vidrio", "Cúpula de madera", "Torre barroca", "Muros de piedra inca"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "cyt_4",
                            enunciado = "¿Qué facultad tiene aquí sus laboratorios de alta computación e investigación?",
                            opciones = listOf("Facultad de Ingeniería", "Facultad de Odontología", "Facultad de Derecho", "Facultad de Bellas Artes"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "cyt_5",
                            enunciado = "¿En qué billete colombiano del Banco de la República aparecía Julio Garavito?",
                            opciones = listOf("Antiguo billete de 20.000 pesos", "Billete de 5.000 pesos", "Billete de 10.000 pesos", "Billete de 50.000 pesos"),
                            indiceCorrecta = 0
                        )
                    ),
                    recompensaXp = 150,
                    recompensaMonedas = 30,
                    itemRecompensaId = "partitura_maestra",
                    insigniaRecompensaId = "insignia_ingenio",
                    coleccionableRecompensaId = "col_microscopio"
                )
            }
            puntoId == "edif_104" || puntoId.contains("greiff", ignoreCase = true) -> {
                RetoQuiz(
                    id = "reto_greiff",
                    puntoInteresId = puntoId,
                    titulo = "Capítulo 4: La Acústica del León de Greiff",
                    descripcion = "Siente la música y el patrimonio en este Monumento Nacional de Colombia.",
                    preguntas = listOf(
                        Pregunta(
                            id = "ldg_1",
                            enunciado = "¿Quién fue la ilustre arquitecta colombiana que diseñó el Auditorio León de Greiff?",
                            opciones = listOf("Eugenia Mantilla de Cardoso", "Luz Amorocho", "Dicken Castro", "Rogelio Salmona"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "ldg_2",
                            enunciado = "¿Por qué cualidad física y arquitectónica es mundialmente elogiado este auditorio?",
                            opciones = listOf("Su acústica perfecta y excepcional", "Ser el más alto de la ciudad", "Estar suspendido en el aire", "Tener túneles subacuáticos"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "ldg_3",
                            enunciado = "¿En qué año fue declarado Monumento Nacional de Colombia?",
                            opciones = listOf("1996", "1972", "1985", "2005"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "ldg_4",
                            enunciado = "¿A qué insigne poeta y catedrático de la UNAL rinde homenaje su nombre?",
                            opciones = listOf("León de Greiff", "Porfirio Barba Jacob", "José Asunción Silva", "Jorge Isaacs"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "ldg_5",
                            enunciado = "¿Qué destacada orquesta colombiana ha tenido como sede emblemática este auditorio?",
                            opciones = listOf("Orquesta Filarmónica de Bogotá", "Sinfónica de Berlín", "Filarmónica de Viena", "Banda Nacional"),
                            indiceCorrecta = 0
                        )
                    ),
                    recompensaXp = 200,
                    recompensaMonedas = 40,
                    itemRecompensaId = "brujula_unal",
                    insigniaRecompensaId = "insignia_arte",
                    coleccionableRecompensaId = "col_vinilo"
                )
            }
            else -> {
                RetoQuiz(
                    id = "reto_campus_${puntoId}",
                    puntoInteresId = puntoId,
                    titulo = "Desafío de Exploración UNAL",
                    descripcion = "Pon a prueba tus conocimientos sobre la Ciudad Blanca.",
                    preguntas = listOf(
                        Pregunta(
                            id = "cam_1",
                            enunciado = "¿En qué año fue fundada la Universidad Nacional de Colombia?",
                            opciones = listOf("1867", "1810", "1936", "1905"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "cam_2",
                            enunciado = "¿Quién fue el arquitecto urbanista alemán que diseñó el plano elíptico del campus Bogotá?",
                            opciones = listOf("Leopoldo Rother", "Fritz Karsen", "Karl Brunner", "Rogelio Salmona"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "cam_3",
                            enunciado = "¿Cuál es el lema en latín presente en el escudo de la UNAL?",
                            opciones = listOf("Inter cornua quaerere disciplinam", "Veritas liberabit vos", "Sapientia et virtus", "Ad astra per aspera"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "cam_4",
                            enunciado = "¿Por qué se le conoce popularmente al campus de Bogotá como la 'Ciudad Blanca'?",
                            opciones = listOf("Por el color original de la arquitectura moderna de Rother", "Por la nieve en los cerros", "Por las flores de jazmín", "Por los eucaliptos blancos"),
                            indiceCorrecta = 0
                        ),
                        Pregunta(
                            id = "cam_5",
                            enunciado = "¿Cuál es el ave mascota representativa de la universidad?",
                            opciones = listOf("El Búho / Nacho", "El Cóndor", "El Águila", "El Colibrí"),
                            indiceCorrecta = 0
                        )
                    ),
                    recompensaXp = 80,
                    recompensaMonedas = 15,
                    itemRecompensaId = null,
                    insigniaRecompensaId = null,
                    coleccionableRecompensaId = null
                )
            }
        }
    }
}