package com.lipa.terminal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun KeyValueRow(k: String, v: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = k.uppercase(),
            style = TextStyle(
                fontFamily = LipaFonts.Mono,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = LipaColors.InkSub,
            ),
        )
        Text(
            text = v,
            style = TextStyle(
                fontFamily = LipaFonts.Mono,
                fontSize = 13.sp,
                color = LipaColors.Ink,
            ),
        )
    }
}
