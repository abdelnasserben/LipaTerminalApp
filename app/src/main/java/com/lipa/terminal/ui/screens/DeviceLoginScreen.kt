package com.lipa.terminal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.components.ButtonTone
import com.lipa.terminal.ui.components.FieldLabel
import com.lipa.terminal.ui.components.LipaMark
import com.lipa.terminal.ui.components.PrimaryButton
import com.lipa.terminal.ui.components.TextInputField
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun DeviceLoginScreen(
    error: String?,
    isBusy: Boolean,
    onLogin: (serial: String, apiKey: String) -> Unit,
) {
    var serial by rememberSaveable { mutableStateOf("POS-AB12-0007") }
    var apiKey by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LipaColors.Bg)
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        LipaMark(sizeSp = 26)
        Text(
            text = "TERMINAL · V1.0",
            style = TextStyle(
                fontFamily = LipaFonts.Mono,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                color = LipaColors.InkSub,
            ),
            modifier = Modifier.padding(top = 6.dp, bottom = 28.dp),
        )

        Text(
            text = "Connect this device",
            style = TextStyle(
                fontFamily = LipaFonts.Display,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                letterSpacing = (-0.6).sp,
                color = LipaColors.Ink,
            ),
        )
        Text(
            text = "Enter the serial number and API key provisioned for this terminal.",
            style = TextStyle(
                fontFamily = LipaFonts.Display,
                fontSize = 14.sp,
                color = LipaColors.InkSub,
            ),
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
        )

        FieldLabel("Serial number")
        TextInputField(value = serial, onValueChange = { serial = it }, mono = true)
        Spacer(modifier = Modifier.size(16.dp))
        FieldLabel("API key")
        TextInputField(
            value = apiKey,
            onValueChange = { apiKey = it },
            mono = true,
            secret = true,
            placeholder = "••••••••••••",
        )

        Spacer(modifier = Modifier.size(24.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LipaColors.CardSub, RoundedCornerShape(12.dp))
                .border(1.dp, LipaColors.Border, RoundedCornerShape(12.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = LipaColors.InkSub,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "5 failed attempts will auto-suspend this terminal. Contact backoffice to recover.",
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontSize = 12.sp,
                    color = LipaColors.InkSub,
                ),
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = error,
                style = TextStyle(
                    fontFamily = LipaFonts.Mono,
                    fontSize = 12.sp,
                    color = LipaColors.Error,
                ),
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(
            text = if (isBusy) "Connecting…" else "Connect terminal",
            onClick = { onLogin(serial.trim(), apiKey.trim()) },
            enabled = !isBusy && serial.isNotBlank() && apiKey.isNotBlank(),
        )
    }
}
