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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.components.AmountAlign
import com.lipa.terminal.ui.components.AmountDisplay
import com.lipa.terminal.ui.components.NumericKeypad
import com.lipa.terminal.ui.components.PinDots
import com.lipa.terminal.ui.components.TopBar
import com.lipa.terminal.ui.components.TopBarTone
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun CustomerPinScreen(
    amount: Long,
    cardMask: String,
    error: Boolean,
    isVerifying: Boolean,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var pin by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(pin) {
        if (pin.length == 4 && !isVerifying) {
            onSubmit(pin)
        }
    }
    LaunchedEffect(error) { if (error) pin = "" }

    Column(modifier = Modifier.fillMaxSize().background(LipaColors.Bg)) {
        TopBar(
            title = "Customer PIN required",
            subtitle = "PENDING_PIN",
            tone = TopBarTone.Warning,
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
            Text(
                text = "CARD $cardMask",
                style = TextStyle(
                    fontFamily = LipaFonts.Mono,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = LipaColors.InkSub,
                ),
                modifier = Modifier.padding(bottom = 10.dp),
            )
            AmountDisplay(amount = amount, sizeSp = 48, align = AmountAlign.Center)
            Spacer(modifier = Modifier.size(36.dp))
            Text(
                text = "Pass the device to the customer",
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    letterSpacing = (-0.2).sp,
                    color = LipaColors.Ink,
                    textAlign = TextAlign.Center,
                ),
            )
            Text(
                text = when {
                    isVerifying -> "Verifying PIN…"
                    error -> "Wrong PIN. Try again."
                    else -> "They tap their 4-digit card PIN"
                },
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontSize = 13.sp,
                    color = if (error) LipaColors.Error else LipaColors.InkSub,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )
            PinDots(length = pin.length, total = 4, error = error)
        }
        NumericKeypad(
            onDigit = { d -> if (pin.length < 4) pin += d },
            onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
        )
    }
}
