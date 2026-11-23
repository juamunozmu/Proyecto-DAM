package com.unal.nachoquest.domain.model

import java.time.LocalDate
import kotlin.random.Random

/**
 * Trivia del día: cinco preguntas sobre la universidad que cambian cada día y son las mismas
 * para todos los jugadores. Jugarla en días seguidos alarga la racha, que da XP extra.
 */
object TriviaDiaria {

    /** Id de "punto" con el que se abre la pantalla de preguntas en modo trivia. */
    const val PUNTO_ID = "trivia_diaria"
    const val RETO_ID = "reto_trivia_diaria"

    const val PREGUNTAS_POR_DIA = 5
    const val XP_BASE = 30
    const val XP_POR_DIA_DE_RACHA = 5
    const val BONO_RACHA_MAXIMO = 25

    /** Día actual como número de días desde 1970, según el calendario del teléfono. */
    fun diaActual(): Long = LocalDate.now().toEpochDay()

    /** Las preguntas dependen solo del día, así que todos los jugadores reciben las mismas. */
    fun preguntasDelDia(dia: Long): List<Pregunta> =
        banco.shuffled(Random(dia)).take(PREGUNTAS_POR_DIA)

    fun retoDelDia(dia: Long, rachaActual: Int): RetoQuiz = RetoQuiz(
        id = RETO_ID,
        puntoInteresId = PUNTO_ID,
        titulo = "Trivia del día",
        descripcion = "Cinco preguntas nuevas cada día sobre la Universidad Nacional.",
        preguntas = preguntasDelDia(dia),
        recompensaXp = XP_BASE + bonoRacha(rachaActual),
        recompensaMonedas = 0
    )

    fun yaJugoHoy(perfil: UserProfile, hoy: Long): Boolean = perfil.ultimoDiaTrivia == hoy

    /** Racha que sigue viva hoy: se pierde si pasó un día entero sin jugar. */
    fun rachaVigente(perfil: UserProfile, hoy: Long): Int =
        if (perfil.ultimoDiaTrivia >= hoy - 1) perfil.rachaDias else 0

    /** Racha que queda después de superar la trivia de hoy. */
    fun rachaTrasJugar(perfil: UserProfile, hoy: Long): Int = when (perfil.ultimoDiaTrivia) {
        hoy -> perfil.rachaDias
        hoy - 1 -> perfil.rachaDias + 1
        else -> 1
    }

    /** XP extra por la racha con la que se juega (el primer día no da bono). */
    fun bonoRacha(racha: Int): Int =
        ((racha - 1) * XP_POR_DIA_DE_RACHA).coerceIn(0, BONO_RACHA_MAXIMO)

