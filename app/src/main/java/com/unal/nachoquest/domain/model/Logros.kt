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

    /** Logros que el jugador no tenía en [antes] y sí tiene en [despues]. */
    fun nuevos(antes: UserProfile, despues: UserProfile): List<Logro> {
        val yaTenia = calcular(antes).filter { it.desbloqueado }.map { it.id }.toSet()
        return calcular(despues).filter { it.desbloqueado && it.id !in yaTenia }
    }

    fun calcular(perfil: UserProfile): List<Logro> {
        val capitulosHechos = Historia.capitulos.count { it.retoId in perfil.retosCompletados }
        val coleccionables = CatalogoItems.itemsColeccionables.count { it.id in perfil.itemsDesbloqueados }
        val perfectos = perfil.estrellasPorReto.values.count { it >= 3 }
        val retosLugar = RetosLugares.retos.count { it.retoId in perfil.retosCompletados }
        val totalRetosLugar = RetosLugares.retos.size

        return listOf(
            Logro(
                id = "primera_exploracion",
                titulo = "Primera exploración",
                descripcion = "Recupera tu primer Fragmento de Memoria",
                emoji = "🧭",
                desbloqueado = capitulosHechos >= 1
            ),
            Logro(
                id = "guia_campus",
                titulo = "Guía del campus",
                descripcion = "Supera 5 retos de lugar en el mapa",
                emoji = "❓",
                desbloqueado = retosLugar >= 5,
                progreso = retosLugar.coerceAtMost(5),
                meta = 5
            ),
            Logro(
                id = "enciclopedia",
                titulo = "Enciclopedia andante",
                descripcion = "Supera todos los retos de lugar del campus",
                emoji = "🏛️",
                desbloqueado = retosLugar == totalRetosLugar,
                progreso = retosLugar,
                meta = totalRetosLugar
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
                id = "cartografo",
                titulo = "Cartógrafo de la Nacho",
                descripcion = "Descubre 40 lugares del campus en el mapa",
                emoji = "🗺️",
                desbloqueado = perfil.lugaresDescubiertos.size >= 40,
                progreso = perfil.lugaresDescubiertos.size.coerceAtMost(40),
                meta = 40
            ),
            Logro(
                id = "sabelotodo",
                titulo = "Sabelotodo",
                descripcion = "Supera una trivia del día sin fallar ninguna pregunta",
                emoji = "🧠",
                desbloqueado = perfil.triviasPerfectas >= 1
            ),
            Logro(
                id = "en_racha",
                titulo = "En racha",
                descripcion = "Juega la trivia del día 3 días seguidos",
                emoji = "🔥",
                desbloqueado = perfil.mejorRachaDias >= 3,
                progreso = perfil.mejorRachaDias.coerceAtMost(3),
                meta = 3
            ),
            Logro(
                id = "constancia",
                titulo = "Constancia de hierro",
                descripcion = "Juega la trivia del día 7 días seguidos",
                emoji = "📅",
                desbloqueado = perfil.mejorRachaDias >= 7,
                progreso = perfil.mejorRachaDias.coerceAtMost(7),
                meta = 7
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
