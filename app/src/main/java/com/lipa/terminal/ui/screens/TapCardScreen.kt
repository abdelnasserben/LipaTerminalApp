package com.lipa.terminal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.domain.TapStatus
import com.lipa.terminal.ui.components.AmountAlign
import com.lipa.terminal.ui.components.AmountDisplay
import com.lipa.terminal.ui.components.ButtonTone
import com.lipa.terminal.ui.components.NfcRing
import com.lipa.terminal.ui.components.PrimaryButton
import com.lipa.terminal.ui.components.TopBar
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun TapCardScreen(
    amount: Long,
    status: TapStatus,
    authModeLabel: String,
    nfcUnavailable: Boolean,
    onCancel: () -> Unit,
    onSimulateTap: () -> Unit,
) {
    val statusText = when (status) {
        TapStatus.Waiting -> "Tap card to pay"
        TapStatus.Reading -> "Reading card…"
        TapStatus.Authorizing -> "Authorizing…"
    }
    val hint = when (status) {
        TapStatus.Waiting -> "Hold the card flat against the back of the device"
        else -> "Do not move the card"
    }

    Column(modifier = Modifier.fillMaxSize().background(LipaColors.Bg)) {
        TopBar(
            title = "Take payment",
            subtitle = "NFC · $authModeLabel",
            onBack = onCancel,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AmountDisplay(
                amount = amount,
                sizeSp = 56,
                label = "Charging",
                align = AmountAlign.Center,
            )
            Spacer(modifier = Modifier.size(40.dp))
            NfcRing(status = status)
            Spacer(modifier = Modifier.size(32.dp))
            Text(
                text = statusText,
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.3).sp,
                    color = LipaColors.Ink,
                    textAlign = TextAlign.Center,
                ),
            )
            Text(
                text = hint,
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontSize = 14.sp,
                    color = LipaColors.InkSub,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.padding(top = 6.dp),
            )
            if (nfcUnavailable) {
                Text(
                    text = "NFC unavailable on this device — using simulated tap",
                    style = TextStyle(
                        fontFamily = LipaFonts.Mono,
                        fontSize = 11.sp,
                        color = LipaColors.Warning,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (status == TapStatus.Waiting) {
                PrimaryButton(
                    text = if (nfcUnavailable) "Simulate card tap" else "Simulate card tap (dev)",
                    onClick = onSimulateTap,
                    tone = ButtonTone.Dark,
                )
            }
            PrimaryButton(
                text = "Cancel",
                onClick = onCancel,
                tone = ButtonTone.Ghost,
            )
        }
    }
}
