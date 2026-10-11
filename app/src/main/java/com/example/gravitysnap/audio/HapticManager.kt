package com.example.gravitysnap.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(private val context: Context) {

    var isEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    enum class HapticType {
        GRAVITY_FLIP,
        SURFACE_LANDING,
        CLOSE_CALL,
        COLLISION,
        BUTTON_TAP
    }

    fun play(type: HapticType) {
        if (!isEnabled) return
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = when (type) {
                    HapticType.GRAVITY_FLIP -> {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    }
                    HapticType.SURFACE_LANDING -> {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    }
                    HapticType.CLOSE_CALL -> {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    }
                    HapticType.COLLISION -> {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    }
                    HapticType.BUTTON_TAP -> {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    }
                }
                vib.vibrate(effect)
            } else {
                val durationMs = when (type) {
                    HapticType.GRAVITY_FLIP -> 18L
                    HapticType.SURFACE_LANDING -> 10L
                    HapticType.CLOSE_CALL -> 25L
                    HapticType.COLLISION -> 80L
                    HapticType.BUTTON_TAP -> 12L
                }
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Silently ignore if device doesn't support the requested haptic effect
        }
    }
}
