package com.unal.nachoquest.domain.model

/**
 * Historia principal de NachoQuest: "El Olvido de la Ciudad Blanca".
 *
 * Una bruma gris (el Olvido) está borrando la memoria del campus. Nacho, el espíritu
 * guardián de la universidad, recluta al jugador para recuperar un Fragmento de Memoria
 * en cada edificio emblemático. Cada capítulo entrega el ítem que abre el siguiente.
 */

enum class Hablante { NACHO, OLVIDO, NARRADOR }

enum class Expresion { NORMAL, FELIZ, SORPRENDIDO, PREOCUPADO, DECIDIDO }

/** Minijuego con el que se recupera el Fragmento de Memoria de cada capítulo. */
enum class TipoMinijuego(val titulo: String, val instrucciones: String) {
    MURAL(
        "Reconstruye el recuerdo",
        "El Olvido desordenó el recuerdo de la plaza. Toca dos piezas para intercambiarlas hasta armar la imagen."
    ),
    LETRAS(
        "Letras borradas",
        "La bruma borró las letras de los libros. Adivina cada palabra antes de que se acabe la tinta."
    ),
    CIRCUITO(
        "Reconecta el laboratorio",
        "Gira las piezas para que la energía llegue del generador al tablero."
    ),
    SINFONIA(
        "La última sinfonía",
        "Escucha la melodía y repítela en el mismo orden. Cada ronda suma una nota."
    )
}

data class LineaDialogo(
    val hablante: Hablante,
    val texto: String,
    val expresion: Expresion = Expresion.NORMAL
)

data class Capitulo(
    val numero: Int,
    val puntoId: String,
    // Otros ids de puntos del mapa que también llevan a este capítulo
    val puntoIdsAlternos: List<String> = emptyList(),
    val retoId: String,
    val titulo: String,
    val lugar: String,
    val sinopsis: String,
    val itemRequeridoId: String?,
    val itemRecompensaId: String,
    val xpRecompensa: Int,
    val minijuego: TipoMinijuego
) {
    val escenaIntro: String get() = "cap${numero}_intro"
    val escenaOutro: String get() = "cap${numero}_outro"

    fun correspondeAPunto(id: String): Boolean = id == puntoId || id in puntoIdsAlternos
}

object Historia {

    const val ESCENA_PROLOGO = "prologo"
    const val ESCENA_EPILOGO = "epilogo"

    val capitulos = listOf(
        Capitulo(
            numero = 1,
            puntoId = "poi_plaza_che",
            retoId = "reto_plaza_che",
            titulo = "El Despertar",
            lugar = "Plaza Central Santander (Plaza Che)",
            sinopsis = "La bruma llegó primero al corazón del campus. Recupera la memoria de la plaza donde todo empieza.",
            itemRequeridoId = null,
            itemRecompensaId = "carnet_antiguo",
            xpRecompensa = 100,
            minijuego = TipoMinijuego.MURAL
        ),
        Capitulo(
            numero = 2,
            puntoId = "edif_102",
            retoId = "reto_biblio",
            titulo = "Páginas en Blanco",
            lugar = "Biblioteca Central Gabriel García Márquez",
            sinopsis = "El Olvido está borrando la tinta de los libros. Usa el Carné Histórico para entrar antes de que no quede ni una letra.",
            itemRequeridoId = "carnet_antiguo",
            itemRecompensaId = "regla_takeuchi",
            xpRecompensa = 120,
            minijuego = TipoMinijuego.LETRAS
        ),
        Capitulo(
            numero = 3,
            puntoId = "edif_401",
            retoId = "reto_cyt",
            titulo = "La Ecuación Perdida",
            lugar = "Edificio Julio Garavito (Ingeniería)",
            sinopsis = "Los laboratorios se quedaron sin energía. Con la Regla de Takeuchi puedes volver a encenderlos.",
            itemRequeridoId = "regla_takeuchi",
            itemRecompensaId = "partitura_maestra",
            xpRecompensa = 150,
            minijuego = TipoMinijuego.CIRCUITO
        ),
        Capitulo(
            numero = 4,
            puntoId = "edif_104",
            retoId = "reto_greiff",
            titulo = "La Última Sinfonía",
            lugar = "Auditorio León de Greiff",
            sinopsis = "El Olvido se esconde donde antes sonaba la música. Es la batalla final: haz que el auditorio vuelva a sonar.",
            itemRequeridoId = "partitura_maestra",
            itemRecompensaId = "brujula_unal",
            xpRecompensa = 200,
            minijuego = TipoMinijuego.SINFONIA
        )
    )

