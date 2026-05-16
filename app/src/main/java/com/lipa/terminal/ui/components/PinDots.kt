package com.lipa.terminal.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lipa.terminal.ui.theme.LipaColors

@Composable
fun PinDots(
    length: Int,
    total: Int = 4,
    error: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val filled = i < length
            val border = when {
                error -> LipaColors.Error
                filled -> LipaColors.Ink
                else -> LipaColors.BorderStrong
            }
            val fill = when {
                error -> LipaColors.Error
                filled -> LipaColors.Ink
                else -> Color.Transparent
            }
            val animatedFill by animateColorAsState(fill, label = "pinFill")
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .border(2.dp, border, CircleShape)
                    .background(animatedFill, CircleShape),
            )
        }
    }
}
