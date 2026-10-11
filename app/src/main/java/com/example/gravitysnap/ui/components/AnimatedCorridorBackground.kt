package com.example.gravitysnap.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.gravitysnap.ui.theme.GravitySnapTheme
import kotlin.random.Random

private data class AmbientParticle(
    val initialX: Float,
    val yFraction: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

@Composable
fun AnimatedCorridorBackground(
    modifier: Modifier = Modifier,
    speedFactor: Float = 1.0f
) {
    val palette = GravitySnapTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "bg_transition")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (4000 / speedFactor).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bg_phase"
    )

    val particles = remember {
        List(18) {
            AmbientParticle(
                initialX = Random.nextFloat(),
                yFraction = Random.nextFloat() * 0.7f + 0.15f,
                speed = Random.nextFloat() * 0.4f + 0.6f,
                size = Random.nextFloat() * 2.5f + 1.5f,
                alpha = Random.nextFloat() * 0.35f + 0.15f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Solid background
        drawRect(color = palette.background)

        // 2. Perspective grid lines
        val gridStep = 80f
        val offset = (phase * gridStep) % gridStep
        var curX = -offset
        while (curX < w + gridStep) {
            if (curX >= 0) {
                drawLine(
                    color = palette.gridLine,
                    start = Offset(curX, 0f),
                    end = Offset(curX, h),
                    strokeWidth = 1f
                )
            }
            curX += gridStep
        }

        // 3. Subtle horizontal corridor lines
        val topCorridorY = h * 0.12f
        val bottomCorridorY = h * 0.88f

        drawLine(
            color = palette.primaryCyan.copy(alpha = 0.2f),
            start = Offset(0f, topCorridorY),
            end = Offset(w, topCorridorY),
            strokeWidth = 1.5f
        )
        drawLine(
            color = palette.primaryCyan.copy(alpha = 0.2f),
            start = Offset(0f, bottomCorridorY),
            end = Offset(w, bottomCorridorY),
            strokeWidth = 1.5f
        )

        // 4. Floating ambient motes
        for (p in particles) {
            val curParticleX = ((p.initialX - phase * p.speed) % 1.0f + 1.0f) % 1.0f * w
            val curParticleY = p.yFraction * h

            drawCircle(
                color = palette.primaryCyan.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(curParticleX, curParticleY)
            )
        }
    }
}
