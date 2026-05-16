package com.lipa.terminal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun LipaMark(
    sizeSp: Int = 22,
    color: Color = LipaColors.Ink,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "lipa",
            style = TextStyle(
                fontFamily = LipaFonts.Display,
                fontWeight = FontWeight.Bold,
                fontSize = sizeSp.sp,
                letterSpacing = (-0.5).sp,
                color = color,
            ),
        )
        Box(
            modifier = Modifier
                .size((sizeSp / 4).coerceAtLeast(5).dp)
                .background(LipaColors.Green, CircleShape),
        )
    }
}
