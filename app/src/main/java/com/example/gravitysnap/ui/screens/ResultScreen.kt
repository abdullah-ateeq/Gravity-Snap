package com.example.gravitysnap.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gravitysnap.game.GameStats
import com.example.gravitysnap.ui.components.AnimatedCorridorBackground
import com.example.gravitysnap.ui.components.GlassCard
import com.example.gravitysnap.ui.components.PremiumButton
import com.example.gravitysnap.ui.theme.GravitySnapTheme

@Composable
fun ResultScreen(
    stats: GameStats,
    onPlayAgainClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    val palette = GravitySnapTheme.colors
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        AnimatedCorridorBackground(speedFactor = 0.5f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 44.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: RUN OVER
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = "RUN OVER",
                    color = palette.hazardRed,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // NEW BEST Badge
                if (stats.isNewBest) {
                    NewBestBadge()
                }
            }

            // Center Score Card & Statistics
            GlassCard(
                cornerRadius = 24.dp,
                borderColor = if (stats.isNewBest) palette.primaryCyan else palette.cardBorder,
                borderWidth = 1.5.dp,
                padding = 24.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SCORE",
                        color = palette.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Text(
                        text = "${stats.score}",
                        color = palette.textPrimary,
                        fontSize = 58.sp,
                        fontWeight = FontWeight.Black
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "BEST SCORE",
                            color = palette.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${stats.bestScore}",
                            color = palette.primaryCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 Key Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "DISTANCE",
                            value = "${(stats.distance / 12).toInt()}m"
                        )
                        StatItem(
                            label = "FLIPS",
                            value = "${stats.flips}"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "CLEARED",
                            value = "${stats.obstaclesCleared}"
                        )
                        StatItem(
                            label = "CLOSE CALLS",
                            value = "${stats.closeCalls}"
                        )
                    }
                }
            }

            // Action Buttons: PLAY AGAIN, HOME, SHARE
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PremiumButton(
                    text = "PLAY AGAIN",
                    icon = Icons.Rounded.Refresh,
                    onClick = onPlayAgainClick,
                    height = 58.dp,
                    isPrimary = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PremiumButton(
                        text = "HOME",
                        icon = Icons.Rounded.Home,
                        onClick = onHomeClick,
                        isPrimary = false,
                        height = 50.dp,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    PremiumButton(
                        text = "SHARE",
                        icon = Icons.Rounded.Share,
                        onClick = { shareScore(context, stats.score) },
                        isPrimary = false,
                        height = 50.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NewBestBadge() {
    val palette = GravitySnapTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "new_best_glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "best_badge_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.primaryCyan.copy(alpha = 0.2f))
            .border(width = 1.2.dp, color = palette.primaryCyan, shape = RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = palette.primaryCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "NEW BEST!",
                color = palette.primaryCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    val palette = GravitySnapTheme.colors

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = palette.textSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            color = palette.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun shareScore(context: Context, score: Int) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "I survived and scored $score in Gravity Snap! Can you beat my run? #GravitySnap"
        )
    }
    context.startActivity(Intent.createChooser(intent, "Share your score"))
}
