package com.example.gravitysnap.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gravitysnap.game.GameEngine
import com.example.gravitysnap.game.PlayState
import com.example.gravitysnap.ui.components.GameHud
import com.example.gravitysnap.ui.components.GlassCard
import com.example.gravitysnap.ui.components.PremiumButton
import com.example.gravitysnap.ui.theme.GravitySnapTheme

@Composable
fun GameScreen(
    engine: GameEngine,
    particlesEnabled: Boolean,
    reducedEffects: Boolean,
    onGameOverFinished: () -> Unit,
    onHomeClick: () -> Unit
) {
    val palette = GravitySnapTheme.colors
    var frameTick by remember { mutableLongStateOf(0L) }

    // Frame-based game loop with delta time (nanoseconds)
    LaunchedEffect(Unit) {
        var lastFrameTimeNanos = 0L

        while (true) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameTimeNanos != 0L) {
                    val dtNanos = frameTimeNanos - lastFrameTimeNanos
                    // Clamp delta time to avoid large physics steps on frame drops (max 50ms)
                    val dt = (dtNanos / 1_000_000_000f).coerceIn(0f, 0.05f)

                    engine.update(
                        dt = dt,
                        particlesEnabled = particlesEnabled,
                        reducedEffects = reducedEffects
                    )

                    if (engine.state == PlayState.GAME_OVER && engine.isGameOverSequenceFinished) {
                        onGameOverFinished()
                    }
                }
                lastFrameTimeNanos = frameTimeNanos
                frameTick++
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                engine.handleTap()
            }
            .onSizeChanged { size ->
                if (size.width > 0 && size.height > 0) {
                    engine.setDimensions(size.width.toFloat(), size.height.toFloat())
                }
            }
    ) {
        // Main Game Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Force redraw on each frame tick
            @Suppress("UNUSED_VARIABLE")
            val tick = frameTick
            engine.draw(this, palette)
        }

        // Top Game HUD
        GameHud(
            score = engine.stats.score,
            bestScore = engine.stats.bestScore,
            gravityDirection = engine.player.gravityDirection,
            onPauseClick = { engine.togglePause(true) },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // READY: Tap to Start prompt
        AnimatedVisibility(
            visible = engine.state == PlayState.READY,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            ReadyOverlay()
        }

        // COUNTDOWN: 3, 2, 1, SNAP!
        AnimatedVisibility(
            visible = engine.state == PlayState.COUNTDOWN,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            CountdownOverlay(text = engine.countdownDisplay)
        }

        // Speed Milestone Announcement
        AnimatedVisibility(
            visible = engine.speedMilestoneText != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
        ) {
            SpeedMilestoneBanner(text = engine.speedMilestoneText ?: "")
        }

        // Floating Close Call notices
        for (notice in engine.floatingNotices) {
            Text(
                text = notice.text,
                color = if (notice.isCombo) palette.accentViolet else Color(0xFFFBBF24),
                fontSize = if (notice.isCombo) 20.sp else 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .offset { IntOffset(notice.x.toInt(), (notice.y + notice.offsetY).toInt()) }
                    .alpha(notice.alpha)
            )
        }

        // First-Run Tutorial Overlay
        AnimatedVisibility(
            visible = engine.isTutorialActive && engine.state == PlayState.PLAYING,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
        ) {
            TutorialPrompt()
        }

        // PAUSE Menu Modal
        AnimatedVisibility(
            visible = engine.state == PlayState.PAUSED,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            PauseMenuDialog(
                onResume = { engine.togglePause(false) },
                onRestart = { engine.prepareGame(engine.stats.bestScore, !engine.isTutorialActive) },
                onHome = onHomeClick
            )
        }
    }
}

@Composable
private fun ReadyOverlay() {
    val palette = GravitySnapTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "tap_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ready_pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        Icon(
            imageVector = Icons.Rounded.TouchApp,
            contentDescription = null,
            tint = palette.primaryCyan,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "TAP ANYWHERE TO START",
            color = palette.textPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun CountdownOverlay(text: String) {
    val palette = GravitySnapTheme.colors
    val scale by animateFloatAsState(
        targetValue = 1.15f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "cd_scale"
    )

    Text(
        text = text,
        color = if (text == "SNAP!") palette.primaryCyan else palette.textPrimary,
        fontSize = if (text == "SNAP!") 52.sp else 64.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        modifier = Modifier.scale(scale)
    )
}

@Composable
private fun SpeedMilestoneBanner(text: String) {
    val palette = GravitySnapTheme.colors
    GlassCard(
        cornerRadius = 16.dp,
        borderColor = palette.primaryCyan,
        borderWidth = 1.5.dp,
        padding = 8.dp
    ) {
        Text(
            text = text,
            color = palette.primaryCyan,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TutorialPrompt() {
    val palette = GravitySnapTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "tut_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tut_scale"
    )

    GlassCard(
        cornerRadius = 16.dp,
        borderColor = palette.primaryCyan,
        padding = 12.dp,
        modifier = Modifier.scale(scale)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.TouchApp,
                contentDescription = null,
                tint = palette.primaryCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "TAP TO FLIP GRAVITY",
                color = palette.primaryCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun PauseMenuDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    val palette = GravitySnapTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            cornerRadius = 24.dp,
            borderColor = palette.cardBorder,
            borderWidth = 1.5.dp,
            padding = 28.dp,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    color = palette.textPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                PremiumButton(
                    text = "RESUME",
                    icon = Icons.Rounded.PlayArrow,
                    onClick = onResume,
                    height = 52.dp,
                    isPrimary = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                PremiumButton(
                    text = "RESTART",
                    icon = Icons.Rounded.Refresh,
                    onClick = onRestart,
                    height = 52.dp,
                    isPrimary = false
                )

                Spacer(modifier = Modifier.height(14.dp))

                PremiumButton(
                    text = "HOME",
                    icon = Icons.Rounded.Home,
                    onClick = onHome,
                    height = 52.dp,
                    isPrimary = false
                )
            }
        }
    }
}
