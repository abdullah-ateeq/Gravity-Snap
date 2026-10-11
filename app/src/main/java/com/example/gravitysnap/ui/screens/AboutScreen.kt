package com.example.gravitysnap.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gravitysnap.ui.components.AnimatedCorridorBackground
import com.example.gravitysnap.ui.components.GameTopBar
import com.example.gravitysnap.ui.components.GlassCard
import com.example.gravitysnap.ui.theme.GravitySnapTheme

@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    val palette = GravitySnapTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        AnimatedCorridorBackground(speedFactor = 0.4f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp)
        ) {
            GameTopBar(
                title = "ABOUT",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Game Info Card
                GlassCard(
                    cornerRadius = 20.dp,
                    padding = 20.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Gravity Snap",
                            color = palette.primaryCyan,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Version 1.0.0",
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Reverse gravity, switch between floor and ceiling, and survive an endless stream of barriers.",
                            color = palette.textPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // How to Play Card
                GlassCard(
                    cornerRadius = 20.dp,
                    padding = 20.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "HOW TO PLAY",
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        RuleBullet(text = "The cube moves automatically.")
                        RuleBullet(text = "Tap to reverse gravity.")
                        RuleBullet(text = "Run on the floor or ceiling.")
                        RuleBullet(text = "Avoid barriers.")
                        RuleBullet(text = "Survive as long as possible.")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Privacy & Offline Notice Card
                GlassCard(
                    cornerRadius = 20.dp,
                    padding = 20.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "PRIVACY & OFFLINE",
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Gravity Snap works fully offline and does not require an account.",
                            color = palette.textPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun RuleBullet(text: String) {
    val palette = GravitySnapTheme.colors

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(palette.primaryCyan)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = palette.textPrimary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}
