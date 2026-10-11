package com.example.gravitysnap.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gravitysnap.game.GravityDirection
import com.example.gravitysnap.ui.theme.GravitySnapTheme

@Composable
fun GameHud(
    score: Int,
    bestScore: Int,
    gravityDirection: GravityDirection,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = GravitySnapTheme.colors

    // Score bounce animation on change
    val scoreScale by animateFloatAsState(
        targetValue = 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "score_scale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Top-left: BEST score
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "BEST",
                color = palette.textSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "$bestScore",
                color = palette.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        // Top-center: Current Score + subtle gravity direction arrow
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scoreScale)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Subtle gravity direction indicator
                val arrowIcon = if (gravityDirection == GravityDirection.UP) {
                    Icons.Rounded.KeyboardArrowUp
                } else {
                    Icons.Rounded.KeyboardArrowDown
                }

                Icon(
                    imageVector = arrowIcon,
                    contentDescription = "Gravity Direction",
                    tint = palette.primaryCyan,
                    modifier = Modifier.size(26.dp)
                )

                Text(
                    text = "$score",
                    color = palette.textPrimary,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }

        // Top-right: Pause button
        NeonIconButton(
            icon = Icons.Rounded.Pause,
            contentDescription = "Pause Game",
            onClick = onPauseClick,
            size = 44.dp,
            iconSize = 22.dp
        )
    }
}
