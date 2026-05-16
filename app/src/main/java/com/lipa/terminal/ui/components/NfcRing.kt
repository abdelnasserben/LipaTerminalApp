package com.lipa.terminal.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lipa.terminal.domain.TapStatus
import com.lipa.terminal.ui.theme.LipaColors

@Composable
fun NfcRing(status: TapStatus) {
    val transition = rememberInfiniteTransition(label = "nfc")
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (status) {
            TapStatus.Waiting -> {
                val phases = listOf(0, 800, 1600)
                phases.forEachIndexed { i, delayMs ->
                    val progress by transition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 2400, delayMillis = delayMs, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart,
                        ),
                        label = "ripple$i",
                    )
                    val scale = 0.6f + progress * 0.8f
                    val alpha = (1f - progress).coerceIn(0f, 0.6f)
                    Canvas(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(scale)
                            .alpha(alpha),
                    ) {
                        drawCircle(
                            color = LipaColors.Green,
                            radius = size.minDimension / 2 - 4,
                            style = Stroke(width = 4f),
                            center = Offset(size.width / 2, size.height / 2),
                        )
                    }
                }
            }
            TapStatus.Reading, TapStatus.Authorizing -> {
                val rotation by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 900, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "spin",
                )
                Canvas(modifier = Modifier.size(200.dp).rotate(rotation)) {
                    val sweep = 270f
                    drawArc(
                        color = LipaColors.Green,
                        startAngle = 0f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 8f),
                        topLeft = Offset(8f, 8f),
                        size = androidx.compose.ui.geometry.Size(size.width - 16f, size.height - 16f),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .size(124.dp)
                .background(LipaColors.Green, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Wifi,
                contentDescription = "NFC",
                tint = Color.White,
                modifier = Modifier.size(56.dp).rotate(45f),
            )
        }
    }
}
