package com.unal.nachoquest.domain.model

/**
 * Historia principal de NachoQuest: "El Olvido de la Ciudad Blanca".
 *
 * Una bruma gris (el Olvido) está borrando la memoria del campus. Nacho, el espíritu
 * guardián de la universidad, recluta al jugador para recuperar un Fragmento de Memoria
 * en cada edificio emblemático. Cada capítulo entrega el ítem que abre el siguiente.
 */

/**
 * Quién habla en una escena. El Olvido se volvió el Recuerdo al final del Acto II y desde el
 * Acto III es aliado de Nacho; el Enredo es el personaje nuevo del Acto III y desde el IV
 * también es aliado. En el Acto IV llegan el Sello Pendiente y el Búho, guardián del Archivo.
 */
enum class Hablante { NACHO, OLVIDO, RECUERDO, ENREDO, SELLO, BUHO, NARRADOR }

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
    ),
    MONTAJE(
        "Arma la película",
        "El Enredo cortó la película y pegó los fotogramas al azar. Tócalos en orden para que cada escena vuelva a contarse bien."
    ),
    TANQUES(
        "Agua exacta",
        "Llena, vacía y pasa agua de un tanque al otro hasta que en uno quede exactamente la cantidad que se pide."
    ),
    HELICE(
        "Completa la hélice",
        "El Enredo le quitó un lado al ADN. Ponle a cada letra su pareja (A con T, C con G) antes de que la hélice se desenrolle."
    ),
    VITRAL(
        "Enciende el vitral",
        "El Enredo apagó los vidrios del vitral. Al tocar uno cambian él y los de arriba, abajo y a los lados. Déjalos todos encendidos."
    ),
    RELEVOS(
        "La carrera de relevos",
        "Un 4×100 contra el Enredo. Toca los pies en orden para correr y pasa el testigo en la zona amarilla: entre más al final, con más impulso sale el siguiente."
    ),
    TRAMITES(
        "Cada papel a su bandeja",
        "Los trámites llegan uno por uno. Déjalos en su bandeja antes de que el Sello les estampe «PENDIENTE»."
    ),
    ARCHIVO(
        "Busca el folio",
        "Las cajas del Archivo están en orden de número. Abre una y sabrás si el folio está antes o después. Encuéntralo abriendo las menos posibles."
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
    // Epílogos del Acto I (después del capítulo 5), del Acto II (después del 8) y del Acto III (después del 13)
    const val ESCENA_EPILOGO = "epilogo"
    const val ESCENA_EPILOGO_FINAL = "epilogo_final"
    const val ESCENA_EPILOGO_ACTO3 = "epilogo_acto3"

    val titulosActos = mapOf(
        1 to "El Olvido de la Ciudad Blanca",
        2 to "Los Ecos del Olvido",
        3 to "Los Hilos de la Memoria",
        4 to "El Sello Pendiente"
    )

    /** Número romano del acto, para los títulos («ACTO III»). */
    fun romano(acto: Int): String = when (acto) {
        1 -> "I"
        2 -> "II"
        3 -> "III"
        4 -> "IV"
        5 -> "V"
        else -> acto.toString()
    }

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
        ),
        // ---------- Acto III: Los Hilos de la Memoria ----------
        Capitulo(
            numero = 9,
            puntoId = "edif_701",
            retoId = "reto_cine",
            titulo = "Fuera de Cuadro",
            lugar = "Antigua sede de Cine y Televisión",
            sinopsis = "De la Copa Restaurada salió el Enredo, que se llevó cinco Hilos de la Memoria. El primero está en la vieja casa de la Escuela de Cine, donde cortó la película del campus en pedacitos.",
            itemRequeridoId = "copa_restaurada",
            itemRecompensaId = "hilo_rojo",
            xpRecompensa = 300,
            minijuego = TipoMinijuego.MONTAJE,
            acto = 3
        ),
        Capitulo(
            numero = 10,
            puntoId = "edif_408",
            retoId = "reto_hidraulica",
            titulo = "Agua Revuelta",
            lugar = "Laboratorio de Hidráulica",
            sinopsis = "El Enredo revolvió el agua de los tanques de ensayo y el Hilo Azul quedó en el fondo. Solo sube si el agua queda exacta.",
            itemRequeridoId = "hilo_rojo",
            itemRecompensaId = "hilo_azul",
            xpRecompensa = 330,
            minijuego = TipoMinijuego.TANQUES,
            acto = 3
        ),
        Capitulo(
            numero = 11,
            puntoId = "edif_426",
            retoId = "reto_genetica",
            titulo = "La Doble Hélice",
            lugar = "Instituto de Genética (IGUN)",
            sinopsis = "El Enredo le quitó un lado al ADN de las muestras del instituto. Si la hélice se desenrolla, se lleva el Hilo Verde con ella.",
            itemRequeridoId = "hilo_azul",
            itemRecompensaId = "hilo_verde",
            xpRecompensa = 360,
            minijuego = TipoMinijuego.HELICE,
            acto = 3
        ),
        Capitulo(
            numero = 12,
            puntoId = "edif_251",
            retoId = "reto_capilla",
            titulo = "El Vitral Roto",
            lugar = "Capilla Cristo Maestro",
            sinopsis = "El Enredo apagó el vitral de la capilla vidrio por vidrio. El Hilo Amarillo solo aparece cuando la luz vuelve a pasar por todos.",
            itemRequeridoId = "hilo_verde",
            itemRecompensaId = "hilo_amarillo",
            xpRecompensa = 390,
            minijuego = TipoMinijuego.VITRAL,
            acto = 3
        ),
        Capitulo(
            numero = 13,
            puntoId = "edif_103",
            retoId = "reto_polideportivo",
            titulo = "El Último Nudo",
            lugar = "Centro Polideportivo",
            sinopsis = "El Enredo lleva el Hilo Morado amarrado a la cintura y te reta a una carrera de relevos. Si le ganas, te lo entrega.",
            itemRequeridoId = "hilo_amarillo",
            itemRecompensaId = "madeja_memoria",
            xpRecompensa = 450,
            minijuego = TipoMinijuego.RELEVOS,
            acto = 3,
            escenaFinal = ESCENA_EPILOGO_ACTO3
        ),
        // ---------- Acto IV: El Sello Pendiente ----------
        Capitulo(
            numero = 14,
            puntoId = "edif_861",
            retoId = "reto_uriel",
            titulo = "Todo Pendiente",
            lugar = "Edificio Uriel Gutiérrez",
            sinopsis = "Los carnés y los recibos de matrícula salen con un sello rojo: PENDIENTE. El rastro de tinta lleva a la sede administrativa de la universidad.",
            itemRequeridoId = "madeja_memoria",
            itemRecompensaId = "folio_uriel",
            xpRecompensa = 480,
            minijuego = TipoMinijuego.TRAMITES,
            acto = 4
        ),
        Capitulo(
            numero = 15,
            puntoId = "edif_500B",
            retoId = "reto_archivo",
            titulo = "El Guardián del Archivo",
            lugar = "Archivo Central e Histórico",
            sinopsis = "El Sello revolvió las cajas donde duerme la memoria escrita de la universidad. Entre los estantes, alguien te espera con los ojos abiertos.",
            itemRequeridoId = "folio_uriel",
            itemRecompensaId = "folio_archivo",
            xpRecompensa = 510,
            minijuego = TipoMinijuego.ARCHIVO,
            acto = 4
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
            LineaDialogo(Hablante.NACHO, "Gracias, {jugador}. Desde hoy eres Guardián de la Memoria de la Ciudad Blanca. ¡Qué nota!", Expresion.FELIZ),
            // Gancho del Acto III
            LineaDialogo(Hablante.NARRADOR, "Esa misma noche, la Copa Restaurada empezó a soltar hilitos de colores, como un saco de lana viejo."),
            LineaDialogo(Hablante.NARRADOR, "Y desde algún rincón del campus, alguien se rió bajito.")
        ),

        // ---------- Acto III: Los Hilos de la Memoria ----------
        "cap9_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "¡{jugador}! ¿Viste la Copa? Está soltando hilos de colores, como un saco de lana viejo.", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.RECUERDO, "Volvieron tantos recuerdos de golpe que algunos se anudaron entre sí... y de ese nudo nació alguien."),
            LineaDialogo(Hablante.ENREDO, "¡Hola, hola! ¿Jugamos? Me llevé cinco hilos de la Copa. Si los quieren, ¡vengan a buscarlos!"),
            LineaDialogo(Hablante.NACHO, "¿Y este quién es? No borra nada, pero... lo revuelve todo.", Expresion.PREOCUPADO),
            LineaDialogo(Hablante.RECUERDO, "Es el Enredo. Escondió el primer hilo aquí, junto al estadio: la vieja casa de la Escuela de Cine, donde se aprendió a hacer películas durante casi treinta años."),
            LineaDialogo(Hablante.ENREDO, "¡Corté la película del campus y pegué los pedacitos al revés! ¿A que no la arman?"),
            LineaDialogo(Hablante.NACHO, "Pon los fotogramas en orden, {jugador}. Una película solo tiene sentido si cada imagen va donde toca.", Expresion.DECIDIDO)
        ),
        "cap9_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "El proyector se enciende. La película del campus corre de principio a fin y, por un instante, todo el edificio huele a crispetas."),
            LineaDialogo(Hablante.ENREDO, "¡Ey, la armaron rapidísimo! Bueno, bueno... tomen su hilo. ¡Pero el siguiente está mojado!"),
            LineaDialogo(Hablante.NACHO, "¡El Hilo Rojo! El primero de los cinco. Está tibio, como una sala de cine en pleno estreno.", Expresion.FELIZ),
            LineaDialogo(Hablante.RECUERDO, "Escuchen... agua que corre por donde no debería. El Enredo se fue al Laboratorio de Hidráulica."),
            LineaDialogo(Hablante.NACHO, "¡Pues para allá vamos, {jugador}! Ojalá no nos toque nadar.", Expresion.DECIDIDO)
        ),
        "cap10_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "El Laboratorio de Hidráulica. Aquí los ingenieros prueban con modelos cómo se porta el agua antes de construir un puente, una represa o un acueducto."),
            LineaDialogo(Hablante.ENREDO, "¡Splash! Revolví el agua de todos los tanques. ¿Cuánta hay en cada uno? ¡Ni idea!"),
            LineaDialogo(Hablante.RECUERDO, "El Hilo Azul está en el fondo de un tanque. Solo sube si el agua queda exacta, ni una gota más ni una menos."),
            LineaDialogo(Hablante.NACHO, "Llena, vacía y pasa agua de un tanque al otro hasta dejar justo lo que se pide. Piensa antes de echar agua, {jugador}.", Expresion.DECIDIDO)
        ),
        "cap10_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "El agua se aquieta. Justo en la marca exacta, algo azul sube flotando hasta la superficie."),
            LineaDialogo(Hablante.ENREDO, "¡Ustedes sí saben medir! Bueno... el siguiente hilo lo escondí donde todo viene de a pares."),
            LineaDialogo(Hablante.NACHO, "¡El Hilo Azul! Ya van dos de cinco.", Expresion.FELIZ),
            LineaDialogo(Hablante.RECUERDO, "¿De a pares...? La A con la T, la C con la G. Está hablando del ADN: se fue al Instituto de Genética."),
            LineaDialogo(Hablante.NACHO, "¡Si el Enredo se pone a revolver ADN, quién sabe qué sale! Vamos, {jugador}.", Expresion.SORPRENDIDO)
        ),
        "cap11_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "El Instituto de Genética. Aquí se estudia el ADN y se hacen pruebas para saber quién es familia de quién."),
            LineaDialogo(Hablante.ENREDO, "¡Miren esta escalera retorcida tan bonita! Le quité un lado. Así se ve más... liviana, ¿no?"),
            LineaDialogo(Hablante.RECUERDO, "Sin su otra mitad, la doble hélice se desenrolla. Y se lleva el Hilo Verde con ella."),
            LineaDialogo(Hablante.NACHO, "Cada letra tiene su pareja: la A va con la T y la C con la G. Completa la cadena antes de que se desenrolle, {jugador}.", Expresion.DECIDIDO)
        ),
        "cap11_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "La doble hélice vuelve a girar, tranquila. Entre dos de sus peldaños brilla un hilo verde."),
            LineaDialogo(Hablante.ENREDO, "Uy, qué juiciosos. ¡Pero el próximo escondite es el más bonito! Puros colores... y vidrios."),
            LineaDialogo(Hablante.NACHO, "¡El Hilo Verde! Ya van tres.", Expresion.FELIZ),
            LineaDialogo(Hablante.RECUERDO, "Vidrios de colores... Eso es un vitral. La capilla del campus tiene unos preciosos."),
            LineaDialogo(Hablante.NACHO, "Si el Enredo rompe ese vitral, no se lo perdono. ¡A la capilla, {jugador}!", Expresion.PREOCUPADO)
        ),
        "cap12_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "La Capilla Cristo Maestro, de 1953. Su nombre viene del vitral del pintor francés Paul Boné, que muestra a Cristo enseñando."),
            LineaDialogo(Hablante.ENREDO, "¡Qué oscurito tan rico! Le apagué los vidrios uno por uno. Y los enredé: si tocas uno, cambian también sus vecinos."),
            LineaDialogo(Hablante.RECUERDO, "Un vitral sin luz es solo plomo y vidrio gris. Y el Hilo Amarillo se esconde donde no llega el sol."),
            LineaDialogo(Hablante.NACHO, "Toca los vidrios hasta que todos brillen, {jugador}. Piensa bien: el mismo toque que prende uno puede apagar al de al lado.", Expresion.DECIDIDO)
        ),
        "cap12_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "La luz de la tarde atraviesa el vitral y pinta el piso de colores. En el rayo más dorado flota un hilo amarillo."),
            LineaDialogo(Hablante.ENREDO, "¡Ay, qué brillo! Me encandilé... Bueno, me queda un solo hilo. Y ese no lo suelto: lo llevo amarrado a mí."),
            LineaDialogo(Hablante.NACHO, "¡El Hilo Amarillo! Ya van cuatro de cinco.", Expresion.FELIZ),
            LineaDialogo(Hablante.RECUERDO, "El último lo tiene él. Lo oí correr hacia el Polideportivo: quiere que lo alcancemos."),
            LineaDialogo(Hablante.NACHO, "Pues a correr se dijo. ¡Al Polideportivo, {jugador}!", Expresion.DECIDIDO)
        ),
        "cap13_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "El Centro Polideportivo. Canchas, gimnasio y gente entrenando a toda hora: aquí nadie se queda quieto."),
            LineaDialogo(Hablante.ENREDO, "¡Llegaron! El Hilo Morado lo tengo amarrado a la cintura. Si lo quieren, ¡gánenme una carrera de relevos!"),
            LineaDialogo(Hablante.RECUERDO, "Cuatro tramos de cien metros. El testigo se pasa en la zona de entrega; si se cae, hay que recogerlo."),
            LineaDialogo(Hablante.NACHO, "Alterna los pies para correr y pasa el testigo al final de la zona, {jugador}. ¡Vamos a desenredar esto!", Expresion.DECIDIDO)
        ),
        "cap13_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "El testigo cruza la meta primero. El Enredo se deja caer en la pista, sin aire... y se ríe."),
            LineaDialogo(Hablante.ENREDO, "¡Qué carrera! Nunca nadie había jugado conmigo tanto rato. Tomen, se lo ganaron."),
            LineaDialogo(Hablante.NACHO, "¡El Hilo Morado! Ya están los cinco.", Expresion.FELIZ),
            LineaDialogo(Hablante.NARRADOR, "Rojo, azul, verde, amarillo y morado. Los cinco hilos se trenzan solos y forman una madeja que brilla como la Copa."),
            LineaDialogo(Hablante.ENREDO, "¿Entonces... ya se acabó el juego? Yo solo quería que alguien me persiguiera por el campus."),
            LineaDialogo(Hablante.RECUERDO, "Yo también estuve sola, enredada en mis propios recuerdos. Quédate con nosotros: siempre hay algo que ordenar."),
            LineaDialogo(Hablante.NACHO, "¡Y algo que jugar! Bienvenido, Enredo.", Expresion.FELIZ)
        ),
        ESCENA_EPILOGO_ACTO3 to listOf(
            LineaDialogo(Hablante.NARRADOR, "Desde ese día, la Madeja de la Memoria descansa junto a la Copa Restaurada."),
            LineaDialogo(Hablante.NACHO, "El Enredo ya no revuelve recuerdos: ahora los teje. Cada historia del campus tiene su propio hilo.", Expresion.FELIZ),
            LineaDialogo(Hablante.ENREDO, "¡Y cuando alguien se pierde en el campus, le tiro un hilito para que encuentre el camino!"),
            LineaDialogo(Hablante.RECUERDO, "La película, el agua, el ADN, el vitral y la carrera. Ninguno de esos recuerdos se va a perder."),
            LineaDialogo(Hablante.NACHO, "Gracias, {jugador}. Ahora eres Tejedor de la Memoria de la Ciudad Blanca. ¡Qué nota!", Expresion.FELIZ),
            LineaDialogo(Hablante.NARRADOR, "Todavía quedan rincones por descubrir, preguntas por responder y una trivia nueva cada día."),
            // Gancho del Acto IV (los edificios de trámites y oficinas)
            LineaDialogo(Hablante.NARRADOR, "Pero esa misma noche, en las oficinas del campus, un sello empezó a estampar solo: «PENDIENTE», «PENDIENTE», «PENDIENTE»..."),
            LineaDialogo(Hablante.NARRADOR, "Y a la mañana siguiente, todos los formularios de la universidad tenían una casilla nueva que nadie sabía llenar.")
        ),

        // ---------- Acto IV: El Sello Pendiente ----------
        "cap14_intro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "En la División de Registro, en el segundo piso del Polideportivo, la fila no avanza. Cada carné y cada recibo de matrícula sale con un sello rojo: PENDIENTE."),
            LineaDialogo(Hablante.NACHO, "¡{jugador}! Hasta mi carné dice «PENDIENTE». ¿Y esta casilla nueva? «Firma de quien guarda la memoria»... ¿Quién es ese?", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.ENREDO, "¡Esta vez no fui yo, lo juro por mis cinco hilos! Seguí el rastro de tinta roja y llega hasta el Edificio Uriel Gutiérrez."),
            LineaDialogo(Hablante.RECUERDO, "La sede administrativa de la universidad. Lleva el nombre de Uriel Gutiérrez, un estudiante que murió en 1954. Ahí se firma lo que hace que la universidad funcione."),
            LineaDialogo(Hablante.SELLO, "¡PENDIENTE! ¡PENDIENTE! ¡PENDIENTE! Si nada se termina... nada se acaba. ¡Y si nada se acaba, nadie se va!"),
            LineaDialogo(Hablante.NACHO, "Revolvió todos los trámites. Pon cada papel en su bandeja antes de que lo selle, {jugador}.", Expresion.DECIDIDO)
        ),
        "cap14_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "Las bandejas quedan en orden. Debajo de la última, aparece una hoja amarillenta con un sello de lacre: «Expediente de la Memoria. Folio 1»."),
            LineaDialogo(Hablante.SELLO, "¡Esa hoja no! Esa tiene la casilla que nadie sabe llenar... ¡y si alguien la llena, el expediente se cierra!"),
            LineaDialogo(Hablante.ENREDO, "Oye, Sello... ¿tú también tienes miedo de que el juego se acabe? Yo lo tenía."),
            LineaDialogo(Hablante.SELLO, "¡PENDIENTE!", Expresion.NORMAL),
            LineaDialogo(Hablante.RECUERDO, "Se fue saltando hacia el Archivo Central e Histórico. Si existe un expediente así de viejo, ahí deben estar los otros folios."),
            LineaDialogo(Hablante.NACHO, "¡El Folio del Expediente! Vamos al Archivo, {jugador}.", Expresion.FELIZ)
        ),
        "cap15_intro" to listOf(
            LineaDialogo(Hablante.NACHO, "El Archivo Central e Histórico. Aquí se guarda la memoria escrita de la universidad, con documentos desde su fundación, en 1867."),
            LineaDialogo(Hablante.NARRADOR, "Entre los estantes, en la penumbra, se encienden dos ojos grandes y dorados."),
            LineaDialogo(Hablante.BUHO, "Uuh, uuh. Por fin llega alguien que busca en vez de sellar. Bienvenidos. Yo cuido este archivo."),
            LineaDialogo(Hablante.NACHO, "¡Un búho! El búho es el símbolo de la universidad... y dicen que el plano de la Ciudad Universitaria tiene su forma.", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.BUHO, "Desde el aire, las alas son los edificios y la cabeza mira hacia la plaza. Llevo aquí desde que se trazó el campus, guardando lo que otros olvidan."),
            LineaDialogo(Hablante.BUHO, "El Sello revolvió mis estantes, pero las cajas siguen en orden de número. Abre la del medio: si el folio es menor, está antes; si es mayor, después. Así nunca te pierdes, {jugador}.")
        ),
        "cap15_outro" to listOf(
            LineaDialogo(Hablante.NARRADOR, "En la última caja, entre actas y fotografías viejas, brilla el segundo folio del Expediente de la Memoria."),
            LineaDialogo(Hablante.BUHO, "Este expediente lo empezó la universidad el día que abrió sus puertas. Cada generación le agrega una hoja. La casilla nueva pide la firma de quien guarda la memoria."),
            LineaDialogo(Hablante.NACHO, "¿Y no es usted quien la guarda?", Expresion.SORPRENDIDO),
            LineaDialogo(Hablante.BUHO, "Yo cuido el archivo. La memoria la guarda quien la vive. Ya lo verás."),
            LineaDialogo(Hablante.SELLO, "Pero si la firman... ¿se acaba todo?"),
            LineaDialogo(Hablante.BUHO, "Terminar no es olvidar, pequeño Sello. Un recuerdo cerrado se guarda para siempre; uno pendiente se pierde en un cajón."),
            LineaDialogo(Hablante.RECUERDO, "Los otros folios están entre periódicos viejos. Huele a tinta de imprenta... a la Hemeroteca."),
            LineaDialogo(Hablante.NACHO, "Pues a la Hemeroteca, {jugador}. ¡Con búho y todo!", Expresion.FELIZ)
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