    val banco: List<Pregunta> = listOf(
        Pregunta("td_fundacion", "¿En qué año fue fundada la Universidad Nacional de Colombia?",
            listOf("1867", "1810", "1886", "1936"), 0),
        Pregunta("td_nombre_original", "¿Con qué nombre nació la universidad?",
            listOf("Universidad Nacional de los Estados Unidos de Colombia", "Universidad Central de Bogotá", "Real Universidad de Santafé", "Universidad de la Gran Colombia"), 0),
        Pregunta("td_primer_rector", "¿Quién fue el primer rector de la universidad?",
            listOf("Manuel Ancízar", "Santos Acosta", "José Celestino Mutis", "Rafael Núñez"), 0),
        Pregunta("td_rother", "¿Qué arquitecto alemán diseñó la Ciudad Universitaria de Bogotá?",
            listOf("Leopoldo Rother", "Le Corbusier", "Karl Brunner", "Rogelio Salmona"), 0),
        Pregunta("td_karsen", "¿Qué pedagogo alemán ideó, junto con Rother, la organización del campus?",
            listOf("Fritz Karsen", "Walter Gropius", "Alexander von Humboldt", "Friedrich Fröbel"), 0),
        Pregunta("td_lopez", "¿Qué presidente impulsó la construcción de la Ciudad Universitaria en los años treinta?",
            listOf("Alfonso López Pumarejo", "Enrique Olaya Herrera", "Eduardo Santos", "Laureano Gómez"), 0),
        Pregunta("td_ciudad_blanca", "¿Cómo se le dice al campus de Bogotá por el color de sus edificios?",
            listOf("La Ciudad Blanca", "La Ciudad Jardín", "La Ciudad Luz", "La Atenas Suramericana"), 0),
        Pregunta("td_lema", "¿Cuál es el lema en latín del escudo de la universidad?",
            listOf("Inter Aulas Academiæ Quære Verum", "Veritas liberabit vos", "Sapientia et virtus", "Ad astra per aspera"), 0),
        Pregunta("td_apodo", "¿Cuál es el apodo cariñoso de la Universidad Nacional?",
            listOf("La Nacho", "La Blanca", "La Central", "La Uni"), 0),
        Pregunta("td_primiparo", "¿Cómo se le dice a un estudiante de primer semestre?",
            listOf("Primíparo", "Novato", "Pichón", "Becario"), 0),
        Pregunta("td_gabo", "¿Qué estudió Gabriel García Márquez en la Universidad Nacional?",
            listOf("Derecho", "Periodismo", "Filosofía", "Literatura"), 0),
        Pregunta("td_takeuchi", "¿En qué área enseñó el profesor japonés Yu Takeuchi durante décadas?",
            listOf("Matemáticas", "Arquitectura", "Medicina", "Música"), 0),
        Pregunta("td_garavito_luna", "¿Qué lleva el nombre del astrónomo Julio Garavito fuera de la Tierra?",
            listOf("Un cráter de la Luna", "Un anillo de Saturno", "Un cometa", "Una luna de Júpiter"), 0),
        Pregunta("td_sociologia", "¿Qué facultad ayudaron a fundar Orlando Fals Borda y Camilo Torres en 1959?",
            listOf("Sociología", "Economía", "Psicología", "Antropología"), 0),
        Pregunta("td_observatorio", "El Observatorio Astronómico Nacional, a cargo de la universidad, fue fundado en...",
            listOf("1803", "1867", "1910", "1936"), 0),
        Pregunta("td_mutis", "¿Quién promovió la creación del Observatorio Astronómico Nacional?",
            listOf("José Celestino Mutis", "Francisco José de Caldas", "Antonio Nariño", "Julio Garavito"), 0),
        Pregunta("td_estadio", "¿Para qué evento se inauguró el Estadio Alfonso López en 1938?",
            listOf("Los Juegos Bolivarianos", "Un Mundial de fútbol", "Los Juegos Olímpicos", "La Copa América"), 0),
        Pregunta("td_sedes", "¿Cuántas sedes tiene la Universidad Nacional en el país?",
            listOf("9", "4", "6", "12"), 0),
        Pregunta("td_amazonia", "¿En qué ciudad queda la sede Amazonia?",
            listOf("Leticia", "Mocoa", "Florencia", "Mitú"), 0),
        Pregunta("td_caribe", "¿Dónde queda la sede Caribe?",
            listOf("San Andrés", "Cartagena", "Santa Marta", "Barranquilla"), 0),
        Pregunta("td_orinoquia", "¿En qué ciudad queda la sede Orinoquia?",
            listOf("Arauca", "Yopal", "Villavicencio", "Puerto Carreño"), 0),
        Pregunta("td_minas", "¿En qué sede está la Facultad de Minas?",
            listOf("Medellín", "Manizales", "Bogotá", "Palmira"), 0),
        Pregunta("td_museo_rother", "¿Qué museo funciona en el antiguo edificio de la Imprenta?",
            listOf("El Museo de Arquitectura Leopoldo Rother", "El Museo de Historia Natural", "El Museo de la Ciencia y el Juego", "El Museo de Arte"), 0),
        Pregunta("td_salmona", "¿Qué arquitecto diseñó el edificio de Posgrados de Ciencias Humanas?",
            listOf("Rogelio Salmona", "Leopoldo Rother", "Bruno Violi", "Germán Samper"), 0),
        Pregunta("td_herbario", "¿Qué colección guarda el Instituto de Ciencias Naturales?",
            listOf("El Herbario Nacional Colombiano", "El Archivo General de la Nación", "La Biblioteca Luis Ángel Arango", "El Museo del Oro"), 0),
        Pregunta("td_estudiante_caido", "¿Qué se conmemora en la universidad el 8 y 9 de junio?",
            listOf("El Día del Estudiante Caído", "La fundación de la universidad", "El Día del Profesor", "La inauguración del campus"), 0),
        Pregunta("td_vacas", "¿Qué animales se ven pastando en los potreros de Veterinaria?",
            listOf("Vacas", "Llamas", "Avestruces", "Venados"), 0),
        Pregunta("td_radio", "¿Cómo se llama la emisora de la universidad?",
            listOf("Radio UNAL", "Radio Nacional", "Radiónica", "Javeriana Estéreo"), 0),
        Pregunta("td_greiff_oficio", "¿A qué se dedicaba León de Greiff, que da nombre al auditorio?",
            listOf("Era poeta", "Era pintor", "Era arquitecto", "Era físico"), 0),
        Pregunta("td_transmilenio", "¿Por qué avenida pasa la estación de TransMilenio «Universidad Nacional»?",
            listOf("La NQS (carrera 30)", "La avenida Caracas", "La autopista Norte", "La avenida Boyacá"), 0),
        Pregunta("td_ley_66", "¿Qué ley creó la Universidad Nacional en 1867?",
            listOf("La Ley 66", "La Ley 30", "La Ley 100", "La Ley 1"), 0),
        Pregunta("td_santos_acosta", "¿Qué presidente sancionó la ley que creó la universidad?",
            listOf("Santos Acosta", "Tomás Cipriano de Mosquera", "Rafael Núñez", "Manuel Murillo Toro"), 0),
        Pregunta("td_150_anos", "¿En qué año cumplió la universidad 150 años?",
            listOf("2017", "2010", "2000", "2021"), 0),
        Pregunta("td_publica_grande", "¿Qué lugar ocupa la Nacional entre las universidades públicas del país por su tamaño?",
            listOf("Es la más grande", "Es la más pequeña", "Es la segunda", "Es la quinta"), 0),
        Pregunta("td_sede_la_paz", "¿En qué departamento queda la sede La Paz?",
            listOf("Cesar", "Bolívar", "Magdalena", "La Guajira"), 0),
        Pregunta("td_sede_tumaco", "¿En qué departamento queda la sede Tumaco?",
            listOf("Nariño", "Cauca", "Valle del Cauca", "Chocó"), 0),
        Pregunta("td_sede_palmira", "¿En qué departamento queda la sede Palmira?",
            listOf("Valle del Cauca", "Tolima", "Huila", "Risaralda"), 0),
        Pregunta("td_peama", "¿Qué programa permite empezar la carrera en una sede de frontera y terminarla en otra sede?",
            listOf("El PEAMA", "El ICETEX", "El SENA", "Ser Pilo Paga"), 0),
        Pregunta("td_un_periodico", "¿Cómo se llama el periódico de la universidad?",
            listOf("UN Periódico", "El Tiempo", "El Espectador", "La Nacho Hoy"), 0),
        Pregunta("td_calle_26", "¿Qué avenida bordea el campus por el sur?",
            listOf("La calle 26 (avenida El Dorado)", "La calle 72", "La avenida Primero de Mayo", "La calle 100"), 0),
        Pregunta("td_estacion_cu", "¿Cómo se llama la estación de TransMilenio de la calle 26 junto al campus?",
            listOf("Ciudad Universitaria", "Campín", "Salitre", "Concejo de Bogotá"), 0),
        Pregunta("td_garavito_billete", "¿En qué billete colombiano apareció el ingeniero Julio Garavito?",
            listOf("En el de 20.000 pesos", "En el de 1.000 pesos", "En el de 100.000 pesos", "En el de 5.000 pesos"), 0),
        Pregunta("td_virginia_gutierrez", "¿Qué antropóloga de la universidad aparece en el billete de 10.000 pesos?",
            listOf("Virginia Gutiérrez de Pineda", "Débora Arango", "Policarpa Salavarrieta", "Gabriela Mistral"), 0),
        Pregunta("td_patarroyo", "¿Contra qué enfermedad buscó una vacuna el científico Manuel Elkin Patarroyo?",
            listOf("La malaria", "La tuberculosis", "El dengue", "La rabia"), 0),
        Pregunta("td_mockus", "¿Qué exrector de la universidad fue después alcalde de Bogotá?",
            listOf("Antanas Mockus", "Enrique Peñalosa", "Gustavo Petro", "Luis Eduardo Garzón"), 0),
        Pregunta("td_nobel_gabo", "¿En qué año recibió Gabriel García Márquez el Premio Nobel de Literatura?",
            listOf("1982", "1967", "1975", "1990"), 0),
        Pregunta("td_cien_anos", "¿En qué año se publicó «Cien años de soledad»?",
            listOf("1967", "1955", "1982", "1985"), 0),
        Pregunta("td_bogotazo", "¿Qué fecha se recuerda como el Bogotazo?",
            listOf("9 de abril de 1948", "20 de julio de 1810", "7 de agosto de 1819", "6 de noviembre de 1985"), 0),
        Pregunta("td_che", "¿De qué país era el Che Guevara, cuyo rostro está pintado en la plaza central?",
            listOf("Argentina", "Cuba", "México", "Bolivia"), 0),
        Pregunta("td_altura_bogota", "¿A qué altura aproximada sobre el nivel del mar está Bogotá?",
            listOf("2.600 metros", "1.500 metros", "3.500 metros", "800 metros"), 0)
    )
}
