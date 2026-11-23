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
    ),
    PENALES(
        "La tanda final",
        "El Olvido se plantó de arquero. Patea cuando la mira esté lejos de él y mete tres goles."
    ),
    DIFERENCIAS(
        "La copia falsa",
        "El Eco pintó encima del cuadro. Compara el original con la copia y toca en la copia cada cosa que cambió."
    ),
    PAREJAS(
        "Parejas del potrero",
        "Los animales olvidaron quién es su compañero. Voltea las cartas de dos en dos hasta juntar todas las parejas."
    ),
    LABERINTO(
        "Laberinto en la bruma",
        "La bruma no deja ver más allá de un paso. Guía a Nacho hasta el Eco que espera al otro lado del laberinto."
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
    val minijuego: TipoMinijuego,
    val acto: Int = 1,
    // Escena que se muestra después del cierre del último capítulo de cada acto (los epílogos)
    val escenaFinal: String? = null
) {
    val escenaIntro: String get() = "cap${numero}_intro"
    val escenaOutro: String get() = "cap${numero}_outro"

    fun correspondeAPunto(id: String): Boolean = id == puntoId || id in puntoIdsAlternos
}

object Historia {

    const val ESCENA_PROLOGO = "prologo"
    // Epílogo del Acto I (después del capítulo 5) y epílogo final (después del capítulo 8)
    const val ESCENA_EPILOGO = "epilogo"
    const val ESCENA_EPILOGO_FINAL = "epilogo_final"

