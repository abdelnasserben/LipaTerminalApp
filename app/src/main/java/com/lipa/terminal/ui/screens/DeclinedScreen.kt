package com.lipa.terminal.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.data.model.ErrorCodes
import com.lipa.terminal.ui.components.AmountDisplay
import com.lipa.terminal.ui.components.ButtonTone
import com.lipa.terminal.ui.components.LipaCard
import com.lipa.terminal.ui.components.PrimaryButton
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

private data class DeclineCopy(val title: String, val hint: String, val retryable: Boolean)

private fun copyFor(code: String?): DeclineCopy = when (code) {
    ErrorCodes.CARD_BLOCKED -> DeclineCopy("Card blocked", "This card has been blocked. Ask the customer to contact Lipa support.", false)
    ErrorCodes.CARD_LOST -> DeclineCopy("Card reported lost", "Do not retry. Inform the customer to contact Lipa support.", false)
    ErrorCodes.CARD_STOLEN -> DeclineCopy("Card reported stolen", "Do not retry. Inform the customer.", false)
    ErrorCodes.CARD_EXPIRED -> DeclineCopy("Card expired", "Ask the customer to use a different card.", false)
    ErrorCodes.CARD_NOT_ACTIVE -> DeclineCopy("Card not active", "The card has not been activated yet.", false)
    ErrorCodes.CARD_AUTH_FAILED -> DeclineCopy("Card authentication failed", "The card did not respond correctly. Try tapping again.", true)
    ErrorCodes.PIN_LOCKED -> DeclineCopy("PIN locked", "Too many wrong PIN attempts. Customer must contact Lipa to unlock.", false)
    ErrorCodes.INSUFFICIENT_BALANCE -> DeclineCopy("Insufficient balance", "The customer does not have enough funds for this amount.", true)
    ErrorCodes.LIMIT_EXCEEDED -> DeclineCopy("Limit exceeded", "This payment exceeds the customer's configured limit.", true)
    ErrorCodes.WALLET_FROZEN -> DeclineCopy("Wallet frozen", "The customer's wallet is temporarily frozen.", false)
    ErrorCodes.WALLET_SUSPENDED -> DeclineCopy("Wallet suspended", "The customer's wallet is suspended.", false)
    ErrorCodes.WALLET_CLOSED -> DeclineCopy("Wallet closed", "The customer's wallet is closed.", false)
    ErrorCodes.CARD_NOT_FOUND -> DeclineCopy("Card not recognized", "This card is not registered with Lipa.", true)
    ErrorCodes.CUSTOMER_NOT_FOUND -> DeclineCopy("Customer not found", "No customer matches this card.", false)
    else -> DeclineCopy("Declined", code ?: "Unknown error", true)
}

@Composable
fun DeclinedScreen(
    code: String?,
    message: String?,
    amount: Long,
    onRetry: () -> Unit,
    onDone: () -> Unit,
) {
    val copy = copyFor(code)
    var pop by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) { pop = 1f }
    val scale by animateFloatAsState(pop, animationSpec = tween(durationMillis = 320), label = "pop")

    Column(modifier = Modifier.fillMaxSize().background(LipaColors.Bg)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LipaColors.ErrorSoft)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(scale)
                    .background(LipaColors.Error, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = copy.title,
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    letterSpacing = (-0.5).sp,
                    color = LipaColors.ErrorInk,
                ),
            )
            Text(
                text = code ?: "ERROR",
                style = TextStyle(
                    fontFamily = LipaFonts.Mono,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = LipaColors.ErrorInk.copy(alpha = 0.7f),
                ),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            LipaCard {
                AmountDisplay(amount = amount, sizeSp = 40, label = "Not charged")
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = copy.hint,
                    style = TextStyle(
                        fontFamily = LipaFonts.Display,
                        fontSize = 14.sp,
                        color = LipaColors.Ink,
                    ),
                )
                if (message != null && message != copy.hint) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = message,
                        style = TextStyle(
                            fontFamily = LipaFonts.Mono,
                            fontSize = 12.sp,
                            color = LipaColors.InkSub,
                        ),
                    )
                }
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (copy.retryable) {
                PrimaryButton(text = "Try again", onClick = onRetry)
                PrimaryButton(text = "Cancel", onClick = onDone, tone = ButtonTone.Ghost)
            } else {
                PrimaryButton(text = "Done", onClick = onDone)
            }
        }
    }
}
