package com.lipa.terminal.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.components.AmountDisplay
import com.lipa.terminal.ui.components.LipaMark
import com.lipa.terminal.ui.components.NumericKeypad
import com.lipa.terminal.ui.components.PrimaryButton
import com.lipa.terminal.ui.theme.KmfFormat
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun IdleScreen(
    amount: String,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onQuickAmount: (Long) -> Unit,
    onCharge: () -> Unit,
    onOpenShiftMenu: () -> Unit,
) {
    val value = amount.toLongOrNull() ?: 0L

    Column(modifier = Modifier.fillMaxSize().background(LipaColors.Bg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LipaMark(sizeSp = 18)
                Text(
                    text = "TERMINAL",
                    style = TextStyle(
                        fontFamily = LipaFonts.Mono,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = LipaColors.InkSub,
                    ),
                )
            }
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = LipaColors.Card,
                border = BorderStroke(1.dp, LipaColors.Border),
                onClick = onOpenShiftMenu,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Shift menu",
                        tint = LipaColors.Ink,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LipaColors.Border)
                .height(1.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            AmountDisplay(
                amount = value,
                sizeSp = 56,
                label = "Amount to charge",
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(1000L, 2500L, 5000L, 10000L).forEach { q ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = LipaColors.Card,
                        border = BorderStroke(1.dp, LipaColors.Border),
                        onClick = { onQuickAmount(q) },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = KmfFormat.format(q),
                                style = TextStyle(
                                    fontFamily = LipaFonts.Mono,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.3.sp,
                                    color = LipaColors.Ink,
                                ),
                            )
                        }
                    }
                }
            }
            Text(
                text = "QUICK AMOUNTS",
                style = TextStyle(
                    fontFamily = LipaFonts.Mono,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    color = LipaColors.InkFaint,
                ),
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        NumericKeypad(
            onDigit = onDigit,
            onBackspace = onBackspace,
            submit = {
                PrimaryButton(
                    text = if (value > 0) "Charge ${KmfFormat.format(value)} KMF" else "Charge",
                    onClick = onCharge,
                    enabled = value > 0,
                )
            },
        )
    }
}
