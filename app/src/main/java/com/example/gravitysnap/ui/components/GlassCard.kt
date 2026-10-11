package com.example.gravitysnap.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gravitysnap.ui.theme.GravitySnapTheme

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    padding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = GravitySnapTheme.colors
    val effectiveBorder = borderColor ?: palette.cardBorder
    val shape = RoundedCornerShape(cornerRadius)

    val bgColor = if (palette.isDark) {
        palette.cardBackground.copy(alpha = 0.85f)
    } else {
        palette.cardBackground.copy(alpha = 0.95f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(width = borderWidth, color = effectiveBorder, shape = shape)
            .padding(padding),
        content = content
    )
}
