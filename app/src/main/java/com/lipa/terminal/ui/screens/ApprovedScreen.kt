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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.lipa.terminal.data.model.TerminalPaymentResponse
import com.lipa.terminal.ui.components.AmountDisplay
import com.lipa.terminal.ui.components.KeyValueRow
import com.lipa.terminal.ui.components.LipaCard
import com.lipa.terminal.ui.components.PrimaryButton
import com.lipa.terminal.ui.theme.KmfFormat
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun ApprovedScreen(
    result: TerminalPaymentResponse,
    cardMask: String,
    onDone: () -> Unit,
) {
    var pop by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) { pop = 1f }
    val scale by animateFloatAsState(pop, animationSpec = tween(durationMillis = 320), label = "pop")

    Column(modifier = Modifier.fillMaxSize().background(LipaColors.Bg)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(LipaColors.SuccessSoft)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(scale)
                    .background(LipaColors.Green, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(42.dp),
                )
            }
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = "Payment approved",
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp,
                    letterSpacing = (-0.5).sp,
                    color = LipaColors.GreenDeep,
                ),
            )
            if (result.completedAt != null) {
                Text(
                    text = result.completedAt.replace("T", " · ").take(19).uppercase(),
                    style = TextStyle(
                        fontFamily = LipaFonts.Mono,
                        fontSize = 11.sp,
                        letterSpacing = 0.6.sp,
                        color = LipaColors.InkSub,
                    ),
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            LipaCard {
                AmountDisplay(amount = result.requestedAmount, sizeSp = 48, label = "Charged")
                Spacer(modifier = Modifier.size(18.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LipaColors.Border))
                Spacer(modifier = Modifier.size(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    result.status?.let { KeyValueRow("Status", it.name) }
                    result.feeAmount?.let { KeyValueRow("Fee", "${KmfFormat.format(it)} KMF") }
                    result.netAmountToMerchant?.let {
                        KeyValueRow("Net to merchant", "${KmfFormat.format(it)} KMF")
                    }
                    KeyValueRow("Card", cardMask)
                    result.cardAuthMethod?.let { KeyValueRow("Card auth", it.name) }
                    result.transactionId?.let {
                        val short = it.take(8) + "…" + it.takeLast(4)
                        KeyValueRow("Transaction", short)
                    }
                    if (result.replayed == true) {
                        KeyValueRow("Replayed", "TRUE (idempotent)")
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PrimaryButton(text = "New payment", onClick = onDone)
        }
    }
}
