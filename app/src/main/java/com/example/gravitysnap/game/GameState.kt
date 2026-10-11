package com.example.gravitysnap.game

enum class PlayState {
    READY,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class GravityDirection {
    DOWN, // Cube pulled to floor
    UP    // Cube pulled to ceiling
}

enum class PlayerState {
    FLOOR_RUNNING,
    TRANSITIONING,
    CEILING_RUNNING
}

data class FloatingNotice(
    val id: Long,
    val text: String,
    val x: Float,
    val y: Float,
    var alpha: Float = 1.0f,
    var offsetY: Float = 0f,
    val isCombo: Boolean = false
)

data class GameStats(
    val score: Int = 0,
    val bestScore: Int = 0,
    val distance: Float = 0f,
    val flips: Int = 0,
    val obstaclesCleared: Int = 0,
    val closeCalls: Int = 0,
    val combo: Int = 1,
    val maxCombo: Int = 1,
    val isNewBest: Boolean = false
)
