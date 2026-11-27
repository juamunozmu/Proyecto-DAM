package com.unal.nachoquest.domain.model

/**
 * Banco de preguntas de cada reto. Los capítulos de la historia tienen su propio reto;
 * los edificios emblemáticos tienen un reto de lugar (ver [RetosLugares]) y el resto usa un
 * reto genérico sobre el campus.
 */
object BancoRetos {

    fun retoParaPunto(puntoId: String): RetoQuiz {
        return when (Historia.capituloPorPunto(puntoId)?.retoId) {
            "reto_plaza_che" -> RetoQuiz(
                id = "reto_plaza_che",
                puntoInteresId = puntoId,
                titulo = "Capítulo 1: Misterios de la Plaza Che",
                descripcion = "Descubre la historia del corazón estudiantil y político de la UNAL.",
                preguntas = listOf(
                    Pregunta(
                        id = "che_1",
                        // La estatua ya no está: la derribaron en 1976
                        enunciado = "¿De qué prócer era la estatua que había en la plaza y que le da su nombre oficial?",
                        opciones = listOf("Simón Bolívar", "Francisco de Paula Santander", "Antonio Nariño", "Camilo Torres"),
                        indiceCorrecta = 1
                    ),
                    Pregunta(
                        id = "che_2",
                        // El rostro está en el muro occidental del auditorio, el que mira a la plaza. No se
                        // pregunta el año: las fuentes no coinciden (entre 1976 y 1982)
                        enunciado = "¿En qué edificio se pintó el rostro del Che que le dio nombre a la plaza?",
                        opciones = listOf("Biblioteca Central", "Auditorio León de Greiff", "Edificio de Sociología", "Torre de Enfermería"),
                        indiceCorrecta = 1
                    ),
                    Pregunta(
                        id = "che_3",
                        // El Auditorio León de Greiff queda al oriente de la plaza y la biblioteca al occidente
                        enunciado = "¿Qué edificio está en el costado occidental de la plaza?",
                        opciones = listOf("Biblioteca Central Gabriel García Márquez", "Facultad de Artes", "Conservatorio de Música", "Concha Acústica"),
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
                        enunciado = "¿Qué ha pasado varias veces con el rostro del Che en la plaza?",
                        opciones = listOf(
                            "Lo han borrado y lo han vuelto a pintar",
                            "Lo trasladaron a un museo",
                            "Lo convirtieron en una estatua",
                            "Nunca se ha tocado desde que se pintó"
                        ),
                        indiceCorrecta = 0
                    )
                ),
                recompensaXp = 100,
                recompensaMonedas = 20,
                itemRecompensaId = "carnet_antiguo",
                insigniaRecompensaId = "insignia_che",
                coleccionableRecompensaId = "col_afrecho"
            )
            "reto_biblio" -> RetoQuiz(
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
            "reto_cyt" -> RetoQuiz(
                id = "reto_cyt",
                puntoInteresId = puntoId,
                titulo = "Capítulo 3: La Ecuación Perdida",
                descripcion = "Demuestra tu ingenio en la casa de la Ingeniería.",
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
            "reto_greiff" -> RetoQuiz(
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
            "reto_estadio" -> RetoQuiz(
                id = "reto_estadio",
                puntoInteresId = puntoId,
                titulo = "Capítulo 5: La tanda final",
                descripcion = "Demuestra cuánto sabes del estadio y del deporte en la Ciudad Universitaria.",
                preguntas = listOf(
                    Pregunta(
                        id = "est_1",
                        enunciado = "¿En qué año se inauguró el Estadio Alfonso López?",
                        opciones = listOf("1938", "1948", "1967", "1925"),
                        indiceCorrecta = 0
                    ),
                    Pregunta(
                        id = "est_2",
                        enunciado = "¿Qué evento deportivo estrenó el estadio?",
                        opciones = listOf("Los primeros Juegos Bolivarianos", "Un Mundial de fútbol", "Los Juegos Olímpicos", "La primera Copa América"),
                        indiceCorrecta = 0
                    ),
                    Pregunta(
                        id = "est_3",
                        enunciado = "¿Qué arquitecto diseñó el estadio?",
                        opciones = listOf("Leopoldo Rother", "Rogelio Salmona", "Fritz Karsen", "Eugenia Mantilla de Cardoso"),
                        indiceCorrecta = 0
                    ),
                    Pregunta(
                        id = "est_4",
                        enunciado = "¿A quién rinde homenaje el nombre del estadio?",
                        opciones = listOf(
                            "Al presidente que impulsó la Ciudad Universitaria",
                            "Al primer rector de la universidad",
                            "A un goleador histórico de la selección",
                            "Al arquitecto que trazó el campus"
                        ),
                        indiceCorrecta = 0
                    ),
                    Pregunta(
                        id = "est_5",
                        enunciado = "¿Qué celebraba Bogotá el año en que se inauguró el estadio?",
                        opciones = listOf("Sus 400 años de fundada", "Los 100 años de la independencia", "Sus 200 años de fundada", "Sus 50 años como capital"),
                        indiceCorrecta = 0
                    )
                ),
                recompensaXp = 250,
                recompensaMonedas = 50,
                itemRecompensaId = "copa_memoria",
                insigniaRecompensaId = "insignia_estadio",
                coleccionableRecompensaId = "col_camiseta"
            )
            // ---------- Acto II (la respuesta correcta va primero; la pantalla mezcla las opciones) ----------
            "reto_museo" -> RetoQuiz(
                id = "reto_museo",
                puntoInteresId = puntoId,
                titulo = "Capítulo 6: Colores Robados",
                descripcion = "El arte colombiano que el Eco quiso borrar.",
                preguntas = listOf(
                    Pregunta("museo_1", "¿Qué artista colombiana abrió una enorme grieta en el piso de la galería Tate Modern de Londres en 2007?",
                        listOf("Doris Salcedo", "Débora Arango", "Beatriz González", "Ana Mercedes Hoyos"), 0),
                    Pregunta("museo_2", "¿Qué ave aparece una y otra vez en las pinturas de Alejandro Obregón?",
                        listOf("El cóndor", "El colibrí", "La guacamaya", "El tucán"), 0),
                    Pregunta("museo_3", "¿En qué municipio del Valle del Cauca está el museo del pintor Omar Rayo?",
                        listOf("Roldanillo", "Buga", "Tuluá", "Cartago"), 0),
                    Pregunta("museo_4", "¿Qué escultor de Popayán es famoso por sus esculturas de metal pintado?",
                        listOf("Edgar Negret", "Fernando Botero", "Rodrigo Arenas Betancourt", "Eduardo Ramírez Villamizar"), 0),
                    Pregunta("museo_5", "¿En qué ciudad nació la pintora Débora Arango?",
                        listOf("Medellín", "Bogotá", "Cali", "Cartagena"), 0)
                ),
                recompensaXp = 280,
                recompensaMonedas = 55,
                itemRecompensaId = "pincel_restaurador",
                insigniaRecompensaId = "insignia_museo",
                coleccionableRecompensaId = "col_paleta"
            )
            "reto_potrero" -> RetoQuiz(
                id = "reto_potrero",
                puntoInteresId = puntoId,
                titulo = "Capítulo 7: El Rebaño Perdido",
                descripcion = "Lo que se aprende entre potreros, establos y clínicas.",
                preguntas = listOf(
                    Pregunta("potrero_1", "¿Qué estudia la zootecnia?",
                        listOf("La producción y la cría de animales", "Los fósiles de animales", "El comportamiento de los insectos", "Las plantas medicinales"), 0),
                    Pregunta("potrero_2", "¿Cómo se le dice a la cría de la vaca?",
                        listOf("Ternero", "Potro", "Cordero", "Lechón"), 0),
                    Pregunta("potrero_3", "¿Cuál de estas es una raza de ganado criollo colombiano?",
                        listOf("Blanco orejinegro", "Holstein", "Angus", "Jersey"), 0),
                    Pregunta("potrero_4", "¿Contra qué enfermedad desarrolló Louis Pasteur una vacuna en 1885?",
                        listOf("La rabia", "La viruela", "La fiebre amarilla", "La polio"), 0),
                    Pregunta("potrero_5", "¿Cuál es el ave nacional de Colombia?",
                        listOf("El cóndor de los Andes", "El águila arpía", "El gallito de roca", "La guacamaya roja"), 0)
                ),
                recompensaXp = 320,
                recompensaMonedas = 60,
                itemRecompensaId = "cencerro_memoria",
                insigniaRecompensaId = "insignia_potrero",
                coleccionableRecompensaId = "col_leche"
            )
            "reto_concha" -> RetoQuiz(
                id = "reto_concha",
                puntoInteresId = puntoId,
                titulo = "Capítulo 8: La Voz del Campus",
                descripcion = "La música que el último Eco quiso callar.",
                preguntas = listOf(
                    Pregunta("concha_1", "¿Para qué sirve la forma curva de una concha acústica?",
                        listOf("Para devolver el sonido hacia el público", "Para proteger del sol", "Para que quepa más gente", "Para apagar el eco"), 0),
                    Pregunta("concha_2", "¿Qué ritmo de la costa Caribe se toca con acordeón, caja y guacharaca?",
                        listOf("El vallenato", "El bambuco", "El joropo", "El currulao"), 0),
                    Pregunta("concha_3", "¿En qué ciudad se celebra el Festival de la Leyenda Vallenata?",
                        listOf("Valledupar", "Barranquilla", "Santa Marta", "Riohacha"), 0),
                    Pregunta("concha_4", "¿Qué festival gratuito de rock se celebra cada año en Bogotá?",
                        listOf("Rock al Parque", "Estéreo Picnic", "Altavoz", "Lollapalooza"), 0),
                    Pregunta("concha_5", "¿Qué instrumento de viento de origen indígena acompaña a la cumbia?",
                        listOf("La gaita", "La quena", "El saxofón", "La armónica"), 0)
                ),
                recompensaXp = 400,
                recompensaMonedas = 80,
                itemRecompensaId = "copa_restaurada",
                insigniaRecompensaId = "insignia_concha",
                coleccionableRecompensaId = "col_afiche"
            )
            // ---------- Acto III ----------
            // Datos verificados en noviembre de 2026: el traslado de la escuela (Periódico UNAL,
            // MinEducación), los 24 fotogramas y la claqueta, los Lumière (28 dic. 1895) y la
            // nominación de El abrazo de la serpiente (Óscar 2016)
            "reto_cine" -> RetoQuiz(
                id = "reto_cine",
                puntoInteresId = puntoId,
                titulo = "Capítulo 9: Fuera de Cuadro",
                descripcion = "Lo que hay detrás de cada película.",
                preguntas = listOf(
                    Pregunta("cine_1", "¿Dónde funcionó cerca de 30 años la Escuela de Cine y Televisión de la UNAL, antes de pasar al nuevo edificio de Artes?",
                        listOf("En los camerinos del Estadio Alfonso López", "En el sótano de la Biblioteca Central", "En el Auditorio León de Greiff", "En el Museo de Arte"), 0),
                    Pregunta("cine_2", "¿Cuántos fotogramas por segundo se volvieron el estándar del cine cuando llegó el sonido?",
                        listOf("24", "12", "60", "100"), 0),
                    Pregunta("cine_3", "¿Para qué sirve la claqueta que se cierra antes de cada toma?",
                        listOf("Para sincronizar la imagen con el sonido e identificar la toma", "Para pedirle silencio al público", "Para encender las luces del set", "Para avisar que se acabó el rollo"), 0),
                    Pregunta("cine_4", "¿Qué hermanos hicieron en París, en 1895, la primera proyección pública de cine?",
                        listOf("Los hermanos Lumière", "Los hermanos Wright", "Los hermanos Grimm", "Los hermanos Marx"), 0),
                    Pregunta("cine_5", "¿Qué película de Ciro Guerra le dio a Colombia su primera nominación al Óscar a mejor película de habla no inglesa?",
                        listOf("El abrazo de la serpiente", "Pájaros de verano", "La vendedora de rosas", "María llena eres de gracia"), 0)
                ),
                recompensaXp = 300,
                recompensaMonedas = 65,
                itemRecompensaId = "hilo_rojo",
                insigniaRecompensaId = "insignia_cine",
                coleccionableRecompensaId = "col_rollo"
            )
            // Datos verificados en noviembre de 2026: Chingaza aporta cerca del 70 % del agua de
            // Bogotá (Acueducto); el agua hierve a menos de 100 °C en la altura (menor presión)
            "reto_hidraulica" -> RetoQuiz(
                id = "reto_hidraulica",
                puntoInteresId = puntoId,
                titulo = "Capítulo 10: Agua Revuelta",
                descripcion = "El agua, medida gota a gota.",
                preguntas = listOf(
                    Pregunta("hidraulica_1", "¿Para qué sirve construir un modelo a escala en un laboratorio de hidráulica?",
                        listOf("Para probar cómo se portará el agua en un río, una presa o un puerto antes de construirlos", "Para guardar agua lluvia", "Para criar peces", "Para decorar el laboratorio"), 0),
                    Pregunta("hidraulica_2", "¿Qué sistema, en los páramos del oriente, aporta cerca del 70 % del agua de Bogotá?",
                        listOf("El Sistema Chingaza", "El río Bogotá", "La laguna de Tota", "El embalse del Guavio"), 0),
                    Pregunta("hidraulica_3", "¿Qué sabio griego descubrió que un cuerpo sumergido recibe un empuje igual al peso del agua que desaloja?",
                        listOf("Arquímedes", "Pitágoras", "Aristóteles", "Euclides"), 0),
                    Pregunta("hidraulica_4", "¿Por qué en Bogotá el agua hierve a menos de 100 °C?",
                        listOf("Porque a esa altura la presión del aire es menor", "Porque el agua de Bogotá es más pura", "Porque hace frío", "Porque las ollas son de aluminio"), 0),
                    Pregunta("hidraulica_5", "¿Cuál es el río principal de Colombia, que la recorre de sur a norte hasta el mar Caribe?",
                        listOf("El Magdalena", "El Amazonas", "El Orinoco", "El Atrato"), 0)
                ),
                recompensaXp = 330,
                recompensaMonedas = 70,
                itemRecompensaId = "hilo_azul",
                insigniaRecompensaId = "insignia_hidraulica",
                coleccionableRecompensaId = "col_molinete"
            )
            // Datos verificados en noviembre de 2026: el Grupo de Genética de Poblaciones e
            // Identificación del IGUN hace pruebas de filiación con el ICBF; Watson y Crick (1953)
            // y la Fotografía 51 del grupo de Rosalind Franklin
            "reto_genetica" -> RetoQuiz(
                id = "reto_genetica",
                puntoInteresId = puntoId,
                titulo = "Capítulo 11: La Doble Hélice",
                descripcion = "Las letras con las que está escrita la vida.",
                preguntas = listOf(
                    Pregunta("genetica_1", "¿Qué pruebas hace el Instituto de Genética de la UNAL, de la mano del ICBF, para saber quién es el papá o la mamá de alguien?",
                        listOf("Pruebas de paternidad con ADN", "Exámenes de la vista", "Pruebas de embarazo", "Radiografías"), 0),
                    Pregunta("genetica_2", "En el ADN, ¿con qué letra se empareja la adenina (A)?",
                        listOf("Con la timina (T)", "Con la guanina (G)", "Con la citosina (C)", "Con otra adenina (A)"), 0),
                    Pregunta("genetica_3", "¿Quiénes publicaron en 1953 el modelo de la doble hélice del ADN?",
                        listOf("James Watson y Francis Crick", "Marie y Pierre Curie", "Charles Darwin y Alfred Wallace", "Louis Pasteur y Robert Koch"), 0),
                    Pregunta("genetica_4", "¿Qué científica dirigía el trabajo que produjo la «Fotografía 51», clave para descubrir la forma del ADN?",
                        listOf("Rosalind Franklin", "Marie Curie", "Ada Lovelace", "Jane Goodall"), 0),
                    Pregunta("genetica_5", "¿Con qué planta hizo Gregor Mendel sus experimentos sobre la herencia?",
                        listOf("Con arvejas", "Con rosas", "Con maíz", "Con girasoles"), 0)
                ),
                recompensaXp = 360,
                recompensaMonedas = 75,
                itemRecompensaId = "hilo_verde",
                insigniaRecompensaId = "insignia_genetica",
                coleccionableRecompensaId = "col_arveja"
            )
            // Datos verificados en noviembre de 2026 (El Tiempo, 25 de mayo de 2023): se inauguró en
            // octubre de 1953; el vitral de Paul Boné muestra a Cristo enseñando y le da el nombre;
            // Camilo Torres fue capellán en los años sesenta; Miguel Sopó Duque fundió en bronce
            // las catorce estaciones del viacrucis
            "reto_capilla" -> RetoQuiz(
                id = "reto_capilla",
                puntoInteresId = puntoId,
                titulo = "Capítulo 12: El Vitral Roto",
                descripcion = "La luz que entra de colores.",
                preguntas = listOf(
                    Pregunta("capilla_1", "¿En qué año se inauguró la Capilla Cristo Maestro de la Ciudad Universitaria?",
                        listOf("En 1953", "En 1867", "En 1999", "En 1910"), 0),
                    Pregunta("capilla_2", "¿De dónde le viene el nombre a la Capilla Cristo Maestro?",
                        listOf("De su vitral principal, que muestra a Cristo enseñando", "De un profesor llamado Cristo", "De la calle donde queda", "De una canción de la universidad"), 0),
                    Pregunta("capilla_3", "¿Qué sacerdote y sociólogo fue capellán de la Universidad Nacional en los años sesenta?",
                        listOf("Camilo Torres Restrepo", "José Celestino Mutis", "Francisco José de Caldas", "Antonio Nariño"), 0),
                    Pregunta("capilla_4", "¿En qué material están fundidas las catorce estaciones del viacrucis de la capilla?",
                        listOf("En bronce", "En oro", "En plástico", "En cera"), 0),
                    Pregunta("capilla_5", "En un vitral tradicional, ¿con qué metal se unen los pedazos de vidrio de colores?",
                        listOf("Con plomo", "Con oro", "Con aluminio", "Con cobre de cables"), 0)
                ),
                recompensaXp = 390,
                recompensaMonedas = 80,
                itemRecompensaId = "hilo_amarillo",
                insigniaRecompensaId = "insignia_capilla",
                coleccionableRecompensaId = "col_vidrio"
            )
            // Datos verificados en noviembre de 2026 (reglamento de World Athletics): desde 2018 la
            // zona de entrega del 4×100 mide 30 m, y entregar el testigo fuera de ella descalifica
            "reto_polideportivo" -> RetoQuiz(
                id = "reto_polideportivo",
                puntoInteresId = puntoId,
                titulo = "Capítulo 13: El Último Nudo",
                descripcion = "Una carrera se gana entre todos.",
                preguntas = listOf(
                    Pregunta("poli_1", "¿Cuántos corredores forman un equipo de relevos 4×100?",
                        listOf("Cuatro", "Cien", "Dos", "Ocho"), 0),
                    Pregunta("poli_2", "¿Cómo se llama el tubo que se pasan los corredores en un relevo?",
                        listOf("El testigo", "El bastón de mando", "La antorcha", "El cronómetro"), 0),
                    Pregunta("poli_3", "En el relevo 4×100, ¿cuánto mide hoy la zona donde se entrega el testigo?",
                        listOf("30 metros", "5 metros", "100 metros", "1 metro"), 0),
                    Pregunta("poli_4", "¿Qué le pasa a un equipo de relevos si entrega el testigo fuera de la zona?",
                        listOf("Queda descalificado", "Gana un punto extra", "Vuelve a empezar desde cero", "No pasa nada"), 0),
                    Pregunta("poli_5", "¿Cuántos metros corre en total un equipo en el relevo 4×400?",
                        listOf("1600 metros", "400 metros", "800 metros", "4400 metros"), 0)
                ),
                recompensaXp = 450,
                recompensaMonedas = 90,
                itemRecompensaId = "madeja_memoria",
                insigniaRecompensaId = "insignia_relevos",
                coleccionableRecompensaId = "col_testigo"
            )
            // Las cuatro primeras eran del reto de lugar del edificio, que pasó a ser capítulo.
            // Datos verificados en noviembre de 2026: la División de Registro de la Sede Bogotá
            // atiende en el Polideportivo y se encarga de carnés y recibos de matrícula
            "reto_uriel" -> RetoQuiz(
                id = "reto_uriel",
                puntoInteresId = puntoId,
                titulo = "Capítulo 14: Todo Pendiente",
                descripcion = "Donde se firma lo que hace andar la universidad.",
                preguntas = listOf(
                    Pregunta("uriel_1", "¿Quién fue Uriel Gutiérrez, que da nombre a este edificio?",
                        listOf("Un estudiante asesinado en el campus", "Un rector de la universidad", "Un arquitecto", "Un presidente de Colombia"), 0),
                    Pregunta("uriel_2", "¿En qué año murió Uriel Gutiérrez?",
                        listOf("1954", "1929", "1948", "1971"), 0),
                    Pregunta("uriel_3", "¿Quién gobernaba Colombia cuando murió Uriel Gutiérrez?",
                        listOf("Gustavo Rojas Pinilla", "Alfonso López Pumarejo", "Carlos Lleras Restrepo", "Laureano Gómez"), 0),
                    Pregunta("uriel_4", "¿Qué estudiante murió el 8 de junio de 1929, la otra fecha que recuerda el Día del Estudiante Caído?",
                        listOf("Gonzalo Bravo Pérez", "Camilo Torres", "Jorge Eliécer Gaitán", "Manuel Ancízar"), 0),
                    Pregunta("uriel_5", "En la Sede Bogotá, ¿qué oficina se encarga de la carnetización y de los recibos de matrícula?",
                        listOf("La División de Registro", "La Biblioteca Central", "El Museo de Arte", "El Observatorio Astronómico"), 0)
                ),
                recompensaXp = 480,
                recompensaMonedas = 95,
                itemRecompensaId = "folio_uriel",
                insigniaRecompensaId = "insignia_uriel",
                coleccionableRecompensaId = "col_sello"
            )
            // Datos verificados en noviembre de 2026: el ACH recupera, organiza, preserva y divulga
            // el patrimonio documental de la Sede Bogotá (Circular UNAL), tiene cerca de 9 km de
            // archivo y su Fondo UN empieza en 1867; guarda fondos como el de Yu Takeuchi. La forma
            // de búho del plano de Leopoldo Rother (1936-1940) es una lectura muy difundida
            "reto_archivo" -> RetoQuiz(
                id = "reto_archivo",
                puntoInteresId = puntoId,
                titulo = "Capítulo 15: El Guardián del Archivo",
                descripcion = "La memoria escrita de la universidad.",
                preguntas = listOf(
                    Pregunta("archivo_1", "¿Qué hace el Archivo Central e Histórico de la Sede Bogotá?",
                        listOf("Recupera, organiza, preserva y divulga los documentos históricos de la universidad", "Vende libros usados", "Presta salones para clase", "Expide los diplomas de grado"), 0),
                    Pregunta("archivo_2", "Los documentos de la Universidad Nacional que guarda el archivo empiezan en el año de su fundación. ¿Cuál es?",
                        listOf("1867", "1910", "1950", "1991"), 0),
                    Pregunta("archivo_3", "¿El archivo personal de qué profesor japonés de matemáticas guarda el Archivo Central e Histórico?",
                        listOf("Yu Takeuchi", "Akira Kurosawa", "Hideki Yukawa", "Hayao Miyazaki"), 0),
                    Pregunta("archivo_4", "¿Qué animal, símbolo de la universidad, ven muchos en el plano de la Ciudad Universitaria?",
                        listOf("Un búho", "Un cóndor", "Un jaguar", "Una mariposa"), 0),
                    Pregunta("archivo_5", "Más o menos, ¿cuánto archivo central e histórico tiene la Universidad Nacional, si se pusieran las cajas en fila?",
                        listOf("Cerca de 9 kilómetros", "Cerca de 9 metros", "Cerca de 90 centímetros", "Cerca de 900 kilómetros"), 0)
                ),
                recompensaXp = 510,
                recompensaMonedas = 100,
                itemRecompensaId = "folio_archivo",
                insigniaRecompensaId = "insignia_archivo",
                coleccionableRecompensaId = "col_pluma"
            )
            // Las cuatro primeras eran del reto de lugar de la Hemeroteca, que pasó a ser capítulo
            "reto_hemeroteca" -> RetoQuiz(
                id = "reto_hemeroteca",
                puntoInteresId = puntoId,
                titulo = "Capítulo 16: Noticias sin Fecha",
                descripcion = "Lo que pasó, contado el mismo día.",
                preguntas = listOf(
                    Pregunta("hemeroteca_1", "¿Qué guarda principalmente una hemeroteca?",
                        listOf("Periódicos y revistas", "Plantas secas", "Fósiles", "Instrumentos musicales"), 0),
                    Pregunta("hemeroteca_2", "¿Qué cargo ocupó Carlos Lleras Restrepo, que da nombre a la Hemeroteca?",
                        listOf("Presidente de Colombia", "Rector de la universidad", "Alcalde de Medellín", "Arzobispo de Bogotá"), 0),
                    Pregunta("hemeroteca_3", "¿En qué periodo gobernó Carlos Lleras Restrepo?",
                        listOf("1966-1970", "1946-1950", "1982-1986", "1994-1998"), 0),
                    Pregunta("hemeroteca_4", "Si buscas la noticia original del Bogotazo de 1948, ¿dónde la encuentras?",
                        listOf("En la Hemeroteca", "En el Herbario", "En el Observatorio", "En el Conservatorio"), 0),
                    Pregunta("hemeroteca_5", "¿Cómo se llaman las publicaciones que salen cada cierto tiempo con el mismo nombre, como los diarios y las revistas?",
                        listOf("Publicaciones seriadas", "Publicaciones únicas", "Manuscritos", "Enciclopedias"), 0)
                ),
                recompensaXp = 540,
                recompensaMonedas = 105,
                itemRecompensaId = "folio_hemeroteca",
                insigniaRecompensaId = "insignia_hemeroteca",
                coleccionableRecompensaId = "col_hemeroteca"
            )
            // Datos verificados en noviembre de 2026 (Facultad de Ingeniería): la Secretaría de
            // Facultad atiende en el Edificio 408 - CADE; las solicitudes estudiantiles se radican
            // en el primer piso, en la ventanilla del área curricular; las certificaciones
            // académicas se piden por el aplicativo Ticket FIBOG
            "reto_cade" -> RetoQuiz(
                id = "reto_cade",
                puntoInteresId = puntoId,
                titulo = "Capítulo 17: La Fila Eterna",
                descripcion = "Una ventanilla para cada área.",
                preguntas = listOf(
                    Pregunta("cade_1", "En el CADE de Ingeniería, ¿dónde se radican las solicitudes de los estudiantes?",
                        listOf("En el primer piso, en la ventanilla de su área curricular", "En la cafetería", "En el Estadio Alfonso López", "En la Biblioteca Central"), 0),
                    Pregunta("cade_2", "¿Qué oficina de la Facultad de Ingeniería funciona en el CADE?",
                        listOf("La Secretaría de Facultad", "El Museo de Arte", "La Hemeroteca", "El Observatorio Astronómico"), 0),
                    Pregunta("cade_3", "¿Qué laboratorio queda justo al lado del CADE de Ingeniería?",
                        listOf("El Laboratorio de Hidráulica", "El Laboratorio de Genética", "El Herbario", "El planetario"), 0),
                    Pregunta("cade_4", "En la Facultad de Ingeniería, ¿cómo se piden las certificaciones académicas?",
                        listOf("En línea, por el aplicativo Ticket FIBOG", "Por carta en el estadio", "Llamando a la radio", "Solo en persona en la rectoría"), 0),
                    Pregunta("cade_5", "En una fila bien atendida, ¿a quién se atiende primero?",
                        listOf("Al que llegó primero", "Al que llegó de último", "Al más alto", "Al que grite más duro"), 0)
                ),
                recompensaXp = 570,
                recompensaMonedas = 110,
                itemRecompensaId = "folio_cade",
                insigniaRecompensaId = "insignia_cade",
                coleccionableRecompensaId = "col_turno"
            )
            // Datos verificados en noviembre de 2026 (directorios de la UNAL): la Unidad Camilo
            // Torres (Calle 44 # 45-67) se organiza en bloques como el B7; allí funcionan el
            // Programa de Egresados, oficinas de Bienestar y la Unidad de Educación Continua y
            // Permanente de Ingeniería. Camilo Torres cofundó la carrera de Sociología con Fals Borda
            "reto_camilo_torres" -> RetoQuiz(
                id = "reto_camilo_torres",
                puntoInteresId = puntoId,
                titulo = "Capítulo 18: La Firma",
                descripcion = "Donde la universidad recuerda a los que se fueron.",
                preguntas = listOf(
                    Pregunta("camilo_1", "¿Qué programa de la universidad, que sigue en contacto con quienes ya se graduaron, funciona en la Unidad Camilo Torres?",
                        listOf("El Programa de Egresados", "El Programa de Admisiones", "El equipo de fútbol", "La emisora"), 0),
                    Pregunta("camilo_2", "¿Cómo se le dice a una persona que ya terminó su carrera y se graduó de la universidad?",
                        listOf("Egresada", "Aspirante", "Admitida", "Visitante"), 0),
                    Pregunta("camilo_3", "¿Cómo se identifican los edificios de la Unidad Camilo Torres?",
                        listOf("Por bloques con letra y número, como el B7", "Por colores", "Por nombres de animales", "Por números romanos"), 0),
                    Pregunta("camilo_4", "¿Qué unidad de la Facultad de Ingeniería, que ofrece cursos y diplomados, tiene su sede en la Unidad Camilo Torres?",
                        listOf("La Unidad de Educación Continua y Permanente", "El Laboratorio de Hidráulica", "El CADE de Ingeniería", "El Observatorio Astronómico"), 0),
                    Pregunta("camilo_5", "Camilo Torres, cuyo nombre lleva la Unidad, fundó con Orlando Fals Borda una carrera de la universidad. ¿Cuál?",
                        listOf("Sociología", "Medicina", "Ingeniería Civil", "Música"), 0)
                ),
                recompensaXp = 620,
                recompensaMonedas = 120,
                itemRecompensaId = "expediente_memoria",
                insigniaRecompensaId = "insignia_camilo_torres",
                coleccionableRecompensaId = "col_sello_dorado"
            )
            else -> RetosLugares.porPunto(puntoId)?.let { lugar ->
                RetoQuiz(
                    id = lugar.retoId,
                    puntoInteresId = puntoId,
                    titulo = lugar.nombre,
                    descripcion = "Reto de lugar: demuestra cuánto sabes de este rincón del campus.",
                    preguntas = lugar.preguntas,
                    recompensaXp = RetosLugares.XP_RECOMPENSA,
                    recompensaMonedas = RetosLugares.MONEDAS_RECOMPENSA
                )
            } ?: RetoQuiz(
                id = Historia.retoIdParaPunto(puntoId),
                puntoInteresId = puntoId,
                titulo = "Desafío de Exploración UNAL",
                descripcion = "Pon a prueba tus conocimientos sobre la Ciudad Blanca.",
                preguntas = TriviaDiaria.banco.take(5),
                recompensaXp = 80,
                recompensaMonedas = 15,
                itemRecompensaId = null,
                insigniaRecompensaId = null,
                coleccionableRecompensaId = null
            )
        }
    }
}
