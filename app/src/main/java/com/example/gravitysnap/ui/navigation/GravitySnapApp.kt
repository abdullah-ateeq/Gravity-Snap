package com.example.gravitysnap.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gravitysnap.ui.screens.AboutScreen
import com.example.gravitysnap.ui.screens.GameScreen
import com.example.gravitysnap.ui.screens.HomeScreen
import com.example.gravitysnap.ui.screens.ResultScreen
import com.example.gravitysnap.ui.screens.SettingsScreen
import com.example.gravitysnap.ui.screens.SplashScreen
import com.example.gravitysnap.ui.theme.GravitySnapTheme
import com.example.gravitysnap.viewmodel.GameViewModel
import com.example.gravitysnap.viewmodel.SettingsViewModel

enum class Screen {
    SPLASH,
    HOME,
    GAME,
    RESULT,
    SETTINGS,
    ABOUT
}

@Composable
fun GravitySnapApp(
    gameViewModel: GameViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
    val userPrefs by gameViewModel.userPreferences.collectAsState()

    GravitySnapTheme(themeMode = userPrefs.themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = GravitySnapTheme.colors.background
        ) {
            // Handle system back button gracefully
            BackHandler(enabled = currentScreen != Screen.HOME && currentScreen != Screen.SPLASH) {
                when (currentScreen) {
                    Screen.GAME -> {
                        // Pause game instead of instantly leaving, or leave if paused
                        if (gameViewModel.engine.state == com.example.gravitysnap.game.PlayState.PLAYING) {
                            gameViewModel.pauseGame()
                        } else {
                            currentScreen = Screen.HOME
                        }
                    }
                    Screen.RESULT, Screen.SETTINGS, Screen.ABOUT -> {
                        currentScreen = Screen.HOME
                    }
                    else -> {}
                }
            }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    when {
                        targetState == Screen.GAME -> {
                            (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.94f, animationSpec = tween(220)))
                                .togetherWith(fadeOut(animationSpec = tween(180)) + scaleOut(targetScale = 1.04f, animationSpec = tween(180)))
                        }
                        initialState == Screen.GAME && targetState == Screen.RESULT -> {
                            fadeIn(animationSpec = tween(300))
                                .togetherWith(fadeOut(animationSpec = tween(250)))
                        }
                        else -> {
                            fadeIn(animationSpec = tween(200))
                                .togetherWith(fadeOut(animationSpec = tween(200)))
                        }
                    }
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    Screen.SPLASH -> {
                        SplashScreen(
                            onSplashFinished = {
                                currentScreen = Screen.HOME
                            }
                        )
                    }

                    Screen.HOME -> {
                        HomeScreen(
                            bestScore = userPrefs.bestScore,
                            onPlayClick = {
                                gameViewModel.startNewGame()
                                currentScreen = Screen.GAME
                            },
                            onSettingsClick = {
                                currentScreen = Screen.SETTINGS
                            },
                            onAboutClick = {
                                currentScreen = Screen.ABOUT
                            }
                        )
                    }

                    Screen.GAME -> {
                        GameScreen(
                            engine = gameViewModel.engine,
                            particlesEnabled = userPrefs.particlesEnabled,
                            reducedEffects = userPrefs.reducedEffects,
                            onGameOverFinished = {
                                gameViewModel.checkGameOverTransition()
                                currentScreen = Screen.RESULT
                            },
                            onHomeClick = {
                                currentScreen = Screen.HOME
                            }
                        )
                    }

                    Screen.RESULT -> {
                        ResultScreen(
                            stats = gameViewModel.engine.stats,
                            onPlayAgainClick = {
                                gameViewModel.startNewGame()
                                currentScreen = Screen.GAME
                            },
                            onHomeClick = {
                                currentScreen = Screen.HOME
                            }
                        )
                    }

                    Screen.SETTINGS -> {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBackClick = {
                                currentScreen = Screen.HOME
                            }
                        )
                    }

                    Screen.ABOUT -> {
                        AboutScreen(
                            onBackClick = {
                                currentScreen = Screen.HOME
                            }
                        )
                    }
                }
            }
        }
    }
}
