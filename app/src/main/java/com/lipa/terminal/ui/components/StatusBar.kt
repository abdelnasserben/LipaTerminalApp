package com.lipa.terminal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun LipaStatusBar(
    time: String,
    operator: String?,
    online: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LipaColors.Bg)
            .height(32.dp)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = time,
            style = TextStyle(
                fontFamily = LipaFonts.Mono,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                color = LipaColors.Ink,
            ),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (operator != null) {
                Text(
                    text = operator,
                    style = TextStyle(
                        fontFamily = LipaFonts.Mono,
                        fontSize = 11.sp,
                        letterSpacing = 0.3.sp,
                        color = LipaColors.InkSub,
                    ),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            if (online) LipaColors.Green else LipaColors.Error,
                            CircleShape,
                        ),
                )
                Text(
                    text = if (online) "ONLINE" else "OFFLINE",
                    style = TextStyle(
                        fontFamily = LipaFonts.Mono,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                        color = if (online) LipaColors.Green else LipaColors.Error,
                    ),
                )
            }
        }
    }
}
