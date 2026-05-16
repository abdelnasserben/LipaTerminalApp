package com.lipa.terminal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lipa.terminal.ui.theme.LipaColors
import com.lipa.terminal.ui.theme.LipaFonts

enum class TopBarTone { Plain, Success, Warning, Error }

@Composable
fun TopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    tone: TopBarTone = TopBarTone.Plain,
) {
    val bg = when (tone) {
        TopBarTone.Plain -> LipaColors.Bg
        TopBarTone.Success -> LipaColors.SuccessSoft
        TopBarTone.Warning -> LipaColors.WarningSoft
        TopBarTone.Error -> LipaColors.ErrorSoft
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(start = 12.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LipaColors.Ink,
                )
            }
        } else {
            Box(modifier = Modifier.size(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            if (subtitle != null) {
                Text(
                    text = subtitle.uppercase(),
                    style = TextStyle(
                        fontFamily = LipaFonts.Mono,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        color = LipaColors.InkSub,
                    ),
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = LipaFonts.Display,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.4).sp,
                    color = LipaColors.Ink,
                ),
            )
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(LipaColors.Border))
}
