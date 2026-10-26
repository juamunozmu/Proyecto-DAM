package com.unal.nachoquest.domain.model

data class Logro(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val emoji: String,
    val desbloqueado: Boolean,
    // Progreso actual y meta, para mostrar "3/10" en logros incrementales
    val progreso: Int = if (desbloqueado) 1 else 0,
    val meta: Int = 1
)

/**
 * Logros calculados a partir del perfil del jugador (no se guardan por separado).
 */
object Logros {

    fun calcular(perfil: UserProfile): List<Logro> {
        val capitulosHechos = Historia.capitulos.count { it.retoId in perfil.retosCompletados }
        val coleccionables = CatalogoItems.itemsColeccionables.count { it.id in perfil.itemsDesbloqueados }
        val perfectos = perfil.estrellasPorReto.values.count { it >= 3 }

        return listOf(
            Logro(
                id = "primera_exploracion",
                titulo = "Primera exploración",
                descripcion = "Recupera tu primer Fragmento de Memoria",
                emoji = "🧭",
                desbloqueado = perfil.retosCompletados.isNotEmpty()
            ),
            Logro(
                id = "maestro_biblioteca",
                titulo = "Maestro de biblioteca",
                descripcion = "Completa el capítulo de la Biblioteca Central",
                emoji = "📚",
                desbloqueado = "reto_biblio" in perfil.retosCompletados
            ),
            Logro(
                id = "mente_brillante",
                titulo = "Mente brillante",
                descripcion = "Consigue 3 estrellas en un reto",
                emoji = "💡",
                desbloqueado = perfectos >= 1
            ),
            Logro(
                id = "caminante",
                titulo = "Caminante incansable",
                descripcion = "Descubre 10 lugares del campus en el mapa",
                emoji = "👟",
                desbloqueado = perfil.lugaresDescubiertos.size >= 10,
                progreso = perfil.lugaresDescubiertos.size.coerceAtMost(10),
                meta = 10
            ),
            Logro(
                id = "coleccionista",
                titulo = "Coleccionista",
                descripcion = "Reúne todos los coleccionables",
                emoji = "🎁",
                desbloqueado = coleccionables == CatalogoItems.itemsColeccionables.size,
                progreso = coleccionables,
                meta = CatalogoItems.itemsColeccionables.size
            ),
            Logro(
                id = "leyenda",
                titulo = "Leyenda de la Ciudad Blanca",
                descripcion = "Derrota al Olvido y termina la historia",
                emoji = "👑",
                desbloqueado = capitulosHechos == Historia.capitulos.size,
                progreso = capitulosHechos,
                meta = Historia.capitulos.size
            )
        )
    }
}
