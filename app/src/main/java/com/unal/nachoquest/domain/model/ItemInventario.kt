package com.unal.nachoquest.domain.model

enum class CategoriaItem {
    OBJETO,
    INSIGNIA,
    COLECCIONABLE
}

data class ItemInventario(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: CategoriaItem,
    val emojiIcono: String,
    val puntoOrigenId: String = "",
    val nombreLugarOrigen: String = "",
    val desbloqueaPuntoId: String? = null,
    val nombrePuntoDesbloqueado: String? = null,
    val requisitoTexto: String = ""
)

object CatalogoItems {

    val itemsObjetos = listOf(
        ItemInventario(
            id = "carnet_antiguo",
            nombre = "Carné Histórico UNAL",
            descripcion = "Antiguo carné estudiantil sellado en rectoría. Sus sellos patrimoniales abren los torniquetes de la Biblioteca Central Gabriel García Márquez.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🪪",
            puntoOrigenId = "poi_plaza_che",
            nombreLugarOrigen = "Plaza Central Santander (Che)",
            desbloqueaPuntoId = "edif_102",
            nombrePuntoDesbloqueado = "Biblioteca Central",
            requisitoTexto = "Completa el reto inicial en la Plaza Che"
        ),
        ItemInventario(
            id = "regla_takeuchi",
            nombre = "Regla de Takeuchi",
            descripcion = "Mítica regla de cálculo inspirada en el gran maestro Yu Takeuchi. Sus escalas logarítmicas devuelven la energía a los laboratorios de Ingeniería.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "📐",
            puntoOrigenId = "edif_102",
            nombreLugarOrigen = "Biblioteca Central",
            desbloqueaPuntoId = "edif_401",
            nombrePuntoDesbloqueado = "Edificio Julio Garavito (Ingeniería)",
            requisitoTexto = "Recupera las letras borradas de la Biblioteca Central"
        ),
        ItemInventario(
            id = "partitura_maestra",
            nombre = "Partitura Maestra de Greiff",
            descripcion = "Manuscrito musical con anotaciones de grandes directores sinfónicos. Otorga entrada de honor al Auditorio León de Greiff.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🎼",
            puntoOrigenId = "edif_401",
            nombreLugarOrigen = "Edificio Julio Garavito (Ingeniería)",
            desbloqueaPuntoId = "edif_104",
            nombrePuntoDesbloqueado = "Auditorio León de Greiff",
            requisitoTexto = "Reconecta los laboratorios del edificio de Ingeniería"
        ),
        ItemInventario(
            id = "brujula_unal",
            nombre = "Brújula de Fritz Karsen",
            descripcion = "Instrumento de topografía con el que, según cuentan, se trazó el campus elíptico. Su aguja señala los lugares donde todavía queda bruma: te guía hasta el Estadio Alfonso López.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🧭",
            puntoOrigenId = "edif_104",
            nombreLugarOrigen = "Auditorio León de Greiff",
            desbloqueaPuntoId = "poi_estadio",
            nombrePuntoDesbloqueado = "Estadio Alfonso López",
            requisitoTexto = "Repite la sinfonía en el León de Greiff"
        ),
        ItemInventario(
            id = "copa_memoria",
            nombre = "Copa de la Memoria",
            descripcion = "El trofeo que la bruma tenía escondido en el Estadio Alfonso López. Quien la levanta no olvida nunca la historia de la Ciudad Blanca. Tiene una grieta que guarda un mensaje: lleva al Museo de Arte.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🏆",
            puntoOrigenId = "poi_estadio",
            nombreLugarOrigen = "Estadio Alfonso López",
            desbloqueaPuntoId = "edif_317",
            nombrePuntoDesbloqueado = "Museo de Arte UNAL",
            requisitoTexto = "Gánale la tanda de penales al Olvido en el estadio"
        ),
        // Acto II
        ItemInventario(
            id = "pincel_restaurador",
            nombre = "Pincel Restaurador",
            descripcion = "Lo dejó caer el Eco del museo. Pinta de vuelta lo que la bruma borra, y con él los animales de Veterinaria pueden volver a reconocerse.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🖌️",
            puntoOrigenId = "edif_317",
            nombreLugarOrigen = "Museo de Arte UNAL",
            desbloqueaPuntoId = "edif_501",
            nombrePuntoDesbloqueado = "Facultad de Medicina Veterinaria y de Zootecnia",
            requisitoTexto = "Encuentra las diferencias en el cuadro del Museo de Arte"
        ),
        ItemInventario(
            id = "cencerro_memoria",
            nombre = "Cencerro de la Memoria",
            descripcion = "Estaba colgado en la cerca de los potreros. Su sonido atraviesa cualquier bruma: te guía por el laberinto de la Concha Acústica.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🔔",
            puntoOrigenId = "edif_501",
            nombreLugarOrigen = "Facultad de Medicina Veterinaria y de Zootecnia",
            desbloqueaPuntoId = "edif_761",
            nombrePuntoDesbloqueado = "Concha Acústica",
            requisitoTexto = "Junta las parejas de animales en Veterinaria"
        ),
        ItemInventario(
            id = "copa_restaurada",
            nombre = "Copa Restaurada",
            descripcion = "La Copa de la Memoria sin grietas, después de reunir a los tres Ecos. Brilla con la bruma dorada del Recuerdo... y suelta hilos de colores que llevan a la antigua sede de Cine y Televisión.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🌟",
            puntoOrigenId = "edif_761",
            nombreLugarOrigen = "Concha Acústica",
            desbloqueaPuntoId = "edif_701",
            nombrePuntoDesbloqueado = "Antigua sede de Cine y Televisión",
            requisitoTexto = "Encuentra al último Eco en el laberinto de la Concha Acústica"
        ),
        // ---------- Acto III: los cinco Hilos de la Memoria ----------
        ItemInventario(
            id = "hilo_rojo",
            nombre = "Hilo Rojo de la Memoria",
            descripcion = "El primero de los cinco hilos que el Enredo se llevó de la Copa. Estaba enrollado en una bobina de película y tira, suavecito, hacia el Laboratorio de Hidráulica.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🧶",
            puntoOrigenId = "edif_701",
            nombreLugarOrigen = "Antigua sede de Cine y Televisión",
            desbloqueaPuntoId = "edif_408",
            nombrePuntoDesbloqueado = "Laboratorio de Hidráulica",
            requisitoTexto = "Arma la película en la antigua sede de Cine y Televisión"
        ),
        ItemInventario(
            id = "hilo_azul",
            nombre = "Hilo Azul de la Memoria",
            descripcion = "El segundo hilo. Subió flotando cuando el agua quedó exacta y todavía está húmedo. Tira hacia el Instituto de Genética.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🧶",
            puntoOrigenId = "edif_408",
            nombreLugarOrigen = "Laboratorio de Hidráulica",
            desbloqueaPuntoId = "edif_426",
            nombrePuntoDesbloqueado = "Instituto de Genética (IGUN)",
            requisitoTexto = "Deja el agua exacta en el Laboratorio de Hidráulica"
        ),
        ItemInventario(
            id = "hilo_verde",
            nombre = "Hilo Verde de la Memoria",
            descripcion = "El tercer hilo. Estaba enredado entre los peldaños de la doble hélice. Tira hacia la Capilla.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🧶",
            puntoOrigenId = "edif_426",
            nombreLugarOrigen = "Instituto de Genética (IGUN)",
            desbloqueaPuntoId = "edif_251",
            nombrePuntoDesbloqueado = "Capilla Cristo Maestro",
            requisitoTexto = "Completa la doble hélice en el Instituto de Genética"
        ),
        ItemInventario(
            id = "hilo_amarillo",
            nombre = "Hilo Amarillo de la Memoria",
            descripcion = "El cuarto hilo. Apareció flotando en el rayo de luz que atravesó el vitral. Tira hacia el Centro Polideportivo.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🧶",
            puntoOrigenId = "edif_251",
            nombreLugarOrigen = "Capilla Cristo Maestro",
            desbloqueaPuntoId = "edif_103",
            nombrePuntoDesbloqueado = "Centro Polideportivo",
            requisitoTexto = "Enciende el vitral de la Capilla Cristo Maestro"
        )
    )

