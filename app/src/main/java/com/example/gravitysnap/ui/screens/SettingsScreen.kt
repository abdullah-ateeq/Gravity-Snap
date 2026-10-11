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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gravitysnap.data.ThemeMode
import com.example.gravitysnap.ui.components.AnimatedCorridorBackground
import com.example.gravitysnap.ui.components.ConfirmDialog
import com.example.gravitysnap.ui.components.GameTopBar
import com.example.gravitysnap.ui.components.GlassCard
import com.example.gravitysnap.ui.components.PremiumButton
import com.example.gravitysnap.ui.components.SettingToggleRow
import com.example.gravitysnap.ui.theme.GravitySnapTheme
import com.example.gravitysnap.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val palette = GravitySnapTheme.colors
    val prefs by viewModel.userPreferences.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }

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
                title = "SETTINGS",
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Theme Selection Card
                GlassCard(
                    cornerRadius = 20.dp,
                    padding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "THEME",
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeSelectorPill(
                                label = "DARK",
                                isSelected = prefs.themeMode == ThemeMode.DARK,
                                onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                                modifier = Modifier.weight(1f)
                            )

                            ThemeSelectorPill(
                                label = "LIGHT",
                                isSelected = prefs.themeMode == ThemeMode.LIGHT,
                                onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                                modifier = Modifier.weight(1f)
                            )

                            ThemeSelectorPill(
                                label = "SYSTEM",
                                isSelected = prefs.themeMode == ThemeMode.SYSTEM,
                                onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Audio & Haptics Card
                GlassCard(
                    cornerRadius = 20.dp,
                    padding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "AUDIO & FEEDBACK",
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingToggleRow(
                            title = "Sound Effects",
                            subtitle = "Futuristic snap, impact, and crystal clear cues",
                            checked = prefs.soundEnabled,
                            onCheckedChange = { viewModel.setSoundEnabled(it) }
                        )

                        SettingToggleRow(
                            title = "Haptic Feedback",
                            subtitle = "Tactile impact response for flips and landings",
                            checked = prefs.hapticsEnabled,
                            onCheckedChange = { viewModel.setHapticsEnabled(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Visual Effects Card
                GlassCard(
                    cornerRadius = 20.dp,
                    padding = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "GRAPHICS & PERFORMANCE",
                            color = palette.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingToggleRow(
                            title = "Particle Effects",
                            subtitle = "Glowing spark trails, geometric shards, and motes",
                            checked = prefs.particlesEnabled,
                            onCheckedChange = { viewModel.setParticlesEnabled(it) }
                        )

                        SettingToggleRow(
                            title = "Reduced Effects",
                            subtitle = "Minimal screen pulse and motion trail for battery saving",
                            checked = prefs.reducedEffects,
                            onCheckedChange = { viewModel.setReducedEffects(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Reset Best Score Button
                PremiumButton(
                    text = "RESET BEST SCORE",
                    onClick = { showResetConfirm = true },
                    isPrimary = false,
                    danger = true,
                    height = 50.dp
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // Reset Confirmation Dialog
        if (showResetConfirm) {
            ConfirmDialog(
                title = "Reset Best Score?",
                message = "This will permanently clear your current best score of ${prefs.bestScore}. This action cannot be undone.",
                confirmText = "RESET",
                cancelText = "CANCEL",
                isDanger = true,
                onConfirm = {
                    viewModel.resetBestScore()
                    showResetConfirm = false
                },
                onDismiss = { showResetConfirm = false }
            )
        }
    }
}

@Composable
private fun ThemeSelectorPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = GravitySnapTheme.colors
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (isSelected) palette.primaryCyan else palette.cardBackground
            )
            .border(
                width = 1.dp,
                color = if (isSelected) palette.primaryCyan else palette.cardBorder,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) {
                if (palette.isDark) palette.background else palette.textPrimary
            } else palette.textSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            letterSpacing = 0.8.sp
        )
    }
}
