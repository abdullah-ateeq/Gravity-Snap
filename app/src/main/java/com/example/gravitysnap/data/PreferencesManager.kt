package com.example.gravitysnap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gravity_snap_settings")

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

data class UserPreferences(
    val bestScore: Int = 0,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val particlesEnabled: Boolean = true,
    val reducedEffects: Boolean = false,
    val tutorialCompleted: Boolean = false
)

class PreferencesManager(private val context: Context) {

    private object PreferencesKeys {
        val BEST_SCORE = intPreferencesKey("best_score")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val PARTICLES_ENABLED = booleanPreferencesKey("particles_enabled")
        val REDUCED_EFFECTS = booleanPreferencesKey("reduced_effects")
        val TUTORIAL_COMPLETED = booleanPreferencesKey("tutorial_completed")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            val bestScore = preferences[PreferencesKeys.BEST_SCORE] ?: 0
            val themeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
            val themeMode = try {
                ThemeMode.valueOf(themeString)
            } catch (e: Exception) {
                ThemeMode.DARK
            }
            val soundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true
            val hapticsEnabled = preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
            val particlesEnabled = preferences[PreferencesKeys.PARTICLES_ENABLED] ?: true
            val reducedEffects = preferences[PreferencesKeys.REDUCED_EFFECTS] ?: false
            val tutorialCompleted = preferences[PreferencesKeys.TUTORIAL_COMPLETED] ?: false

            UserPreferences(
                bestScore = bestScore,
                themeMode = themeMode,
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                particlesEnabled = particlesEnabled,
                reducedEffects = reducedEffects,
                tutorialCompleted = tutorialCompleted
            )
        }

    suspend fun updateBestScore(newScore: Int) {
        context.dataStore.edit { preferences ->
            val currentBest = preferences[PreferencesKeys.BEST_SCORE] ?: 0
            if (newScore > currentBest) {
                preferences[PreferencesKeys.BEST_SCORE] = newScore
            }
        }
    }

    suspend fun resetBestScore() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BEST_SCORE] = 0
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setParticlesEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PARTICLES_ENABLED] = enabled
        }
    }

    suspend fun setReducedEffects(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REDUCED_EFFECTS] = enabled
        }
    }

    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TUTORIAL_COMPLETED] = completed
        }
    }
}
