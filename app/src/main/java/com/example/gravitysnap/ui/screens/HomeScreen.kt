package com.example.gravitysnap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gravitysnap.ui.components.AnimatedCorridorBackground
import com.example.gravitysnap.ui.components.GlassCard
import com.example.gravitysnap.ui.components.MiniGamePreview
import com.example.gravitysnap.ui.components.PremiumButton
import com.example.gravitysnap.ui.theme.GravitySnapTheme

@Composable
fun HomeScreen(
    bestScore: Int,
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val palette = GravitySnapTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Subtle ambient moving background
        AnimatedCorridorBackground(speedFactor = 0.8f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    text = "GRAVITY SNAP",
                    color = palette.textPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "One tap changes everything.",
                    color = palette.primaryCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.8.sp
                )
            }

            // Center: Interactive Gameplay Preview
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                MiniGamePreview()

                Spacer(modifier = Modifier.height(28.dp))

                // Primary CTA: PLAY Button
                PremiumButton(
                    text = "PLAY",
                    icon = Icons.Rounded.PlayArrow,
                    onClick = onPlayClick,
                    height = 62.dp,
                    isPrimary = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Best Score Pill
                GlassCard(
                    cornerRadius = 16.dp,
                    padding = 10.dp,
                    modifier = Modifier.fillMaxWidth(0.65f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "BEST SCORE",
                            color = palette.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "$bestScore",
                            color = palette.primaryCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Bottom Navigation Bar: Home, Settings, About
            BottomNavBar(
                currentTab = BottomTab.HOME,
                onHomeClick = { /* Already Home */ },
                onSettingsClick = onSettingsClick,
                onAboutClick = onAboutClick
            )
        }
    }
}

enum class BottomTab {
    HOME, SETTINGS, ABOUT
}

@Composable
fun BottomNavBar(
    currentTab: BottomTab,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = GravitySnapTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .background(palette.cardBackground.copy(alpha = 0.85f))
            .border(width = 1.2.dp, color = palette.cardBorder, shape = RoundedCornerShape(26.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIconItem(
                icon = Icons.Rounded.Home,
                label = "Home",
                isSelected = currentTab == BottomTab.HOME,
                onClick = onHomeClick
            )

            NavIconItem(
                icon = Icons.Rounded.Settings,
                label = "Settings",
                isSelected = currentTab == BottomTab.SETTINGS,
                onClick = onSettingsClick
            )

            NavIconItem(
                icon = Icons.Rounded.Info,
                label = "About",
                isSelected = currentTab == BottomTab.ABOUT,
                onClick = onAboutClick
            )
        }
    }
}

@Composable
private fun NavIconItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val palette = GravitySnapTheme.colors
    val tint = if (isSelected) palette.primaryCyan else palette.textSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