    fun capituloPorPunto(puntoId: String): Capitulo? = capitulos.find { it.correspondeAPunto(puntoId) }

    fun capituloPorReto(retoId: String): Capitulo? = capitulos.find { it.retoId == retoId }

    /** Primer capítulo sin completar, o null si la historia ya terminó. */
    fun capituloActual(retosCompletados: Collection<String>): Capitulo? =
        capitulos.firstOrNull { it.retoId !in retosCompletados }

    fun historiaCompletada(retosCompletados: Collection<String>): Boolean =
        capituloActual(retosCompletados) == null

    /** Id del reto asociado a un punto del mapa (capítulo de historia o reto genérico del campus). */
    fun retoIdParaPunto(puntoId: String): String =
        capituloPorPunto(puntoId)?.retoId ?: "reto_campus_$puntoId"

    // {jugador} se reemplaza por el primer nombre del jugador al mostrar la escena
    val escenas: Map<String, List<LineaDialogo>> = mapOf(
        ESCENA_PROLOGO to listOf(
            LineaDialogo(Hablante.NARRADOR, "Bogotá. Una tarde cualquiera en la Ciudad Blanca..."),
            LineaDialogo(Hablante.NARRADOR, "Pero hoy algo anda mal. Una bruma gris se arrastra entre los edificios y, por donde pasa, la gente olvida."),
            LineaDialogo(Hablante.NACHO, "¡Ey, tú! Sí, tú, el del celular. ¿Me puedes ver? ¡Qué nota, por fin alguien!", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.NACHO, "Soy Nacho, el espíritu guardián del campus. Llevo aquí desde que esto eran puros planos y potrero.", Expresion.FELIZ),
            LineaDialogo(Hablante.NACHO, "Tenemos un problema serio, {jugador}. Esa bruma es el Olvido, y se está comiendo la historia de la universidad.", Expresion.PREOCUPADO),
            LineaDialogo(Hablante.OLVIDO, "Plaza... biblioteca... auditorio... ¿Para qué recordar? Todo es más tranquilo... en gris..."),
            LineaDialogo(Hablante.NACHO, "¡Ni de riesgos! Cada edificio guarda un Fragmento de Memoria. Si respondes sus preguntas, el recuerdo vuelve y el Olvido retrocede.", Expresion.DECIDIDO),
            LineaDialogo(Hablante.NACHO, "Empecemos por el corazón del campus: la Plaza Che. Busca el marcador dorado en el mapa. ¡Vamos!", Expresion.FELIZ)
        ),
        "cap1_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "Aquí estamos: la Plaza Che. Marchas, conciertos, debates y almuerzos en el pasto... aquí ha pasado de todo."),
            LineaDialogo(Hablante.OLVIDO, "Solo es cemento... nadie recuerda nada..."),
            LineaDialogo(Hablante.NACHO, "¡Mentira! Desordenó el recuerdo de la plaza como un rompecabezas. Ármalo y luego demuéstrale cuánto sabes de este lugar.", Expresion.DECIDIDO)
        ),
        "cap1_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "La bruma se disipa. Los colores vuelven a la plaza y, entre las baldosas, algo brilla..."),
            LineaDialogo(Hablante.NACHO, "¡Un Carné Histórico! Con este sello, los torniquetes de la Biblioteca Central se abren solitos.", Expresion.FELIZ),
            LineaDialogo(Hablante.NACHO, "Pero mira... la bruma se fue hacia la biblioteca. Quiere borrar los libros. ¡Rápido, {jugador}!", Expresion.PREOCUPADO)
        ),
        "cap2_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "La Biblioteca Central. Aquí se ha estudiado para más parciales de los que puedo contar."),
            LineaDialogo(Hablante.OLVIDO, "Millones de páginas... y ni una palabra... ya no quedan palabras..."),
            LineaDialogo(Hablante.NACHO, "¡Está borrando las letras de los libros! Usa tu carné, entra y recupera las palabras antes de que se acabe la tinta.", Expresion.SORPRENDIDO)
        ),
        "cap2_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "Las páginas recuperan su tinta, una por una. En un estante, algo hace clic."),
            LineaDialogo(Hablante.NACHO, "¡La Regla de Takeuchi! En honor al profe Yu Takeuchi, que enseñó matemáticas a generaciones enteras. Con esto abrimos los laboratorios.", Expresion.FELIZ),
            LineaDialogo(Hablante.OLVIDO, "Tú... ¿quién eres para resistirte...? Me iré... a donde están los números..."),
            LineaDialogo(Hablante.NACHO, "¡Va hacia el edificio de Ingeniería! Allá nos vemos.", Expresion.DECIDIDO)
        ),
        "cap3_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "El edificio Julio Garavito, casa de la Ingeniería. Aquí se calcula, se construye y se inventa. Huele a café y a entrega de última hora."),
            LineaDialogo(Hablante.OLVIDO, "Fórmulas... máquinas... tanto ruido... Apagué todo. El silencio es mejor..."),
            LineaDialogo(Hablante.NACHO, "¡Dejó los laboratorios sin energía! Reconecta el circuito y devuélveles la vida.", Expresion.DECIDIDO)
        ),
        "cap3_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "Las luces vuelven, los tableros se iluminan con ecuaciones y de un casillero sale volando una hoja de papel pautado."),
            LineaDialogo(Hablante.NACHO, "¿Una partitura? ¡Es la Partitura Maestra! Esto solo puede ir a un lugar...", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.NACHO, "El León de Greiff. Si el Olvido apaga la música del auditorio, se queda con todo. Es la batalla final, {jugador}.", Expresion.PREOCUPADO)
        ),
        "cap4_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "El Auditorio León de Greiff. Aquí ha sonado la Filarmónica de Bogotá y han vibrado generaciones enteras."),
            LineaDialogo(Hablante.OLVIDO, "Sin música... sin memoria... sin nombre. Este es mi hogar ahora."),
            LineaDialogo(Hablante.NACHO, "No mientras estemos aquí. Pon la partitura en el atril, escucha bien la melodía... y haz que este lugar vuelva a sonar.", Expresion.DECIDIDO)
        ),
        "cap4_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "Una nota. Luego otra. El auditorio entero estalla en música y la bruma se deshace en mil pedazos de luz."),
            LineaDialogo(Hablante.OLVIDO, "Imposible... recuerdan... todos recuerdan..."),
            LineaDialogo(Hablante.NACHO, "¡LO LOGRASTE, {jugador}! ¡La Ciudad Blanca recuerda otra vez!", Expresion.FELIZ)
        ),
        ESCENA_EPILOGO to listOf(
            LineaDialogo(Hablante.NARRADOR, "Esa noche, los edificios del campus brillaron un poco más. Nadie supo explicar por qué."),
            LineaDialogo(Hablante.NACHO, "Toma, te la ganaste: la Brújula de Fritz Karsen. Dicen que con ella se trazó el campus.", Expresion.FELIZ),
            LineaDialogo(Hablante.NACHO, "Un momento... la aguja apunta al Estadio Alfonso López. ¿Quedó algo de bruma por allá?", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.NACHO, "Eso es historia para otro día. Por ahora eres oficialmente una Leyenda de la Ciudad Blanca. ¡Qué nota!", Expresion.FELIZ)
        )
    )

    val frasesDerrota = listOf(
        LineaDialogo(Hablante.NACHO, "Uy, la bruma te alcanzó esta vez. Tranqui, hasta los mejores pierden un parcial.", Expresion.PREOCUPADO),
        LineaDialogo(Hablante.NACHO, "El Olvido es terco, pero tú lo eres más. ¡Dale otra vez!", Expresion.DECIDIDO),
        LineaDialogo(Hablante.NACHO, "Casi, casi. Respira, repasa y vuelve con todo.", Expresion.PREOCUPADO)
    )

    fun escena(id: String, nombreJugador: String): List<LineaDialogo> =
        escenas[id].orEmpty().map { it.copy(texto = it.texto.replace("{jugador}", nombreJugador)) }
}