    val titulosActos = mapOf(
        1 to "El Olvido de la Ciudad Blanca",
        2 to "Los Ecos del Olvido"
    )

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
            sinopsis = "El Olvido se esconde donde antes sonaba la música. Pon la partitura en el atril y haz que el auditorio vuelva a sonar.",
            itemRequeridoId = "partitura_maestra",
            itemRecompensaId = "brujula_unal",
            xpRecompensa = 200,
            minijuego = TipoMinijuego.SINFONIA
        ),
        Capitulo(
            numero = 5,
            puntoId = "poi_estadio",
            retoId = "reto_estadio",
            titulo = "El Último Rastro",
            lugar = "Estadio Alfonso López Pumarejo",
            sinopsis = "Un último jirón de bruma huyó hacia el estadio. La Brújula de Fritz Karsen te lleva hasta él: esto se define por penales.",
            itemRequeridoId = "brujula_unal",
            itemRecompensaId = "copa_memoria",
            xpRecompensa = 250,
            minijuego = TipoMinijuego.PENALES,
            escenaFinal = ESCENA_EPILOGO
        ),
        // ---------- Acto II: Los Ecos del Olvido ----------
        Capitulo(
            numero = 6,
            puntoId = "edif_317",
            retoId = "reto_museo",
            titulo = "Colores Robados",
            lugar = "Museo de Arte UNAL",
            sinopsis = "La Copa de la Memoria se agrietó: el Olvido se partió en tres Ecos. El primero está borrando los colores de los cuadros del museo.",
            itemRequeridoId = "copa_memoria",
            itemRecompensaId = "pincel_restaurador",
            xpRecompensa = 280,
            minijuego = TipoMinijuego.DIFERENCIAS,
            acto = 2
        ),
        Capitulo(
            numero = 7,
            puntoId = "edif_501",
            retoId = "reto_potrero",
            titulo = "El Rebaño Perdido",
            lugar = "Facultad de Medicina Veterinaria y de Zootecnia",
            sinopsis = "El segundo Eco hizo que los animales de los potreros olvidaran a sus compañeros. Con el Pincel Restaurador puedes devolverles la memoria.",
            itemRequeridoId = "pincel_restaurador",
            itemRecompensaId = "cencerro_memoria",
            xpRecompensa = 320,
            minijuego = TipoMinijuego.PAREJAS,
            acto = 2
        ),
        Capitulo(
            numero = 8,
            puntoId = "edif_761",
            retoId = "reto_concha",
            titulo = "La Voz del Campus",
            lugar = "Concha Acústica",
            sinopsis = "El último Eco convirtió la Concha Acústica en un laberinto de bruma. Solo el sonido del Cencerro de la Memoria puede guiarte.",
            itemRequeridoId = "cencerro_memoria",
            itemRecompensaId = "copa_restaurada",
            xpRecompensa = 400,
            minijuego = TipoMinijuego.LABERINTO,
            acto = 2,
            escenaFinal = ESCENA_EPILOGO_FINAL
        )
    )

    fun capitulosDelActo(acto: Int): List<Capitulo> = capitulos.filter { it.acto == acto }

    fun actoCompletado(acto: Int, retosCompletados: Collection<String>): Boolean =
        capitulosDelActo(acto).all { it.retoId in retosCompletados }

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
            LineaDialogo(Hablante.NACHO, "El León de Greiff. Si el Olvido apaga la música del auditorio, se queda con todo. ¡No lo podemos permitir, {jugador}!", Expresion.PREOCUPADO)
        ),
        "cap4_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "El Auditorio León de Greiff. Aquí ha sonado la Filarmónica de Bogotá y han vibrado generaciones enteras."),
            LineaDialogo(Hablante.OLVIDO, "Sin música... sin memoria... sin nombre. Este es mi hogar ahora."),
            LineaDialogo(Hablante.NACHO, "No mientras estemos aquí. Pon la partitura en el atril, escucha bien la melodía... y haz que este lugar vuelva a sonar.", Expresion.DECIDIDO)
        ),
        "cap4_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "Una nota. Luego otra. El auditorio entero estalla en música y la bruma se rompe en mil pedazos de luz."),
            LineaDialogo(Hablante.OLVIDO, "Imposible... recuerdan... todos recuerdan..."),
            LineaDialogo(Hablante.NACHO, "¡Eso, {jugador}! ¡El León de Greiff volvió a sonar! Y mira lo que dejó caer la bruma: la Brújula de Fritz Karsen. Dicen que con ella se trazó el campus.", Expresion.FELIZ),
            LineaDialogo(Hablante.NACHO, "Un momento... la aguja no se queda quieta. Apunta al Estadio Alfonso López. ¡Un pedazo de bruma se nos escapó para allá!", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.NACHO, "Esto todavía no se acaba. ¡Al estadio!", Expresion.DECIDIDO)
        ),
        "cap5_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "El Estadio Alfonso López. Aquí se jugaron los primeros Juegos Bolivarianos, en 1938. Si estas graderías hablaran..."),
            LineaDialogo(Hablante.OLVIDO, "Ya no me queda nada... solo este arco. Aquí nadie me hace un gol... nadie se acuerda de cómo..."),
            LineaDialogo(Hablante.NACHO, "¡Se plantó de arquero! Bueno, {jugador}, esto se define como las mejores finales: por penales. Métele tres goles y la bruma se va para siempre.", Expresion.DECIDIDO)
        ),
        "cap5_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "El balón infla la red. La última nube de bruma se deshace sobre la gramilla y el estadio entero parece rugir."),
            LineaDialogo(Hablante.OLVIDO, "Buen partido... Quizá recordar... no era tan malo..."),
            LineaDialogo(Hablante.NACHO, "¡GOOOOL! ¡Campeones, {jugador}! Y mira: debajo de la bruma estaba escondida la Copa de la Memoria.", Expresion.FELIZ)
        ),
        ESCENA_EPILOGO to listOf(
            LineaDialogo(Hablante.NARRADOR, "Esa noche, los edificios del campus brillaron un poco más. Nadie supo explicar por qué."),
            LineaDialogo(Hablante.NACHO, "Lo logramos. La Ciudad Blanca recuerda otra vez, y fue gracias a ti.", Expresion.FELIZ),
            LineaDialogo(Hablante.NACHO, "El Olvido siempre va a rondar por ahí. Por eso vuelve cada día: la trivia mantiene viva la memoria del campus."),
            LineaDialogo(Hablante.NACHO, "Desde hoy eres oficialmente una Leyenda de la Ciudad Blanca. ¡Qué nota!", Expresion.FELIZ),
            // Gancho del Acto II
            LineaDialogo(Hablante.NARRADOR, "Pero antes de que Nacho se fuera a descansar, la Copa de la Memoria hizo un ruidito. Crac."),
            LineaDialogo(Hablante.NACHO, "¿Oíste eso? La Copa... tiene una grieta. Eso no estaba ahí hace un momento.", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.NACHO, "Descansa, {jugador}. Mañana revisamos el mapa: algo me dice que esto no se ha acabado.", Expresion.DECIDIDO)
        ),

        // ---------- Acto II: Los Ecos del Olvido ----------
        "cap6_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "{jugador}, la grieta de la Copa se hizo más grande. Mira: por dentro brilla un mensaje.", Expresion.PREOCUPADO),
            LineaDialogo(Hablante.NARRADOR, "«Cuando la bruma se rinde no desaparece: se parte en ecos. Y los ecos se esconden donde la gente guarda lo que más quiere.»"),
            LineaDialogo(Hablante.NACHO, "¡El Olvido no se fue! Al perder en el estadio se partió en tres Ecos. Si no los encontramos, la Copa se rompe y todo lo que recuperamos se vuelve a borrar.", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.NACHO, "El primero está aquí, en el Museo de Arte. Donde la gente guarda los colores.", Expresion.DECIDIDO),
            LineaDialogo(Hablante.OLVIDO, "Colores... tan ruidosos... Pinté encima. Nadie notará la diferencia..."),
            LineaDialogo(Hablante.NACHO, "¡Cambió el cuadro por una copia falsa! Compara el original con la copia y encuentra todo lo que el Eco alteró.", Expresion.DECIDIDO)
        ),
        "cap6_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "Cada diferencia que encuentras devuelve un color. Rojo, azul, amarillo... el cuadro vuelve a respirar."),
            LineaDialogo(Hablante.OLVIDO, "Ustedes... sí miran con cuidado..."),
            LineaDialogo(Hablante.NACHO, "¡Un Eco menos! Y dejó caer esto: un Pincel Restaurador. Con él se puede pintar de vuelta lo que la bruma borra.", Expresion.FELIZ),
            LineaDialogo(Hablante.NACHO, "Oye... ¿eso que se oye son vacas? El segundo Eco se fue para los potreros de Veterinaria.", Expresion.SORPRENDIDO)
        ),
        "cap7_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "Veterinaria. Aquí hay potreros, establos y una clínica donde atienden desde perros hasta caballos. Y sí: vacas en plena ciudad."),
            LineaDialogo(Hablante.OLVIDO, "Muuu... ¿quién soy?... ¿quién eres?... Nadie recuerda a su pareja..."),
            LineaDialogo(Hablante.NACHO, "¡Los animales olvidaron quiénes son sus compañeros! Voltea las cartas de dos en dos y vuelve a juntar cada pareja.", Expresion.PREOCUPADO)
        ),
        "cap7_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "Las vacas vuelven al corral, los caballos relinchan y un perro de la clínica mueve la cola como si nada."),
            LineaDialogo(Hablante.NACHO, "¡Lo lograste! Y mira lo que estaba colgado en la cerca: el Cencerro de la Memoria. Dicen que su sonido atraviesa cualquier bruma.", Expresion.FELIZ),
            LineaDialogo(Hablante.OLVIDO, "La voz... nos queda la voz... la Concha... donde nadie se oirá..."),
            LineaDialogo(Hablante.NACHO, "¡El último Eco se escondió en la Concha Acústica! Ahí se ha cantado de todo. Vamos, {jugador}.", Expresion.DECIDIDO)
        ),
        "cap8_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "La Concha Acústica. Su forma devuelve el sonido hacia el público: aquí una sola voz llena todo el lugar."),
            LineaDialogo(Hablante.NARRADOR, "Pero hoy no se oye nada. La bruma convirtió el escenario en un laberinto gris donde no se ve más allá de un paso."),
            LineaDialogo(Hablante.OLVIDO, "Aquí todo es mío... Aquí nadie se encuentra... ni se escucha..."),
            LineaDialogo(Hablante.NACHO, "Tienes el Cencerro, {jugador}. Avanza con calma; si te pierdes, hazlo sonar y te dirá por dónde. ¡Encuentra al último Eco!", Expresion.DECIDIDO)
        ),
        "cap8_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "Al final del laberinto, el cencerro suena una vez. El eco rebota en la Concha... y por primera vez la bruma escucha."),
            LineaDialogo(Hablante.OLVIDO, "Tanto tiempo quise que todo quedara en silencio... Pensé que olvidar dolía menos."),
            LineaDialogo(Hablante.NACHO, "Olvidar no te quita lo que duele, Olvido. Solo te quita lo que vale la pena. ¿Por qué no te quedas a recordar con nosotros?"),
            LineaDialogo(Hablante.OLVIDO, "¿Recordar... yo?..."),
            LineaDialogo(Hablante.NARRADOR, "Los tres Ecos se juntan sobre el escenario. La bruma ya no es gris: brilla como una tarde despejada sobre la Ciudad Blanca."),
            LineaDialogo(Hablante.NACHO, "¡Mira la Copa! Se cerró la grieta. Ahora es la Copa Restaurada... y brilla más que nunca.", Expresion.FELIZ)
        ),
        ESCENA_EPILOGO_FINAL to listOf(
            LineaDialogo(Hablante.NARRADOR, "Desde ese día, cuando cae la tarde, sobre la Concha Acústica flota una bruma suave y dorada."),
            LineaDialogo(Hablante.NACHO, "Es el Olvido. Bueno, ahora se hace llamar el Recuerdo. Me ayuda a cuidar la memoria del campus.", Expresion.FELIZ),
            LineaDialogo(Hablante.NACHO, "Todavía quedan rincones por descubrir, preguntas por responder y una trivia nueva cada día."),
            LineaDialogo(Hablante.NACHO, "Gracias, {jugador}. Desde hoy eres Guardián de la Memoria de la Ciudad Blanca. ¡Qué nota!", Expresion.FELIZ)
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
