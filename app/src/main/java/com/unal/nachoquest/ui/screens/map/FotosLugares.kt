package com.unal.nachoquest.ui.screens.map

import androidx.annotation.DrawableRes
import com.unal.nachoquest.R

/** Foto real de un lugar, con el crédito que exige su licencia. */
data class FotoLugar(
    @DrawableRes val recurso: Int,
    val autor: String,
    val licencia: String
) {
    val credito: String get() = "Foto: $autor · $licencia · Wikimedia Commons"
}

/**
 * Fotos reales de los lugares del campus, tomadas de Wikimedia Commons con licencia libre
 * (reducidas a 800 px de ancho). Las licencias CC BY y CC BY-SA obligan a dar crédito al autor,
 * por eso la ficha del lugar muestra [FotoLugar.credito] debajo de la foto. La lista completa,
 * con el enlace a cada archivo original, está en la sección «Créditos» del README.
 */
object FotosLugares {

    private val fotos = mapOf(
        "poi_plaza_che" to FotoLugar(R.drawable.foto_poi_plaza_che, "Carlos25i", "CC BY-SA 4.0"),
        "edif_102" to FotoLugar(R.drawable.foto_edif_102, "Smcamposa", "CC BY-SA 3.0"),
        "edif_104" to FotoLugar(R.drawable.foto_edif_104, "JuanGris (Lucía Estévez)", "CC BY-SA 3.0"),
        "edif_207" to FotoLugar(R.drawable.foto_edif_207, "MizhyMonroy", "CC BY-SA 3.0"),
        "edif_401" to FotoLugar(R.drawable.foto_edif_401, "Carlos25i", "CC BY-SA 4.0"),
        "edif_404" to FotoLugar(R.drawable.foto_edif_404, "Gaaplex", "CC BY-SA 4.0"),
        "edif_501" to FotoLugar(R.drawable.foto_edif_501, "JuanGris (Lucía Estévez)", "CC BY-SA 3.0")
    )

    fun dePunto(puntoId: String): FotoLugar? = fotos[puntoId]
}
