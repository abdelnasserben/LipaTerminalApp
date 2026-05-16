package com.lipa.terminal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.components.ButtonTone
import com.lipa.terminal.ui.components.KeyValueRow
import com.lipa.terminal.ui.components.LipaCard
import com.lipa.terminal.ui.components.PrimaryButton
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun ShiftMenuSheet(
    phoneCountryCode: String,
    phoneNumber: String,
    terminalId: String,
    merchantId: String,
    sessionEndsAt: String,
    onClose: () -> Unit,
    onEndShift: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(interactionSource = interaction, indication = null, onClick = onClose),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(LipaColors.Bg)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                .padding(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(LipaColors.BorderStrong, RoundedCornerShape(2.dp)),
                )
            }
            Text(
                text = "CURRENT SHIFT",
                style = TextStyle(
                    fontFamily = LipaFonts.Mono,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    color = LipaColors.InkSub,
                ),
                modifier = Modifier.padding(bottom = 6.dp),
            )
            Text(
                text = "+$phoneCountryCode $phoneNumber",
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.3).sp,
                    color = LipaColors.Ink,
                ),
                modifier = Modifier.padding(bottom = 18.dp),
            )
            LipaCard(padding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    KeyValueRow("Operator", "+$phoneCountryCode $phoneNumber")
                    KeyValueRow("Terminal", terminalId)
                    KeyValueRow("Merchant", merchantId)
                    KeyValueRow("Session ends", sessionEndsAt)
                }
            }
            Spacer(modifier = Modifier.size(16.dp))
            PrimaryButton(text = "End shift", onClick = onEndShift, tone = ButtonTone.Dark)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Close",
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontSize = 14.sp,
                    color = LipaColors.InkSub,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClose)
                    .padding(12.dp),
            )
        }
    }
}
