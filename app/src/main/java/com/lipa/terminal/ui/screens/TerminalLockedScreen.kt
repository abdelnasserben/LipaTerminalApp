package com.lipa.terminal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.components.ButtonTone
import com.lipa.terminal.ui.components.PrimaryButton
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

private fun reasonCopy(reason: String?): Pair<String, String> = when (reason) {
    "TERMINAL_SUSPENDED" -> "Terminal suspended" to "This terminal has been suspended. Contact Lipa backoffice to recover."
    "TERMINAL_REVOKED" -> "Terminal revoked" to "This terminal has been permanently revoked."
    "TERMINAL_NOT_REGISTERED" -> "Terminal not registered" to "This serial number is not registered with Lipa."
    else -> "Terminal unavailable" to (reason ?: "Contact Lipa backoffice to recover.")
}

@Composable
fun TerminalLockedScreen(
    reason: String?,
    onSignOut: () -> Unit,
) {
    val (title, hint) = reasonCopy(reason)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LipaColors.Bg)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(LipaColors.ErrorSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = LipaColors.Error,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = title,
            style = TextStyle(
                fontFamily = LipaFonts.Display,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                letterSpacing = (-0.5).sp,
                color = LipaColors.Ink,
                textAlign = TextAlign.Center,
            ),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = hint,
            style = TextStyle(
                fontFamily = LipaFonts.Display,
                fontSize = 14.sp,
                color = LipaColors.InkSub,
                textAlign = TextAlign.Center,
            ),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = reason ?: "",
            style = TextStyle(
                fontFamily = LipaFonts.Mono,
                fontSize = 11.sp,
                letterSpacing = 0.8.sp,
                color = LipaColors.ErrorInk.copy(alpha = 0.7f),
            ),
        )
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(text = "Sign out of terminal", onClick = onSignOut, tone = ButtonTone.Dark)
    }
}
