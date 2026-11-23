package com.unal.nachoquest.domain.model

/** Reto opcional de un lugar del campus que no es capítulo de la historia. */
data class RetoLugar(
    val puntoId: String,
    val nombre: String,
    val preguntas: List<Pregunta>
) {
    val retoId: String get() = Historia.retoIdParaPunto(puntoId)
}

/**
 * Retos de lugar: preguntas propias de edificios emblemáticos que no son capítulos.
 * Se juegan estando a menos de 75 m del edificio (o en el modo de pruebas), no exigen ítems
 * y solo dan XP. En cada partida se sortean [PREGUNTAS_POR_RETO] preguntas del banco del lugar.
 *
 * En las preguntas la respuesta correcta va primero (índice 0); la pantalla mezcla las opciones.
 */
object RetosLugares {

    const val XP_RECOMPENSA = 60
    const val MONEDAS_RECOMPENSA = 15
    const val PREGUNTAS_POR_RETO = 3

    val retos: List<RetoLugar> = listOf(
        RetoLugar(
            puntoId = "edif_207",
            nombre = "Museo de Arquitectura Leopoldo Rother",
            preguntas = listOf(
                p("rother_1", "¿De qué país llegó Leopoldo Rother, el arquitecto de la Ciudad Universitaria?",
                    "Alemania", "Italia", "España", "Francia"),
                p("rother_2", "¿Qué funcionaba en el edificio antes de que fuera el museo?",
                    "La Imprenta Universitaria", "La Facultad de Medicina", "La rectoría", "Un hospital"),
                p("rother_3", "¿Qué plaza de mercado, hoy monumento nacional, diseñó también Rother?",
                    "La de Girardot", "La de Paloquemao", "La de Bazurto", "La de Minorista"),
                p("rother_4", "¿Qué corriente de arquitectura marca los edificios que Rother hizo en el campus?",
                    "La arquitectura moderna", "El barroco", "El gótico", "La arquitectura colonial")
            )
        ),
        RetoLugar(
            puntoId = "edif_404",
            nombre = "Edificio Yu Takeuchi",
            preguntas = listOf(
                p("takeuchi_1", "¿De qué país era el profesor Yu Takeuchi?",
                    "Japón", "China", "Corea del Sur", "Vietnam"),
                p("takeuchi_2", "¿Qué departamentos funcionan en el edificio que lleva su nombre?",
                    "Matemáticas y Física", "Química y Biología", "Música y Artes", "Derecho y Economía"),
                p("takeuchi_3", "¿En qué facultad están Matemáticas y Física en la sede Bogotá?",
                    "Facultad de Ciencias", "Facultad de Ingeniería", "Facultad de Ciencias Humanas", "Facultad de Artes"),
                p("takeuchi_4", "Además de dar clase, ¿por qué es recordado Takeuchi entre los estudiantes?",
                    "Por sus libros de texto de matemáticas", "Por sus pinturas", "Por sus conciertos", "Por sus novelas")
            )
        ),
        RetoLugar(
            puntoId = "edif_571",
            nombre = "Hemeroteca Nacional Universitaria",
            preguntas = listOf(
                p("hemeroteca_1", "¿Qué guarda principalmente una hemeroteca?",
                    "Periódicos y revistas", "Plantas secas", "Fósiles", "Instrumentos musicales"),
                p("hemeroteca_2", "¿Qué cargo ocupó Carlos Lleras Restrepo, que da nombre a la Hemeroteca?",
                    "Presidente de Colombia", "Rector de la universidad", "Alcalde de Medellín", "Arzobispo de Bogotá"),
                p("hemeroteca_3", "¿En qué periodo gobernó Carlos Lleras Restrepo?",
                    "1966-1970", "1946-1950", "1982-1986", "1994-1998"),
                p("hemeroteca_4", "Si buscas la noticia original del Bogotazo de 1948, ¿dónde la encuentras?",
                    "En la Hemeroteca", "En el Herbario", "En el Observatorio", "En el Conservatorio")
            )
        ),
        RetoLugar(
            puntoId = "edif_413",
            nombre = "Observatorio Astronómico",
            preguntas = listOf(
                p("observatorio_1", "¿Quién fue el primer director del Observatorio Astronómico Nacional?",
                    "Francisco José de Caldas", "Julio Garavito", "Manuel Ancízar", "Agustín Codazzi"),
                p("observatorio_2", "¿Junto a qué edificio de Bogotá está la torre original del Observatorio, de 1803?",
                    "La Casa de Nariño", "El Capitolio Nacional", "La Catedral Primada", "El Museo Nacional"),
                p("observatorio_3", "¿Qué ingeniero, que hoy da nombre al edificio 401, dirigió el Observatorio?",
                    "Julio Garavito", "Leopoldo Rother", "Yu Takeuchi", "Rogelio Salmona"),
                p("observatorio_4", "¿Qué ciencia estudia los astros, las estrellas y los planetas?",
                    "La astronomía", "La astrología", "La geología", "La meteorología")
            )
        ),
        RetoLugar(
            puntoId = "edif_224",
            nombre = "Edificio Manuel Ancízar",
            preguntas = listOf(
                p("ancizar_1", "¿Qué cargo tuvo Manuel Ancízar en la Universidad Nacional?",
                    "Fue el primer rector", "Fue el primer decano de Medicina", "Diseñó el campus", "Fundó el Conservatorio"),
                p("ancizar_2", "¿Qué libro escribió Ancízar sobre sus viajes por el país?",
                    "Peregrinación de Alpha", "María", "La vorágine", "El carnero"),
                p("ancizar_3", "¿En qué expedición del siglo XIX que recorrió y describió el país participó Ancízar?",
                    "La Comisión Corográfica", "La Expedición Botánica", "La Expedición de Humboldt", "La Misión Kemmerer"),
                p("ancizar_4", "¿Qué otro oficio tuvo Manuel Ancízar?",
                    "Periodista", "Futbolista", "Pintor", "Músico")
            )
        ),
        RetoLugar(
            puntoId = "edif_621",
            nombre = "Instituto Geográfico Agustín Codazzi",
            preguntas = listOf(
                p("igac_1", "¿Qué produce el Instituto Geográfico Agustín Codazzi?",
                    "Los mapas oficiales de Colombia", "Las monedas", "Los pasaportes", "Los pronósticos del clima"),
                p("igac_2", "¿De qué país era el geógrafo Agustín Codazzi?",
                    "Italia", "Alemania", "Francia", "Venezuela"),
                p("igac_3", "¿Qué expedición dirigió Codazzi para describir el territorio nacional?",
                    "La Comisión Corográfica", "La Expedición Botánica", "La Misión Pedagógica Alemana", "La Expedición Forestal"),
                p("igac_4", "Además de los mapas, ¿qué otro registro lleva el IGAC?",
                    "El catastro de los predios", "El registro civil", "Las cédulas", "Los antecedentes judiciales")
            )
        ),
        RetoLugar(
            puntoId = "edif_225",
            nombre = "Edificio Rogelio Salmona",
            preguntas = listOf(
                p("salmona_1", "¿Qué material es la firma de los edificios de Rogelio Salmona?",
                    "El ladrillo", "El vidrio", "El acero", "La madera"),
                p("salmona_2", "¿Qué conjunto de vivienda junto a la Plaza de Toros de Bogotá diseñó Salmona?",
                    "Las Torres del Parque", "El Centro Andino", "Ciudad Salitre", "Las Torres Atrio"),
                p("salmona_3", "¿Con qué famoso arquitecto trabajó Salmona en París en su juventud?",
                    "Le Corbusier", "Frank Lloyd Wright", "Antoni Gaudí", "Oscar Niemeyer"),
                p("salmona_4", "¿Qué estudios se ofrecen en el edificio Salmona del campus?",
                    "Posgrados de Ciencias Humanas", "Pregrados de Ingeniería", "Medicina", "Odontología")
            )
        ),
        RetoLugar(
            puntoId = "edif_205",
            nombre = "Edificio de Sociología",
            preguntas = listOf(
                p("sociologia_1", "¿Qué método de investigación, en el que la comunidad investiga junto con el académico, impulsó Orlando Fals Borda?",
                    "La Investigación-Acción Participativa", "El método científico experimental", "El psicoanálisis", "La estadística bayesiana"),
                p("sociologia_2", "¿Qué libro de 1962, escrito en parte por Fals Borda, estudió la violencia en el país?",
                    "La violencia en Colombia", "Cien años de soledad", "El otoño del patriarca", "Historia de Colombia"),
                p("sociologia_3", "¿Qué oficio tenía Camilo Torres, cofundador de la carrera de Sociología?",
                    "Era sacerdote", "Era médico", "Era ingeniero", "Era músico"),
                p("sociologia_4", "¿En qué año se creó la carrera de Sociología en la Universidad Nacional?",
                    "1959", "1867", "1936", "1991")
            )
        ),
        RetoLugar(
            puntoId = "edif_861",
            nombre = "Edificio Uriel Gutiérrez",
            preguntas = listOf(
                p("uriel_1", "¿Quién fue Uriel Gutiérrez, que da nombre a este edificio?",
                    "Un estudiante asesinado en el campus", "Un rector de la universidad", "Un arquitecto", "Un presidente de Colombia"),
                p("uriel_2", "¿En qué año murió Uriel Gutiérrez?",
                    "1954", "1929", "1948", "1971"),
                p("uriel_3", "¿Quién gobernaba Colombia cuando murió Uriel Gutiérrez?",
                    "Gustavo Rojas Pinilla", "Alfonso López Pumarejo", "Carlos Lleras Restrepo", "Laureano Gómez"),
                p("uriel_4", "¿Qué estudiante murió el 8 de junio de 1929, la otra fecha que recuerda el Día del Estudiante Caído?",
                    "Gonzalo Bravo Pérez", "Camilo Torres", "Jorge Eliécer Gaitán", "Manuel Ancízar")
            )
        ),
        RetoLugar(
            puntoId = "edif_305",
            nombre = "Conservatorio de Música",
            preguntas = listOf(
                p("conservatorio_1", "¿Quién fue el primer director del Conservatorio Nacional de Música, creado en 1910?",
                    "Guillermo Uribe Holguín", "León de Greiff", "Lucho Bermúdez", "José Barros"),
                p("conservatorio_2", "¿Cuántas líneas tiene un pentagrama?",
                    "Cinco", "Cuatro", "Seis", "Siete"),
                p("conservatorio_3", "¿Qué orquesta ha tenido como casa histórica el Auditorio León de Greiff?",
                    "La Orquesta Filarmónica de Bogotá", "La Orquesta Sinfónica de Londres", "La Sonora Matancera", "La Billo's Caracas Boys"),
                p("conservatorio_4", "¿A qué facultad pertenece el Conservatorio de Música?",
                    "Facultad de Artes", "Facultad de Ciencias", "Facultad de Ingeniería", "Facultad de Ciencias Humanas")
            )
        ),
        RetoLugar(
            puntoId = "edif_301",
            nombre = "Facultad de Artes",
            preguntas = listOf(
                p("artes_1", "¿Qué escuela, fundada en 1886 por Alberto Urdaneta, es antecesora de la Facultad de Artes?",
                    "La Escuela de Bellas Artes", "La Escuela de Minas", "La Escuela de Jurisprudencia", "La Escuela Militar"),
                p("artes_2", "¿Cuál de estas carreras se estudia en la Facultad de Artes?",
                    "Arquitectura", "Medicina", "Contaduría", "Química"),
                p("artes_3", "¿Cuál de estas carreras NO pertenece a la Facultad de Artes?",
                    "Odontología", "Diseño Industrial", "Música", "Cine y Televisión"),
                p("artes_4", "¿De qué ciudad es el pintor Fernando Botero, famoso por sus figuras voluminosas?",
                    "Medellín", "Cali", "Bogotá", "Barranquilla")
            )
        ),
        RetoLugar(
            puntoId = "edif_425",
            nombre = "Instituto de Ciencias Naturales",
            preguntas = listOf(
                p("icn_1", "¿Qué es un herbario?",
                    "Una colección de plantas secas", "Un huerto de plantas medicinales", "Una tienda de hierbas", "Un invernadero"),
                p("icn_2", "¿Qué botánico fundó el Herbario Nacional Colombiano?",
                    "Enrique Pérez Arbeláez", "José Celestino Mutis", "Francisco José de Caldas", "Alexander von Humboldt"),
                p("icn_3", "¿Qué jardín de Bogotá fundó también Enrique Pérez Arbeláez?",
                    "El Jardín Botánico José Celestino Mutis", "El Parque Simón Bolívar", "El Parque de la 93", "El Parque Nacional"),
                p("icn_4", "Colombia es el país con más especies de...",
                    "Aves", "Pingüinos", "Canguros", "Camellos")
            )
        ),
        RetoLugar(
            puntoId = "edif_201",
            nombre = "Facultad de Derecho y Ciencias Políticas",
            preguntas = listOf(
                p("derecho_1", "¿Qué líder político, asesinado el 9 de abril de 1948, estudió Derecho en la Universidad Nacional?",
                    "Jorge Eliécer Gaitán", "Laureano Gómez", "Alfonso López Pumarejo", "Rafael Uribe Uribe"),
                p("derecho_2", "¿Cómo se llamaba la escuela de Derecho cuando nació la universidad en 1867?",
                    "Escuela de Jurisprudencia", "Escuela de Leyes y Cánones", "Colegio de Abogados", "Escuela de Gobierno"),
                p("derecho_3", "¿De qué año es la Constitución Política que rige hoy en Colombia?",
                    "1991", "1886", "1863", "2001"),
                p("derecho_4", "¿Qué mecanismo permite a cualquier persona pedir a un juez que proteja sus derechos fundamentales?",
                    "La acción de tutela", "El referendo", "La moción de censura", "El habeas data")
            )
        ),
        RetoLugar(
            puntoId = "edif_471",
            nombre = "Facultad de Medicina",
            preguntas = listOf(
                p("medicina_1", "¿Qué hospital histórico de Bogotá fue durante décadas el centro de práctica de los estudiantes de Medicina?",
                    "El Hospital San Juan de Dios", "La Clínica del Country", "El Hospital Militar", "La Fundación Santa Fe"),
                p("medicina_2", "¿Cómo se llama el hospital propio de la universidad?",
                    "Hospital Universitario Nacional", "Hospital de la Nacho", "Clínica Ciudad Blanca", "Hospital Rother"),
                p("medicina_3", "¿Qué juramento hacen los médicos al graduarse?",
                    "El juramento hipocrático", "El juramento de Bolonia", "El juramento de Galeno", "El juramento de Asclepio"),
                p("medicina_4", "¿Contra qué enfermedad desarrolló Manuel Elkin Patarroyo una vacuna sintética?",
                    "La malaria", "La gripe", "El sarampión", "La viruela")
            )
        ),
        RetoLugar(
            puntoId = "edif_217",
            nombre = "Edificio Francisco de Paula Santander",
            preguntas = listOf(
                p("santander_1", "¿Con qué apodo se conoce a Francisco de Paula Santander?",
                    "El Hombre de las Leyes", "El Libertador", "El Precursor", "El Sabio"),
                p("santander_2", "¿Qué cargo tuvo Santander en la Gran Colombia mientras Bolívar dirigía las campañas?",
                    "Vicepresidente", "Arzobispo", "Embajador en Francia", "Alcalde de Caracas"),
                p("santander_3", "¿Qué plaza del campus lleva oficialmente el nombre de Santander?",
                    "La Plaza Che", "La plaza del Estadio", "La plaza de Veterinaria", "La plaza de Artes"),
                p("santander_4", "¿En qué departamento de hoy nació Santander?",
                    "Norte de Santander", "Santander", "Boyacá", "Cundinamarca")
            )
        ),
        RetoLugar(
            puntoId = "edif_214",
            nombre = "Edificio Antonio Nariño",
            preguntas = listOf(
                p("narino_1", "¿Qué documento tradujo e imprimió Antonio Nariño en 1793, lo que le costó la cárcel?",
                    "Los Derechos del Hombre y del Ciudadano", "La Constitución de Estados Unidos", "La Biblia", "El Contrato Social"),
                p("narino_2", "¿Con qué apodo se conoce a Antonio Nariño?",
                    "El Precursor", "El Libertador", "El Hombre de las Leyes", "El Sabio"),
                p("narino_3", "¿Qué edificio de Bogotá, sede de la Presidencia, lleva su nombre?",
                    "La Casa de Nariño", "El Palacio Liévano", "El Capitolio Nacional", "El Palacio de San Carlos"),
                p("narino_4", "¿Qué departamento del sur del país lleva el nombre de Nariño?",
                    "Nariño", "Putumayo", "Cauca", "Huila")
            )
        ),
        RetoLugar(
            puntoId = "edif_631",
            nombre = "Servicio Geológico Colombiano",
            preguntas = listOf(
                p("sgc_1", "¿Cómo se llamaba antes el Servicio Geológico Colombiano?",
                    "Ingeominas", "Ideam", "Invías", "Ingetec"),
                p("sgc_2", "¿Qué fenómenos vigila el Servicio Geológico Colombiano?",
                    "Volcanes y sismos", "Huracanes y tornados", "Mareas y oleaje", "Lluvias y sequías"),
                p("sgc_3", "¿Qué volcán hizo erupción en 1985 y causó la tragedia de Armero?",
                    "El Nevado del Ruiz", "El Galeras", "El Puracé", "El Nevado del Huila"),
                p("sgc_4", "¿Qué ciencia estudia las rocas y la estructura de la Tierra?",
                    "La geología", "La geografía", "La ecología", "La astronomía")
            )
        )
    )

    private val porPuntoId: Map<String, RetoLugar> = retos.associateBy { it.puntoId }

    fun porPunto(puntoId: String): RetoLugar? = porPuntoId[puntoId]

    fun tieneReto(puntoId: String): Boolean = puntoId in porPuntoId

    private fun p(id: String, enunciado: String, correcta: String, vararg incorrectas: String) =
        Pregunta(id = id, enunciado = enunciado, opciones = listOf(correcta) + incorrectas, indiceCorrecta = 0)
}
