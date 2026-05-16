package com.lipa.terminal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

enum class ChipTone { Plain, Success, Warning, Error }

@Composable
fun StateChip(text: String, tone: ChipTone = ChipTone.Plain) {
    val (bg, fg, dot) = when (tone) {
        ChipTone.Plain -> Triple(LipaColors.BgDeep, LipaColors.Ink, LipaColors.InkSub)
        ChipTone.Success -> Triple(LipaColors.SuccessSoft, LipaColors.GreenDeep, LipaColors.Green)
        ChipTone.Warning -> Triple(LipaColors.WarningSoft, LipaColors.WarningInk, LipaColors.Warning)
        ChipTone.Error -> Triple(LipaColors.ErrorSoft, LipaColors.ErrorInk, LipaColors.Error)
    }
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.size(6.dp).background(dot, CircleShape))
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = LipaFonts.Mono,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = fg,
            ),
        )
    }
}
