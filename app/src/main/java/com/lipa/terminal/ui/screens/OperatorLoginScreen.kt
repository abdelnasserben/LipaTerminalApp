package com.lipa.terminal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.components.FieldLabel
import com.lipa.terminal.ui.components.LipaMark
import com.lipa.terminal.ui.components.NumericKeypad
import com.lipa.terminal.ui.components.PinDots
import com.lipa.terminal.ui.components.PrimaryButton
import com.lipa.terminal.ui.components.TextInputField
import com.lipa.terminal.ui.components.TopBar
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun OperatorLoginScreen(
    deviceSerial: String,
    error: String?,
    isBusy: Boolean,
    onLogin: (phoneCountryCode: String, phoneNumber: String, pin: String) -> Unit,
    onSignOutOfTerminal: () -> Unit,
) {
    var step by rememberSaveable { mutableStateOf("phone") }
    var phone by rememberSaveable { mutableStateOf("3212345") }
    var pin by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(pin) {
        if (step == "pin" && pin.length == 4 && !isBusy) {
            onLogin("269", phone, pin)
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            pin = ""
        }
    }

    if (step == "phone") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LipaColors.Bg),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LipaMark(sizeSp = 20)
                Text(
                    text = deviceSerial,
                    style = TextStyle(
                        fontFamily = LipaFonts.Mono,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = LipaColors.InkSub,
                    ),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                Text(
                    text = "Start shift",
                    style = TextStyle(
                        fontFamily = LipaFonts.Display,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp,
                        letterSpacing = (-0.6).sp,
                        color = LipaColors.Ink,
                    ),
                )
                Text(
                    text = "Sign in to open an 8-hour cashier session on this terminal.",
                    style = TextStyle(
                        fontFamily = LipaFonts.Display,
                        fontSize = 14.sp,
                        color = LipaColors.InkSub,
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
                )

                FieldLabel("Cashier phone")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .background(LipaColors.Card, RoundedCornerShape(12.dp))
                            .border(1.dp, LipaColors.Border, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "+269",
                            style = TextStyle(
                                fontFamily = LipaFonts.Mono,
                                fontSize = 16.sp,
                                color = LipaColors.Ink,
                            ),
                        )
                    }
                    TextInputField(
                        value = phone,
                        onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                        mono = true,
                        keyboardType = KeyboardType.Phone,
                        modifier = Modifier.weight(1f),
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.padding(top = 12.dp))
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
                    text = "Continue",
                    onClick = { step = "pin" },
                    enabled = phone.length >= 5,
                )
                Spacer(modifier = Modifier.padding(top = 12.dp))
                Text(
                    text = "Sign out of terminal",
                    style = TextStyle(
                        fontFamily = LipaFonts.Display,
                        fontSize = 13.sp,
                        color = LipaColors.InkSub,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSignOutOfTerminal() }
                        .padding(vertical = 12.dp),
                )
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(LipaColors.Bg)) {
        TopBar(
            title = "Enter PIN",
            subtitle = "+269 $phone",
            onBack = { pin = ""; step = "phone" },
        )
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val helper = when {
                isBusy -> "Verifying…"
                error != null -> "Wrong PIN. Try again."
                else -> "4-digit cashier PIN"
            }
            Text(
                text = helper,
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontSize = 15.sp,
                    color = if (error != null) LipaColors.Error else LipaColors.InkSub,
                ),
                modifier = Modifier.padding(bottom = 24.dp),
            )
            PinDots(length = pin.length, total = 4, error = error != null)
        }
        NumericKeypad(
            onDigit = { d -> if (pin.length < 4) pin += d },
            onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
        )
    }
}
