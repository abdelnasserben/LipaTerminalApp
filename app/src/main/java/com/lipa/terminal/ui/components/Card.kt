package com.lipa.terminal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lipa.terminal.ui.theme.LipaColors

@Composable
fun LipaCard(
    modifier: Modifier = Modifier,
    padding: Dp = 20.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LipaColors.Card,
        border = BorderStroke(1.dp, LipaColors.Border),
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}
