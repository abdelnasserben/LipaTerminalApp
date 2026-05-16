package com.lipa.terminal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.components.AmountDisplay
import com.lipa.terminal.ui.components.ButtonTone
import com.lipa.terminal.ui.components.KeyValueRow
import com.lipa.terminal.ui.components.LipaCard
import com.lipa.terminal.ui.components.PrimaryButton
import com.lipa.terminal.ui.components.TopBar
import com.lipa.terminal.ui.components.TopBarTone
import com.lipa.terminal.ui.theme.KmfFormat
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun ConfirmationScreen(
    amount: Long,
    threshold: Long?,
    cardMask: String,
    authMethodLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(LipaColors.Bg)) {
        TopBar(
            title = "Confirm payment",
            subtitle = "PENDING_CONFIRMATION",
            tone = TopBarTone.Warning,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(LipaColors.WarningSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.WarningAmber,
                        contentDescription = null,
                        tint = LipaColors.Warning,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        text = "Large amount",
                        style = TextStyle(
                            fontFamily = LipaFonts.Display,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = LipaColors.Ink,
                        ),
                    )
                    Text(
                        text = if (threshold != null) "Exceeds ${KmfFormat.format(threshold)} KMF threshold" else "Customer confirmation required",
                        style = TextStyle(
                            fontFamily = LipaFonts.Mono,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp,
                            color = LipaColors.InkSub,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.size(18.dp))
            LipaCard(padding = 24.dp) {
                AmountDisplay(amount = amount, sizeSp = 56, label = "Charging")
                Spacer(modifier = Modifier.size(18.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(LipaColors.Border),
                )
                Spacer(modifier = Modifier.size(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    KeyValueRow("Card", cardMask)
                    KeyValueRow("Card auth", authMethodLabel)
                    KeyValueRow("Channel", "TERMINAL_NFC")
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LipaColors.WarningSoft, RoundedCornerShape(12.dp))
                    .padding(14.dp),
            ) {
                Text(
                    text = "Confirm out loud with the customer before proceeding. This action will charge their card.",
                    style = TextStyle(
                        fontFamily = LipaFonts.Display,
                        fontSize = 13.sp,
                        color = LipaColors.WarningInk,
                    ),
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PrimaryButton(text = "Confirm and charge", onClick = onConfirm)
            PrimaryButton(text = "Cancel payment", onClick = onCancel, tone = ButtonTone.Ghost)
        }
    }
}