    val itemsInsignias = listOf(
        ItemInventario(
            id = "insignia_che",
            nombre = "Pionero de la Plaza",
            descripcion = "Insignia otorgada a los estudiantes que conocen los hitos sociales, políticos e históricos de la emblemática Plaza Che.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "⭐",
            puntoOrigenId = "poi_plaza_che",
            nombreLugarOrigen = "Plaza Che",
            requisitoTexto = "Primer logro de bienvenida al campus"
        ),
        ItemInventario(
            id = "insignia_biblioteca",
            nombre = "Guardián del Saber",
            descripcion = "Reconocimiento a los exploradores dedicados a investigar la historia arquitectónica y literaria de la Biblioteca Central.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "🦉",
            puntoOrigenId = "edif_102",
            nombreLugarOrigen = "Biblioteca Central",
            requisitoTexto = "Supera el reto de la Biblioteca Central"
        ),
        ItemInventario(
            id = "insignia_ingenio",
            nombre = "Mente Científica",
            descripcion = "Premio a los inventores y solucionadores de problemas del Edificio Julio Garavito Armero.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "🔬",
            puntoOrigenId = "edif_401",
            nombreLugarOrigen = "Edificio Julio Garavito (Ingeniería)",
            requisitoTexto = "Supera el reto en el edificio de Ingeniería"
        ),
        ItemInventario(
            id = "insignia_arte",
            nombre = "Virtuoso del Auditorio",
            descripcion = "Otorgada por valorar el patrimonio cultural, musical y acústico del Monumento Nacional León de Greiff.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "🎭",
            puntoOrigenId = "edif_104",
            nombreLugarOrigen = "Auditorio León de Greiff",
            requisitoTexto = "Supera el reto en el Auditorio León de Greiff"
        ),
        ItemInventario(
            id = "insignia_estadio",
            nombre = "Crack del Alfonso López",
            descripcion = "Para quienes definen los partidos difíciles con calma y conocen la historia deportiva de la universidad.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "⚽",
            puntoOrigenId = "poi_estadio",
            nombreLugarOrigen = "Estadio Alfonso López",
            requisitoTexto = "Supera el reto en el Estadio Alfonso López"
        ),
        ItemInventario(
            id = "insignia_museo",
            nombre = "Curador de la Memoria",
            descripcion = "Para quienes miran con calma y notan los detalles que los demás pasan por alto.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "🖼️",
            puntoOrigenId = "edif_317",
            nombreLugarOrigen = "Museo de Arte UNAL",
            requisitoTexto = "Supera el reto del Museo de Arte"
        ),
        ItemInventario(
            id = "insignia_potrero",
            nombre = "Amigo del Potrero",
            descripcion = "Reconocimiento a quienes conocen y cuidan a los animales de la Facultad de Veterinaria.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "🐄",
            puntoOrigenId = "edif_501",
            nombreLugarOrigen = "Facultad de Medicina Veterinaria y de Zootecnia",
            requisitoTexto = "Supera el reto de Veterinaria"
        ),
        ItemInventario(
            id = "insignia_concha",
            nombre = "Voz de la Concha",
            descripcion = "Para quienes encontraron el camino en medio de la bruma y le devolvieron la voz al campus.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "🎤",
            puntoOrigenId = "edif_761",
            nombreLugarOrigen = "Concha Acústica",
            requisitoTexto = "Supera el reto de la Concha Acústica"
        ),
        ItemInventario(
            id = "insignia_cine",
            nombre = "Ojo de Director",
            descripcion = "Para quienes saben que una buena historia depende del orden en que se cuenta.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "🎬",
            puntoOrigenId = "edif_701",
            nombreLugarOrigen = "Antigua sede de Cine y Televisión",
            requisitoTexto = "Supera el reto de la antigua sede de Cine y Televisión"
        ),
        ItemInventario(
            id = "insignia_hidraulica",
            nombre = "Maestro del Caudal",
            descripcion = "Para quienes miden con calma y no riegan ni una gota de más.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "💧",
            puntoOrigenId = "edif_408",
            nombreLugarOrigen = "Laboratorio de Hidráulica",
            requisitoTexto = "Supera el reto del Laboratorio de Hidráulica"
        ),
        ItemInventario(
            id = "insignia_genetica",
            nombre = "Lector de ADN",
            descripcion = "Para quienes saben que cada letra tiene su pareja.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "🧬",
            puntoOrigenId = "edif_426",
            nombreLugarOrigen = "Instituto de Genética (IGUN)",
            requisitoTexto = "Supera el reto del Instituto de Genética"
        ),
        ItemInventario(
            id = "insignia_capilla",
            nombre = "Guardián de la Luz",
            descripcion = "Para quienes no se rinden hasta que el último vidrio vuelve a brillar.",
            categoria = CategoriaItem.INSIGNIA,
            emojiIcono = "🌈",
            puntoOrigenId = "edif_251",
            nombreLugarOrigen = "Capilla Cristo Maestro",
            requisitoTexto = "Supera el reto de la Capilla Cristo Maestro"
        )
    )

