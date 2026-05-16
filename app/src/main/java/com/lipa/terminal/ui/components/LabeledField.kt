package com.lipa.terminal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

@Composable
fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = TextStyle(
            fontFamily = LipaFonts.Mono,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            color = LipaColors.InkSub,
        ),
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    mono: Boolean = false,
    secret: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
) {
    val family: FontFamily = if (mono) LipaFonts.Mono else LipaFonts.Display
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(LipaColors.Card, RoundedCornerShape(12.dp))
            .border(1.dp, LipaColors.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                fontFamily = family,
                fontSize = 16.sp,
                color = LipaColors.Ink,
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(LipaColors.Ink),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontFamily = family,
                            fontSize = 16.sp,
                            color = LipaColors.InkFaint,
                        ),
                    )
                }
                inner()
            },
        )
    }
}
