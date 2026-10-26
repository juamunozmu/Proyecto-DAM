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
            descripcion = "Instrumento de topografía usado en 1936 para trazar el campus elíptico. Desbloquea la pista y el legado del Estadio Alfonso López.",
            categoria = CategoriaItem.OBJETO,
            emojiIcono = "🧭",
            puntoOrigenId = "edif_104",
            nombreLugarOrigen = "Auditorio León de Greiff",
            desbloqueaPuntoId = "poi_estadio",
            nombrePuntoDesbloqueado = "Estadio Alfonso López",
            requisitoTexto = "Repite la sinfonía en el León de Greiff"
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
