package com.lipa.terminal.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.theme.KmfFormat
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

enum class AmountAlign { Start, Center }

@Composable
fun AmountDisplay(
    amount: Long,
    modifier: Modifier = Modifier,
    sizeSp: Int = 64,
    label: String = "Amount",
    currency: String = "KMF",
    align: AmountAlign = AmountAlign.Start,
) {
    val arrangement = if (align == AmountAlign.Center) Arrangement.Center else Arrangement.Start
    val textAlign = if (align == AmountAlign.Center) TextAlign.Center else TextAlign.Start
    val display = if (amount == 0L) "0" else KmfFormat.format(amount)

    Column(modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (align == AmountAlign.Center) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontFamily = LipaFonts.Mono,
                fontSize = 11.sp,
                letterSpacing = 1.4.sp,
                color = LipaColors.InkSub,
                textAlign = textAlign,
            ),
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Row(
            horizontalArrangement = arrangement,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = display,
                style = TextStyle(
                    fontFamily = LipaFonts.Mono,
                    fontSize = sizeSp.sp,
                    letterSpacing = (-1.5).sp,
                    color = LipaColors.Ink,
                    textAlign = textAlign,
                ),
            )
            Text(
                text = " " + currency,
                style = TextStyle(
                    fontFamily = LipaFonts.Mono,
                    fontSize = (sizeSp * 0.28).toInt().sp,
                    letterSpacing = 1.sp,
                    color = LipaColors.InkSub,
                ),
            )
        }
    }
}
