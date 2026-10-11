package com.example.gravitysnap.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gravitysnap.ui.components.AnimatedCorridorBackground
import com.example.gravitysnap.ui.theme.GravitySnapTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val palette = GravitySnapTheme.colors

    val cubeYFraction = remember { Animatable(0.70f) }
    val cubeRotation = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleScale = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        // Step 1: Run along floor for 0.4s
        delay(400)

        // Step 2: Gravity flip and snap to ceiling (0.45s)
        launch {
            cubeRotation.animateTo(
                targetValue = 180f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
        }
        cubeYFraction.animateTo(
            targetValue = 0.30f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )

        // Step 3: Title emerges with glowing impact
        launch {
            titleScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        titleAlpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )

        // Pause to absorb
        delay(600)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
        contentAlignment = Alignment.Center
    ) {
        AnimatedCorridorBackground(speedFactor = 1.4f)

        // Animated Splash Cube
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cubeX = w * 0.5f
            val cubeY = h * cubeYFraction.value
            val cubeSize = 44f
            val half = cubeSize / 2f

            rotate(degrees = cubeRotation.value, pivot = Offset(cubeX, cubeY)) {
                // Neon aura
                drawRoundRect(
                    color = palette.primaryCyan.copy(alpha = 0.45f),
                    topLeft = Offset(cubeX - half * 1.3f, cubeY - half * 1.3f),
                    size = Size(cubeSize * 1.3f, cubeSize * 1.3f),
                    cornerRadius = CornerRadius(10f, 10f)
                )

                // Cube body
                drawRoundRect(
                    color = palette.cardBackground,
                    topLeft = Offset(cubeX - half, cubeY - half),
                    size = Size(cubeSize, cubeSize),
                    cornerRadius = CornerRadius(8f, 8f)
                )

                // Neon outline
                drawRoundRect(
                    color = palette.primaryCyan,
                    topLeft = Offset(cubeX - half, cubeY - half),
                    size = Size(cubeSize, cubeSize),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 3.2f)
                )

                // White core
                val core = cubeSize * 0.4f
                val cHalf = core / 2f
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(cubeX - cHalf, cubeY - cHalf),
                    size = Size(core, core),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }

        // Title and Subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Spacer(modifier = Modifier.height(180.dp))

            Text(
                text = "GRAVITY SNAP",
                color = palette.primaryCyan.copy(alpha = titleAlpha.value),
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Flip. Dodge. Survive.",
                color = palette.textSecondary.copy(alpha = titleAlpha.value),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }
    }
}
