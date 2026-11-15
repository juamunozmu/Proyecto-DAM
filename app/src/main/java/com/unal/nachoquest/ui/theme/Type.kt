package com.unal.nachoquest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.unal.nachoquest.R

/**
 * Fuente de títulos, marcadores y etiquetas: Jersey 15 (licencia OFL, ver docs/licencias), de
 * trazo pixelado como Nacho. El texto corrido usa la fuente del sistema.
 *
 * La fuente trae un solo peso. Se declara para todos para que Compose no la engrose de forma
 * artificial cuando una pantalla pide negrita.
 */
val FuentePixel = FontFamily(
    listOf(
        FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold,
        FontWeight.Bold, FontWeight.ExtraBold, FontWeight.Black
    ).map { peso -> Font(R.font.jersey15, peso) }
)

/** Etiqueta pequeña en mayúsculas («CAPÍTULO 1», «TRIVIA DEL DÍA»). */
val EstiloEtiqueta = TextStyle(
    fontFamily = FuentePixel,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 18.sp,
    letterSpacing = 1.5.sp
)

// Las letras de Jersey 15 son más bajas que las de la fuente del sistema, así que los títulos
// usan tamaños más grandes de lo habitual para verse del tamaño esperado.
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FuentePixel,
        fontWeight = FontWeight.Normal,
        fontSize = 44.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FuentePixel,
        fontWeight = FontWeight.Normal,
        fontSize = 37.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.5.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FuentePixel,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 33.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FuentePixel,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 29.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
