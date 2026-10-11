package com.example.gravitysnap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gravitysnap.audio.HapticManager
import com.example.gravitysnap.audio.SoundManager
import com.example.gravitysnap.data.PreferencesManager
import com.example.gravitysnap.data.ThemeMode
import com.example.gravitysnap.data.UserPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val soundManager = SoundManager(application)
    private val hapticManager = HapticManager(application)

    val userPreferences: StateFlow<UserPreferences> = preferencesManager.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
        playClickFeedback()
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSoundEnabled(enabled)
        }
        playClickFeedback()
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHapticsEnabled(enabled)
        }
        playClickFeedback()
    }

    fun setParticlesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setParticlesEnabled(enabled)
        }
        playClickFeedback()
    }

    fun setReducedEffects(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setReducedEffects(enabled)
        }
        playClickFeedback()
    }

    fun resetBestScore() {
        viewModelScope.launch {
            preferencesManager.resetBestScore()
        }
        playClickFeedback()
    }

    private fun playClickFeedback() {
        soundManager.play(SoundManager.SoundType.CLICK)
        hapticManager.play(HapticManager.HapticType.BUTTON_TAP)
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
