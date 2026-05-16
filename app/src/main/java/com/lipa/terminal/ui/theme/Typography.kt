package com.lipa.terminal.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.lipa.terminal.R

object LipaFonts {
    val Display: FontFamily = FontFamily(
        Font(R.font.bricolage_grotesque_regular, FontWeight.Normal),
        Font(R.font.bricolage_grotesque_medium, FontWeight.Medium),
        Font(R.font.bricolage_grotesque_semibold, FontWeight.SemiBold),
        Font(R.font.bricolage_grotesque_bold, FontWeight.Bold),
    )
    val Mono: FontFamily = FontFamily(
        Font(R.font.dm_mono_regular, FontWeight.Normal),
        Font(R.font.dm_mono_medium, FontWeight.Medium),
    )
}
