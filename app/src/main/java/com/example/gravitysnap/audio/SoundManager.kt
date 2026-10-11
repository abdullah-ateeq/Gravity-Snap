package com.example.gravitysnap.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.example.gravitysnap.R

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<SoundType, Int>()
    private var isLoaded = false
    var isEnabled: Boolean = true

    enum class SoundType {
        SNAP,
        LAND,
        SCORE,
        CLOSE_CALL,
        HIT,
        SPEED_UP,
        CLICK
    }

    init {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.let { pool ->
                pool.setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) {
                        isLoaded = true
                    }
                }

                soundMap[SoundType.SNAP] = pool.load(context, R.raw.snap, 1)
                soundMap[SoundType.LAND] = pool.load(context, R.raw.land, 1)
                soundMap[SoundType.SCORE] = pool.load(context, R.raw.score, 1)
                soundMap[SoundType.CLOSE_CALL] = pool.load(context, R.raw.close_call, 1)
                soundMap[SoundType.HIT] = pool.load(context, R.raw.hit, 1)
                soundMap[SoundType.SPEED_UP] = pool.load(context, R.raw.speed_up, 1)
                soundMap[SoundType.CLICK] = pool.load(context, R.raw.click, 1)
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Error initializing SoundPool", e)
        }
    }

    fun play(type: SoundType, volume: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isEnabled) return
        val soundId = soundMap[type] ?: return
        try {
            soundPool?.play(soundId, volume, volume, 1, 0, pitch)
        } catch (e: Exception) {
            Log.e("SoundManager", "Error playing sound: $type", e)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
        isLoaded = false
    }
}
