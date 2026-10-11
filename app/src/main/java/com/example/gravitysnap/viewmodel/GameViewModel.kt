package com.example.gravitysnap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gravitysnap.audio.HapticManager
import com.example.gravitysnap.audio.SoundManager
import com.example.gravitysnap.data.PreferencesManager
import com.example.gravitysnap.data.UserPreferences
import com.example.gravitysnap.game.GameEngine
import com.example.gravitysnap.game.PlayState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesManager = PreferencesManager(application)
    val soundManager = SoundManager(application)
    val hapticManager = HapticManager(application)

    val engine = GameEngine(soundManager, hapticManager)

    val userPreferences: StateFlow<UserPreferences> = preferencesManager.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    private val _navigateToResult = MutableStateFlow(false)
    val navigateToResult: StateFlow<Boolean> = _navigateToResult

    init {
        viewModelScope.launch {
            userPreferences.collect { prefs ->
                soundManager.isEnabled = prefs.soundEnabled
                hapticManager.isEnabled = prefs.hapticsEnabled
            }
        }

        engine.onTutorialCompleted = {
            viewModelScope.launch {
                preferencesManager.setTutorialCompleted(true)
            }
        }
    }

    fun startNewGame() {
        _navigateToResult.value = false
        val currentPrefs = userPreferences.value
        engine.prepareGame(
            bestScore = currentPrefs.bestScore,
            tutorialCompleted = currentPrefs.tutorialCompleted
        )
    }

    fun handleTap() {
        engine.handleTap()
    }

    fun pauseGame() {
        engine.togglePause(true)
    }

    fun resumeGame() {
        engine.togglePause(false)
    }

    fun checkGameOverTransition() {
        if (engine.state == PlayState.GAME_OVER && engine.isGameOverSequenceFinished && !_navigateToResult.value) {
            _navigateToResult.value = true
            // Save best score if beaten
            viewModelScope.launch {
                preferencesManager.updateBestScore(engine.stats.score)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