    val itemsColeccionables = listOf(
        ItemInventario(
            id = "col_afrecho",
            nombre = "Afrecho Legendario",
            descripcion = "El icónico pan integral de las cafeterías de la UNAL, alimento predilecto de incontables generaciones universitarias.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🥐",
            puntoOrigenId = "poi_plaza_che",
            nombreLugarOrigen = "Plaza Che",
            requisitoTexto = "Coleccionable especial de la Plaza Che"
        ),
        ItemInventario(
            id = "col_periodico",
            nombre = "UN Periódico Histórico",
            descripcion = "Una edición vintage impresa en papel prensa que narra los descubrimientos y debates más memorables de la universidad.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "📰",
            puntoOrigenId = "edif_102",
            nombreLugarOrigen = "Biblioteca Central",
            requisitoTexto = "Coleccionable encontrado en la Biblioteca"
        ),
        ItemInventario(
            id = "col_microscopio",
            nombre = "Lente de Precisión 1950",
            descripcion = "Pieza óptica original conservada de las primeras prácticas de laboratorio del campus.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🔍",
            puntoOrigenId = "edif_401",
            nombreLugarOrigen = "Edificio Julio Garavito (Ingeniería)",
            requisitoTexto = "Coleccionable rescatado en Ingeniería"
        ),
        ItemInventario(
            id = "col_vinilo",
            nombre = "Vinilo Filarmónico de Oro",
            descripcion = "Grabación en vivo de la Orquesta Filarmónica de Bogotá realizada bajo la cúpula de madera del León de Greiff.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "💿",
            puntoOrigenId = "edif_104",
            nombreLugarOrigen = "Auditorio León de Greiff",
            requisitoTexto = "Coleccionable hallado en el Auditorio"
        ),
        ItemInventario(
            id = "col_camiseta",
            nombre = "Camiseta de los Bolivarianos",
            descripcion = "Una camiseta de competencia como las que se vieron en los primeros Juegos Bolivarianos, estrenados en este estadio en 1938.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🎽",
            puntoOrigenId = "poi_estadio",
            nombreLugarOrigen = "Estadio Alfonso López",
            requisitoTexto = "Coleccionable escondido en el estadio"
        ),
        ItemInventario(
            id = "col_paleta",
            nombre = "Paleta de Colores Recuperados",
            descripcion = "Una paleta de pintor con los colores que el Eco le había robado al cuadro del museo. Todavía huele a óleo.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🎨",
            puntoOrigenId = "edif_317",
            nombreLugarOrigen = "Museo de Arte UNAL",
            requisitoTexto = "Coleccionable encontrado en el Museo de Arte"
        ),
        ItemInventario(
            id = "col_leche",
            nombre = "Botella de la Planta de Leches",
            descripcion = "Una botella de vidrio como las de la planta de leches de la Facultad de Ciencias Agrarias, vecina de los potreros.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🥛",
            puntoOrigenId = "edif_501",
            nombreLugarOrigen = "Facultad de Medicina Veterinaria y de Zootecnia",
            requisitoTexto = "Coleccionable hallado en Veterinaria"
        ),
        ItemInventario(
            id = "col_afiche",
            nombre = "Afiche de Concierto",
            descripcion = "Un afiche desteñido de un concierto al aire libre en la Concha Acústica. Alguien escribió encima: «¡Qué noche!».",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🎫",
            puntoOrigenId = "edif_761",
            nombreLugarOrigen = "Concha Acústica",
            requisitoTexto = "Coleccionable escondido en la Concha Acústica"
        ),
        ItemInventario(
            id = "col_rollo",
            nombre = "Rollo de 16 mm",
            descripcion = "Un rollo de película de 16 milímetros, como los que se usaban en las escuelas de cine antes del video digital. Tiene una etiqueta escrita a mano: «toma 3».",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🎞️",
            puntoOrigenId = "edif_701",
            nombreLugarOrigen = "Antigua sede de Cine y Televisión",
            requisitoTexto = "Coleccionable rescatado en la antigua sede de Cine y Televisión"
        ),
        ItemInventario(
            id = "col_molinete",
            nombre = "Molinete de Aforo",
            descripcion = "Una hélice pequeña que se mete en el río para medir qué tan rápido corre el agua. Con eso se calcula su caudal.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🌀",
            puntoOrigenId = "edif_408",
            nombreLugarOrigen = "Laboratorio de Hidráulica",
            requisitoTexto = "Coleccionable encontrado en el Laboratorio de Hidráulica"
        ),
        ItemInventario(
            id = "col_arveja",
            nombre = "Arveja de Mendel",
            descripcion = "Una arveja como las que usó Gregor Mendel en el siglo XIX para descubrir cómo se heredan los rasgos de padres a hijos.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🌱",
            puntoOrigenId = "edif_426",
            nombreLugarOrigen = "Instituto de Genética (IGUN)",
            requisitoTexto = "Coleccionable hallado en el Instituto de Genética"
        ),
        ItemInventario(
            id = "col_vidrio",
            nombre = "Vidrio Emplomado",
            descripcion = "Un pedacito de vitral: vidrio de color unido a los demás con una tira de plomo, como los que forman las ventanas de la capilla.",
            categoria = CategoriaItem.COLECCIONABLE,
            emojiIcono = "🪟",
            puntoOrigenId = "edif_251",
            nombreLugarOrigen = "Capilla Cristo Maestro",
            requisitoTexto = "Coleccionable encontrado en la Capilla Cristo Maestro"
        )
    )

    val todosLosItems: List<ItemInventario> = itemsObjetos + itemsInsignias + itemsColeccionables

    fun obtenerPorId(id: String): ItemInventario? = todosLosItems.find { it.id == id }

    fun obtenerPorCategoria(categoria: CategoriaItem): List<ItemInventario> =
        todosLosItems.filter { it.categoria == categoria }

    /**
     * Retorna el ID del ítem requerido para desbloquear un punto de interés por historia.
     * Si retorna null, significa que no requiere ningún ítem previo (está disponible de inmediato, ej. Plaza Che).
     */
    fun itemRequeridoParaPunto(puntoId: String): ItemInventario? {
        return itemsObjetos.find { it.desbloqueaPuntoId == puntoId }
    }
}
