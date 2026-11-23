package com.unal.nachoquest.domain.model

/**
 * Ayudas que se pueden usar en los retos. Se compran por adelantado en la tienda (y quedan
 * guardadas en `UserProfile.ayudas`) o, si no hay ninguna guardada, se pagan con monedas en el
 * momento de usarlas.
 */
enum class TipoAyuda(
    val id: String,
    val nombre: String,
    val emoji: String,
    val descripcion: String,
    val precio: Int
) {
    COMODIN("comodin", "Comodín 50:50", "✂️", "Descarta dos opciones incorrectas. Sirve cuando ya usaste el comodín gratis del reto.", 20),
    TIEMPO("tiempo", "Reloj de arena", "⏳", "Suma 10 segundos al tiempo de una pregunta.", 10),
    CORAZON("corazon", "Corazón extra", "❤️", "Recupera un corazón en medio de un reto, incluso justo después de perder el último.", 30),
    PISTA("pista", "Pista de minijuego", "💡", "Nacho te echa una mano en el minijuego de un capítulo.", 15);

    companion object {
        fun desde(id: String): TipoAyuda? = entries.find { it.id == id }
    }
}

/** Parte de Nacho donde va cada aspecto: solo se puede llevar uno por ranura. */
enum class RanuraAspecto { CABEZA, CARA, CUELLO, CAMISETA }

data class AspectoNacho(
    val id: String,
    val nombre: String,
    val emoji: String,
    val ranura: RanuraAspecto,
    val precio: Int
)

/** Cómo se pagó una ayuda: con una de la reserva, con monedas, o no se pudo pagar. */
enum class PagoAyuda { DE_RESERVA, CON_MONEDAS, SIN_SALDO }

object Tienda {

    /** Monedas por superar la trivia del día (solo la primera vez que se juega cada día). */
    const val MONEDAS_TRIVIA = 10

    /** Segundos que suma el reloj de arena. */
    const val SEGUNDOS_TIEMPO_EXTRA = 10

    /** Pistas que se pueden pedir como máximo en cada minijuego. */
    const val MAX_PISTAS_MINIJUEGO = 3

    val aspectos: List<AspectoNacho> = listOf(
        AspectoNacho("gorra", "Gorra UNAL", "🧢", RanuraAspecto.CABEZA, 60),
        AspectoNacho("birrete", "Birrete de grado", "🎓", RanuraAspecto.CABEZA, 150),
        AspectoNacho("gafas", "Gafas de estudio", "👓", RanuraAspecto.CARA, 50),
        AspectoNacho("gafas_sol", "Gafas de sol", "🕶️", RanuraAspecto.CARA, 80),
        AspectoNacho("bufanda", "Bufanda de la Nacho", "🧣", RanuraAspecto.CUELLO, 70),
        AspectoNacho("camiseta_dorada", "Camiseta dorada", "✨", RanuraAspecto.CAMISETA, 200)
    )

    fun aspecto(id: String): AspectoNacho? = aspectos.find { it.id == id }

    fun reserva(perfil: UserProfile, tipo: TipoAyuda): Int = perfil.ayudas[tipo.id] ?: 0

    /** Cómo se pagaría la ayuda: primero se gasta la reserva y, si no hay, monedas. */
    fun formaDePago(perfil: UserProfile, tipo: TipoAyuda): PagoAyuda = when {
        reserva(perfil, tipo) > 0 -> PagoAyuda.DE_RESERVA
        perfil.monedas >= tipo.precio -> PagoAyuda.CON_MONEDAS
        else -> PagoAyuda.SIN_SALDO
    }

    /**
     * Aspectos equipados después de tocar [id]: si ya estaba puesto se quita; si no, se pone y
     * reemplaza al que hubiera en la misma ranura.
     */
    fun alternarEquipado(equipados: List<String>, id: String): List<String> {
        if (id in equipados) return equipados - id
        val ranura = aspecto(id)?.ranura ?: return equipados
        return equipados.filter { aspecto(it)?.ranura != ranura } + id
    }
}
