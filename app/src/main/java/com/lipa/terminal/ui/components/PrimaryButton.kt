package com.lipa.terminal.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

enum class ButtonTone { Green, Dark, Ghost }

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tone: ButtonTone = ButtonTone.Green,
) {
    val style = TextStyle(
        fontFamily = LipaFonts.Display,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        letterSpacing = (-0.2).sp,
    )
    val shape = RoundedCornerShape(14.dp)
    val rowMod = modifier
        .fillMaxWidth()
        .height(64.dp)

    when (tone) {
        ButtonTone.Green -> Button(
            onClick = onClick,
            enabled = enabled,
            modifier = rowMod,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = LipaColors.Green,
                contentColor = androidx.compose.ui.graphics.Color.White,
                disabledContainerColor = LipaColors.BgDeep,
                disabledContentColor = LipaColors.InkFaint,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(text, style = style)
            }
        }
        ButtonTone.Dark -> Button(
            onClick = onClick,
            enabled = enabled,
            modifier = rowMod,
            shape = shape,
            colors = ButtonDefaults.buttonColors(
                containerColor = LipaColors.Ink,
                contentColor = androidx.compose.ui.graphics.Color.White,
                disabledContainerColor = LipaColors.BgDeep,
                disabledContentColor = LipaColors.InkFaint,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(text, style = style)
            }
        }
        ButtonTone.Ghost -> OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = rowMod,
            shape = shape,
            border = BorderStroke(1.dp, LipaColors.Border),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = LipaColors.Ink,
                disabledContentColor = LipaColors.InkFaint,
            ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(text, style = style)
            }
        }
    }
}
