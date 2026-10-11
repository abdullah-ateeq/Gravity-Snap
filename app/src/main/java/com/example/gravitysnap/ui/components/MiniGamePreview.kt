package com.example.gravitysnap.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gravitysnap.ui.theme.GravitySnapTheme

@Composable
fun MiniGamePreview(
    modifier: Modifier = Modifier
) {
    val palette = GravitySnapTheme.colors
    val transition = rememberInfiniteTransition(label = "preview_loop")

    // Full loop of 3.4 seconds
    val loopProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "loop_progress"
    )

    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(shape)
            .border(width = 1.5.dp, color = palette.primaryCyan.copy(alpha = 0.5f), shape = shape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val floorY = h * 0.82f
            val ceilingY = h * 0.18f
            val corridorH = floorY - ceilingY

            // 1. Dark background
            drawRect(color = palette.backgroundElevated)

            // 2. Moving grid
            val gridStep = 45f
            val gridOffset = (loopProgress * 3400f * 0.15f) % gridStep
            var gx = -gridOffset
            while (gx < w + gridStep) {
                if (gx >= 0) {
                    drawLine(
                        color = palette.gridLine,
                        start = Offset(gx, ceilingY),
                        end = Offset(gx, floorY),
                        strokeWidth = 1f
                    )
                }
                gx += gridStep
            }

            // 3. Surfaces
            // Ceiling
            drawRect(
                color = palette.corridorSurface,
                topLeft = Offset(0f, 0f),
                size = Size(w, ceilingY)
            )
            drawLine(
                color = palette.primaryCyan.copy(alpha = 0.7f),
                start = Offset(0f, ceilingY),
                end = Offset(w, ceilingY),
                strokeWidth = 2.5f
            )

            // Floor
            drawRect(
                color = palette.corridorSurface,
                topLeft = Offset(0f, floorY),
                size = Size(w, h - floorY)
            )
            drawLine(
                color = palette.primaryCyan.copy(alpha = 0.7f),
                start = Offset(0f, floorY),
                end = Offset(w, floorY),
                strokeWidth = 2.5f
            )

            // 4. Cube Position calculation in the preview loop:
            // 0.00 - 0.28: Running on floor
            // 0.28 - 0.38: Snapping upward to ceiling (transition)
            // 0.38 - 0.72: Running on ceiling
            // 0.72 - 0.82: Snapping downward to floor (transition)
            // 0.82 - 1.00: Running on floor
            val cubeX = w * 0.24f
            val cubeSize = 28f
            val half = cubeSize / 2f
            val floorCubeY = floorY - half
            val ceilCubeY = ceilingY + half

            val cubeY: Float
            val stretchX: Float
            val stretchY: Float
            val rotation: Float

            when {
                loopProgress < 0.28f -> {
                    cubeY = floorCubeY
                    stretchX = 1f
                    stretchY = 1f
                    rotation = 0f
                }
                loopProgress in 0.28f..0.38f -> {
                    val t = (loopProgress - 0.28f) / 0.10f
                    // Smooth snap interpolation
                    cubeY = floorCubeY + (ceilCubeY - floorCubeY) * (t * t * (3 - 2 * t))
                    stretchY = 1.4f
                    stretchX = 0.72f
                    rotation = t * 90f
                }
                loopProgress in 0.38f..0.72f -> {
                    cubeY = ceilCubeY
                    stretchX = 1f
                    stretchY = 1f
                    rotation = 90f
                }
                loopProgress in 0.72f..0.82f -> {
                    val t = (loopProgress - 0.72f) / 0.10f
                    cubeY = ceilCubeY + (floorCubeY - ceilCubeY) * (t * t * (3 - 2 * t))
                    stretchY = 1.4f
                    stretchX = 0.72f
                    rotation = 90f - t * 90f
                }
                else -> {
                    cubeY = floorCubeY
                    stretchX = 1f
                    stretchY = 1f
                    rotation = 0f
                }
            }

            // 5. Draw approaching obstacles
            // Obstacle 1 (Floor): approaches and passes player around progress 0.33
            val obs1X = w * 1.1f - (loopProgress / 0.5f) * (w * 1.3f)
            if (obs1X in -50f..(w + 50f)) {
                val obsW = 40f
                val obsH = corridorH * 0.38f
                drawRoundRect(
                    color = palette.hazardRed,
                    topLeft = Offset(obs1X, floorY - obsH),
                    size = Size(obsW, obsH),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 2.5f)
                )
            }

            // Obstacle 2 (Ceiling): approaches and passes around progress 0.77
            val obs2Progress = (loopProgress - 0.44f).let { if (it < 0f) it + 1f else it }
            val obs2X = w * 1.1f - (obs2Progress / 0.5f) * (w * 1.3f)
            if (obs2X in -50f..(w + 50f)) {
                val obsW = 40f
                val obsH = corridorH * 0.38f
                drawRoundRect(
                    color = palette.hazardOrange,
                    topLeft = Offset(obs2X, ceilingY),
                    size = Size(obsW, obsH),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(width = 2.5f)
                )
            }

            // 6. Draw Preview Cube
            rotate(degrees = rotation, pivot = Offset(cubeX, cubeY)) {
                scale(scaleX = stretchX, scaleY = stretchY, pivot = Offset(cubeX, cubeY)) {
                    // Outer glow
                    drawRoundRect(
                        color = palette.primaryCyan.copy(alpha = 0.4f),
                        topLeft = Offset(cubeX - half * 1.25f, cubeY - half * 1.25f),
                        size = Size(cubeSize * 1.25f, cubeSize * 1.25f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    // Body
                    drawRoundRect(
                        color = palette.primaryCyan,
                        topLeft = Offset(cubeX - half, cubeY - half),
                        size = Size(cubeSize, cubeSize),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    // White core
                    val coreHalf = half * 0.45f
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(cubeX - coreHalf, cubeY - coreHalf),
                        size = Size(coreHalf * 2, coreHalf * 2),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
            }
        }

        // Live preview pill badge
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .border(
                    width = 1.dp,
                    color = palette.primaryCyan.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(bottomStart = 12.dp)
                )
                .clip(RoundedCornerShape(bottomStart = 12.dp))
                .background(palette.cardBackground.copy(alpha = 0.8f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "AUTO PREVIEW",
                color = palette.primaryCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}
