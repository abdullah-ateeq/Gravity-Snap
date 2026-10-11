package com.example.gravitysnap.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.gravitysnap.audio.HapticManager
import com.example.gravitysnap.audio.SoundManager
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameEngine(
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager
) {
    var state: PlayState by mutableStateOf(PlayState.READY)
    val player = Player()
    val particleSystem = ParticleSystem()
    val obstacles = ArrayList<Obstacle>(16)

    // Layout dimensions (updated on draw)
    var screenWidth: Float = 1080f
    var screenHeight: Float = 1920f
    var corridorTop: Float = 180f
    var corridorBottom: Float = 1740f

    // Speed & World
    var baseSpeed: Float = 380f
    var currentSpeed: Float = 380f
    private var nextObstacleId: Long = 1L
    private var distanceAccumulator: Float = 0f

    // Statistics
    var stats: GameStats by mutableStateOf(GameStats())
        private set

    // Speed milestone triggers
    private val milestonesPassed = mutableSetOf<Int>()
    var speedMilestoneText: String? by mutableStateOf(null)
    var speedMilestoneTimer: Float = 0f

    // Close call & combo notices
    val floatingNotices = mutableStateListOf<FloatingNotice>()
    private var nextNoticeId: Long = 1L

    // Effects
    var screenPulse: Float = 0f
    var cameraShakeIntensity: Float = 0f
    var cameraShakeX: Float = 0f
    var cameraShakeY: Float = 0f
    var corridorGlowBoost: Float = 1.0f

    // Countdown
    var countdownTimer: Float = 0f
    var countdownDisplay: String by mutableStateOf("3")

    // Game Over sequence timing
    var gameOverTimer: Float = 0f
    var isGameOverSequenceFinished: Boolean = false
    private var freezeTimeRemaining: Float = 0f

    // Tutorial
    var isTutorialActive: Boolean by mutableStateOf(false)
    var tutorialFlipCount: Int = 0
    var onTutorialCompleted: (() -> Unit)? = null
    var onNewHighScoreAchieved: (() -> Unit)? = null

    // Background corridor elements
    private var gridOffset: Float = 0f
    private var panelOffset: Float = 0f
    private val bgStreaks = ArrayList<BgStreak>(12)

    data class BgStreak(
        var x: Float,
        var y: Float,
        var length: Float,
        var speedMultiplier: Float,
        var alpha: Float
    )

    init {
        player.onLanded = { surfaceY, isFloor ->
            onPlayerLanded(surfaceY, isFloor)
        }
        initBackgroundStreaks()
    }

    private fun initBackgroundStreaks() {
        bgStreaks.clear()
        for (i in 0 until 10) {
            bgStreaks.add(
                BgStreak(
                    x = Random.nextFloat() * 1080f,
                    y = Random.nextFloat() * 1000f + 300f,
                    length = Random.nextFloat() * 120f + 60f,
                    speedMultiplier = Random.nextFloat() * 0.8f + 1.2f,
                    alpha = Random.nextFloat() * 0.25f + 0.1f
                )
            )
        }
    }

    fun setDimensions(w: Float, h: Float) {
        screenWidth = w
        screenHeight = h
        // Responsive corridor: leaves 12% at top and bottom for HUD & aesthetic margin
        val margin = (h * 0.12f).coerceIn(90f, 160f)
        corridorTop = margin
        corridorBottom = h - margin

        val startX = (w * 0.22f).coerceIn(80f, 160f)
        if (state == PlayState.READY) {
            player.reset(startX = startX, floorY = corridorBottom)
        }
    }

    fun prepareGame(bestScore: Int, tutorialCompleted: Boolean) {
        state = PlayState.READY
        stats = GameStats(bestScore = bestScore)
        obstacles.clear()
        floatingNotices.clear()
        particleSystem.clear()
        milestonesPassed.clear()
        speedMilestoneText = null
        speedMilestoneTimer = 0f
        screenPulse = 0f
        cameraShakeIntensity = 0f
        gameOverTimer = 0f
        isGameOverSequenceFinished = false
        freezeTimeRemaining = 0f
        distanceAccumulator = 0f
        currentSpeed = baseSpeed
        isTutorialActive = !tutorialCompleted
        tutorialFlipCount = 0

        val startX = (screenWidth * 0.22f).coerceIn(80f, 160f)
        player.reset(startX = startX, floorY = corridorBottom)
    }

    fun startCountdown() {
        state = PlayState.COUNTDOWN
        countdownTimer = 2.0f
        countdownDisplay = "3"
        soundManager.play(SoundManager.SoundType.CLICK)
    }

    fun handleTap() {
        when (state) {
            PlayState.READY -> {
                startCountdown()
            }
            PlayState.COUNTDOWN -> {
                // If user taps during countdown, immediately start playing
                beginPlaying()
            }
            PlayState.PLAYING -> {
                performGravityFlip()
            }
            PlayState.PAUSED -> {
                // Handled by UI pause dialog
            }
            PlayState.GAME_OVER -> {
                // Handled by UI result screen
            }
        }
    }

    private fun beginPlaying() {
        state = PlayState.PLAYING
        soundManager.play(SoundManager.SoundType.SNAP)
        hapticManager.play(HapticManager.HapticType.GRAVITY_FLIP)
        screenPulse = 0.35f
    }

    private fun performGravityFlip() {
        player.flipGravity()
        soundManager.play(SoundManager.SoundType.SNAP)
        hapticManager.play(HapticManager.HapticType.GRAVITY_FLIP)
        screenPulse = 0.28f

        // Flip spark burst
        particleSystem.spawnSparks(
            x = player.x,
            y = player.y,
            count = 6,
            color = Color(0xFF00F0FF),
            speed = 140f
        )

        stats = stats.copy(flips = stats.flips + 1)

        // Tutorial tracking
        if (isTutorialActive) {
            tutorialFlipCount++
            if (tutorialFlipCount >= 3) {
                isTutorialActive = false
                onTutorialCompleted?.invoke()
            }
        }
    }

    private fun onPlayerLanded(surfaceY: Float, isFloor: Boolean) {
        soundManager.play(SoundManager.SoundType.LAND, volume = 0.85f)
        hapticManager.play(HapticManager.HapticType.SURFACE_LANDING)

        // Tiny camera impulse
        cameraShakeIntensity = 3.5f

        // Landing sparks
        particleSystem.spawnSparks(
            x = player.x,
            y = surfaceY,
            count = 4,
            color = Color(0xFF00E5FF),
            speed = 90f
        )
    }

    fun togglePause(pause: Boolean) {
        if (state == PlayState.PLAYING && pause) {
            state = PlayState.PAUSED
        } else if (state == PlayState.PAUSED && !pause) {
            state = PlayState.PLAYING
        }
    }

    fun update(
        dt: Float,
        particlesEnabled: Boolean = true,
        reducedEffects: Boolean = false
    ) {
        particleSystem.isEnabled = particlesEnabled

        // Always update ambient particles and background motion
        updateBackground(dt)

        if (state == PlayState.COUNTDOWN) {
            updateCountdown(dt)
            return
        }

        if (state == PlayState.PAUSED) {
            return
        }

        if (state == PlayState.GAME_OVER) {
            updateGameOver(dt)
            particleSystem.update(dt)
            return
        }

        if (state != PlayState.PLAYING) {
            return
        }

        // Brief freeze-frame effect right on impact
        if (freezeTimeRemaining > 0f) {
            freezeTimeRemaining -= dt
            return
        }

        // 1. Update Speed Progression
        updateSpeedProgression(dt)

        // 2. Update Player
        player.update(
            dt = dt,
            floorY = corridorBottom,
            ceilingY = corridorTop,
            reducedEffects = reducedEffects
        )

        // 3. Update Camera Shake & Screen Pulse
        if (cameraShakeIntensity > 0f) {
            cameraShakeX = (Random.nextFloat() - 0.5f) * cameraShakeIntensity
            cameraShakeY = (Random.nextFloat() - 0.5f) * cameraShakeIntensity
            cameraShakeIntensity = (cameraShakeIntensity - dt * 20f).coerceAtLeast(0f)
        } else {
            cameraShakeX = 0f
            cameraShakeY = 0f
        }

        if (screenPulse > 0f) {
            screenPulse = (screenPulse - dt * 2.2f).coerceAtLeast(0f)
        }

        if (speedMilestoneTimer > 0f) {
            speedMilestoneTimer -= dt
            if (speedMilestoneTimer <= 0f) {
                speedMilestoneText = null
            }
        }

        // 4. Update Distance & Running Score
        val distanceGained = currentSpeed * dt
        stats = stats.copy(distance = stats.distance + distanceGained)
        distanceAccumulator += distanceGained
        if (distanceAccumulator >= 320f) { // Periodic survival score
            distanceAccumulator -= 320f
            addScore(1)
        }

        // 5. Spawn & Move Obstacles
        updateObstacles(dt)

        // 6. Check Collisions & Close Calls
        evaluateCollisionsAndCloseCalls()

        // 7. Update Particles
        particleSystem.update(dt)
        if (particlesEnabled && !reducedEffects) {
            particleSystem.spawnAmbient(
                width = screenWidth,
                height = screenHeight,
                count = 14,
                color = Color(0xFF00F0FF)
            )
        }

        // 8. Update Floating Notices
        updateFloatingNotices(dt)
    }

    private fun updateCountdown(dt: Float) {
        countdownTimer -= dt
        val prevDisplay = countdownDisplay
        countdownDisplay = when {
            countdownTimer > 1.4f -> "3"
            countdownTimer > 0.8f -> "2"
            countdownTimer > 0.2f -> "1"
            else -> "SNAP!"
        }
        if (prevDisplay != countdownDisplay) {
            soundManager.play(SoundManager.SoundType.CLICK, pitch = if (countdownDisplay == "SNAP!") 1.3f else 1.0f)
            hapticManager.play(HapticManager.HapticType.BUTTON_TAP)
        }
        if (countdownTimer <= 0f) {
            beginPlaying()
        }
    }

    private fun updateSpeedProgression(dt: Float) {
        // Base speed scale: 380 at score 0 up to 620 at score 100+
        val targetSpeed = when {
            stats.score <= 20 -> 380f + (stats.score / 20f) * 40f
            stats.score <= 50 -> 420f + ((stats.score - 20) / 30f) * 60f
            stats.score <= 100 -> 480f + ((stats.score - 50) / 50f) * 80f
            else -> min(620f, 560f + ((stats.score - 100) / 100f) * 60f)
        }
        currentSpeed += (targetSpeed - currentSpeed) * (2.5f * dt)

        // Check milestones: 25, 50, 75, 100, 150, 200
        val milestones = listOf(25, 50, 75, 100, 150, 200)
        for (m in milestones) {
            if (stats.score >= m && !milestonesPassed.contains(m)) {
                milestonesPassed.add(m)
                triggerSpeedMilestone()
                break
            }
        }
    }

    private fun triggerSpeedMilestone() {
        speedMilestoneText = "SPEED UP!"
        speedMilestoneTimer = 1.2f
        soundManager.play(SoundManager.SoundType.SPEED_UP)
        hapticManager.play(HapticManager.HapticType.CLOSE_CALL)
        cameraShakeIntensity = 6f
        screenPulse = 0.45f
    }

    private fun updateObstacles(dt: Float) {
        // Move obstacles
        val it = obstacles.iterator()
        while (it.hasNext()) {
            val obs = it.next()
            obs.update(dt, currentSpeed)

            // Check if cleared
            if (!obs.cleared && obs.x + obs.width < player.x) {
                obs.cleared = true
                addScore(1)
                soundManager.play(SoundManager.SoundType.SCORE, volume = 0.7f)
                stats = stats.copy(obstaclesCleared = stats.obstaclesCleared + 1)
            }

            // Remove off-screen
            if (obs.x + obs.width < -100f) {
                it.remove()
            }
        }

        // Spawn new obstacles
        val lastObstacleX = obstacles.maxOfOrNull { it.x + it.width } ?: (screenWidth * 0.5f)
        if (lastObstacleX < screenWidth + 200f) {
            spawnNextObstacle(lastObstacleX)
        }
    }

    private fun spawnNextObstacle(lastX: Float) {
        val corridorHeight = corridorBottom - corridorTop

        // Gap spacing strictly scales with speed to guarantee fair reaction time
        // Reaction window: at least 0.65s to 0.85s
        val minReactionTime = when {
            stats.score <= 20 -> 0.95f
            stats.score <= 50 -> 0.80f
            stats.score <= 100 -> 0.70f
            else -> 0.62f
        }
        val baseGap = currentSpeed * minReactionTime
        val randomExtra = Random.nextFloat() * 160f + 60f
        val spawnX = max(lastX + baseGap + randomExtra, screenWidth + 80f)

        // Decide pattern based on score
        val typeRoll = Random.nextFloat()
        val sideRoll = Random.nextFloat()

        val side = when {
            stats.score < 15 -> if (sideRoll < 0.5f) ObstacleSide.FLOOR else ObstacleSide.CEILING
            stats.score < 50 -> {
                // Alternating sequence
                val lastSide = obstacles.lastOrNull()?.side
                if (lastSide == ObstacleSide.FLOOR) ObstacleSide.CEILING else ObstacleSide.FLOOR
            }
            else -> {
                // Higher score includes DOUBLE barriers with center safe gap
                if (sideRoll < 0.28f) ObstacleSide.BOTH
                else if (sideRoll < 0.64f) ObstacleSide.FLOOR
                else ObstacleSide.CEILING
            }
        }

        val width = when {
            stats.score < 30 -> 60f
            stats.score < 70 -> 72f
            else -> 80f
        }

        if (side == ObstacleSide.BOTH) {
            // DOUBLE BARRIER: guaranteed wide center safe corridor
            val centerGapSize = corridorHeight * 0.42f // ample clearance for player cube (38px)
            val topH = (corridorHeight - centerGapSize) * 0.5f
            val bottomH = (corridorHeight - centerGapSize) * 0.5f

            obstacles.add(
                Obstacle(
                    id = nextObstacleId++,
                    x = spawnX,
                    width = width,
                    height = 0f,
                    side = ObstacleSide.BOTH,
                    type = ObstacleType.DOUBLE_BARRIER,
                    topObstacleHeight = topH,
                    bottomObstacleHeight = bottomH,
                    gapSize = centerGapSize
                )
            )
        } else {
            val obsType = when {
                stats.score > 40 && typeRoll < 0.22f -> ObstacleType.MOVING_BARRIER
                stats.score > 25 && typeRoll < 0.45f -> ObstacleType.ENERGY_BEAM
                stats.score > 55 && typeRoll < 0.68f -> ObstacleType.TALL_PILLAR
                else -> ObstacleType.BLOCK
            }

            val height = when (obsType) {
                ObstacleType.TALL_PILLAR -> corridorHeight * 0.56f
                ObstacleType.ENERGY_BEAM -> 28f
                ObstacleType.MOVING_BARRIER -> corridorHeight * 0.36f
                ObstacleType.BLOCK -> corridorHeight * 0.32f
                ObstacleType.DOUBLE_BARRIER -> 0f
            }

            obstacles.add(
                Obstacle(
                    id = nextObstacleId++,
                    x = spawnX,
                    width = width,
                    height = height,
                    side = side,
                    type = obsType
                )
            )
        }
    }

    private fun evaluateCollisionsAndCloseCalls() {
        val playerHitbox = player.getHitbox()

        for (obs in obstacles) {
            val hitboxes = obs.getHitboxes(corridorBottom, corridorTop)
            for (box in hitboxes) {
                if (playerHitbox.overlaps(box)) {
                    triggerGameOver(box.center)
                    return
                }
            }

            // Close call check
            if (!obs.closeCallChecked) {
                // If player is horizontally passing the obstacle right now
                if (playerHitbox.right >= obs.x && playerHitbox.left <= obs.x + obs.width) {
                    val closeZones = obs.getCloseCallZone(corridorBottom, corridorTop, margin = 28f)
                    val inCloseZone = closeZones.any { it.overlaps(playerHitbox) }

                    if (inCloseZone) {
                        obs.closeCallChecked = true
                        triggerCloseCall()
                    }
                }
            }
        }
    }

    private fun triggerCloseCall() {
        val combo = stats.combo
        val bonus = 1 * combo
        addScore(bonus)

        stats = stats.copy(
            closeCalls = stats.closeCalls + 1,
            combo = combo + 1,
            maxCombo = max(stats.maxCombo, combo + 1)
        )

        soundManager.play(SoundManager.SoundType.CLOSE_CALL)
        hapticManager.play(HapticManager.HapticType.CLOSE_CALL)

        // Floating notice: "CLOSE!" or "x2 CLOSE!"
        val noticeText = if (combo > 1) "x$combo CLOSE!" else "CLOSE!"
        floatingNotices.add(
            FloatingNotice(
                id = nextNoticeId++,
                text = noticeText,
                x = player.x + 30f,
                y = player.y - 30f,
                isCombo = combo > 1
            )
        )

        // Sparks
        particleSystem.spawnCloseCallSparks(
            x = player.x,
            y = player.y,
            color = Color(0xFFFBBF24)
        )
    }

    private fun triggerGameOver(impactPoint: Offset) {
        state = PlayState.GAME_OVER
        gameOverTimer = 0f
        isGameOverSequenceFinished = false
        freezeTimeRemaining = 0.08f // brief freeze frame impact

        soundManager.play(SoundManager.SoundType.HIT)
        hapticManager.play(HapticManager.HapticType.COLLISION)

        cameraShakeIntensity = 18f
        screenPulse = 0.65f

        // Cube geometric explosion
        particleSystem.spawnExplosion(
            x = player.x,
            y = player.y,
            primaryColor = Color(0xFF00F0FF),
            secondaryColor = Color(0xFFFF0055)
        )
    }

    private fun updateGameOver(dt: Float) {
        gameOverTimer += dt

        // Camera shake decay
        if (cameraShakeIntensity > 0f) {
            cameraShakeX = (Random.nextFloat() - 0.5f) * cameraShakeIntensity
            cameraShakeY = (Random.nextFloat() - 0.5f) * cameraShakeIntensity
            cameraShakeIntensity = (cameraShakeIntensity - dt * 25f).coerceAtLeast(0f)
        }

        if (screenPulse > 0f) {
            screenPulse = (screenPulse - dt * 1.5f).coerceAtLeast(0f)
        }

        // After ~0.85 seconds, transition to result screen
        if (gameOverTimer >= 0.85f && !isGameOverSequenceFinished) {
            isGameOverSequenceFinished = true
        }
    }

    private fun updateFloatingNotices(dt: Float) {
        val it = floatingNotices.iterator()
        while (it.hasNext()) {
            val notice = it.next()
            notice.offsetY -= dt * 70f
            notice.alpha -= dt * 1.4f
            if (notice.alpha <= 0f) {
                it.remove()
            }
        }
    }

    private fun addScore(points: Int) {
        val newScore = stats.score + points
        val isNew = newScore > stats.bestScore
        if (isNew && !stats.isNewBest) {
            onNewHighScoreAchieved?.invoke()
        }
        stats = stats.copy(
            score = newScore,
            isNewBest = isNew || stats.isNewBest
        )
    }

    private fun updateBackground(dt: Float) {
        val speed = if (state == PlayState.PLAYING) currentSpeed else 160f
        gridOffset = (gridOffset + speed * dt * 0.6f) % 60f
        panelOffset = (panelOffset + speed * dt * 0.25f) % 240f

        for (streak in bgStreaks) {
            streak.x -= speed * streak.speedMultiplier * dt
            if (streak.x < -streak.length) {
                streak.x = screenWidth + Random.nextFloat() * 100f
                streak.y = corridorTop + Random.nextFloat() * (corridorBottom - corridorTop)
            }
        }
    }

    fun draw(
        drawScope: DrawScope,
        palette: com.example.gravitysnap.ui.theme.GravitySnapPalette
    ) {
        val hasShake = cameraShakeX != 0f || cameraShakeY != 0f
        if (hasShake) {
            drawScope.drawContext.transform.translate(cameraShakeX, cameraShakeY)
        }

        try {
            // 1. Draw Corridor Walls & Background Perspective Grid
            drawCorridorBackground(drawScope, palette)

            // 2. Draw Obstacles
            for (obs in obstacles) {
                obs.draw(
                    drawScope = drawScope,
                    floorY = corridorBottom,
                    ceilingY = corridorTop,
                    dangerColor = palette.hazardRed,
                    dangerAccent = palette.hazardOrange,
                    isDark = palette.isDark
                )
            }

            // 3. Draw Player (hidden during game over explosion)
            if (state != PlayState.GAME_OVER) {
                player.draw(
                    drawScope = drawScope,
                    primaryCyan = palette.primaryCyan,
                    primaryAqua = palette.primaryAqua,
                    coreWhite = Color.White,
                    accentViolet = palette.accentViolet
                )
            }

            // 4. Draw Particles
            particleSystem.draw(drawScope)

            // 5. Draw Floor & Ceiling Glowing Strips & Panels
            drawCorridorSurfaces(drawScope, palette)

            // 6. Draw Screen Pulse Flash
            if (screenPulse > 0f) {
                drawScope.drawRect(
                    color = palette.primaryCyan.copy(alpha = (screenPulse * 0.22f).coerceIn(0f, 0.4f)),
                    size = androidx.compose.ui.geometry.Size(screenWidth, screenHeight)
                )
            }
        } finally {
            if (hasShake) {
                drawScope.drawContext.transform.translate(-cameraShakeX, -cameraShakeY)
            }
        }
    }

    private fun drawCorridorBackground(
        drawScope: DrawScope,
        palette: com.example.gravitysnap.ui.theme.GravitySnapPalette
    ) {
        val corridorH = corridorBottom - corridorTop

        // Subtle gradient backdrop
        drawScope.drawRect(
            color = palette.background,
            topLeft = Offset(0f, corridorTop),
            size = androidx.compose.ui.geometry.Size(screenWidth, corridorH)
        )

        // Light background perspective streaks
        for (streak in bgStreaks) {
            drawScope.drawLine(
                color = palette.primaryCyan.copy(alpha = streak.alpha * 0.35f),
                start = Offset(streak.x, streak.y),
                end = Offset(streak.x + streak.length, streak.y),
                strokeWidth = 1.8f
            )
        }

        // Vertical moving grid lines
        val lineSpacing = 70f
        var curX = -gridOffset
        while (curX < screenWidth + lineSpacing) {
            if (curX >= 0) {
                drawScope.drawLine(
                    color = palette.gridLine,
                    start = Offset(curX, corridorTop),
                    end = Offset(curX, corridorBottom),
                    strokeWidth = 1.2f
                )
            }
            curX += lineSpacing
        }

        // Subtle horizontal center corridor horizon line
        val midY = (corridorTop + corridorBottom) / 2f
        drawScope.drawLine(
            color = palette.gridLine.copy(alpha = 0.08f),
            start = Offset(0f, midY),
            end = Offset(screenWidth, midY),
            strokeWidth = 1f
        )
    }

    private fun drawCorridorSurfaces(
        drawScope: DrawScope,
        palette: com.example.gravitysnap.ui.theme.GravitySnapPalette
    ) {
        // Subtle gravity direction glow boost:
        // Ceiling glows brighter if pulling UP, Floor glows brighter if pulling DOWN
        val isPullingUp = player.gravityDirection == GravityDirection.UP
        val ceilingGlowAlpha = if (isPullingUp) 0.85f else 0.30f
        val floorGlowAlpha = if (!isPullingUp) 0.85f else 0.30f

        // Ceiling Surface
        drawScope.drawRect(
            color = palette.corridorSurface,
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(screenWidth, corridorTop)
        )
        // Ceiling Neon Edge Line
        drawScope.drawLine(
            color = palette.primaryCyan.copy(alpha = ceilingGlowAlpha),
            start = Offset(0f, corridorTop),
            end = Offset(screenWidth, corridorTop),
            strokeWidth = if (isPullingUp) 4f else 2.5f
        )
        // Ceiling Soft Glow Bloom
        drawScope.drawLine(
            color = palette.primaryAqua.copy(alpha = ceilingGlowAlpha * 0.4f),
            start = Offset(0f, corridorTop - 3f),
            end = Offset(screenWidth, corridorTop - 3f),
            strokeWidth = 8f
        )

        // Floor Surface
        drawScope.drawRect(
            color = palette.corridorSurface,
            topLeft = Offset(0f, corridorBottom),
            size = androidx.compose.ui.geometry.Size(screenWidth, screenHeight - corridorBottom)
        )
        // Floor Neon Edge Line
        drawScope.drawLine(
            color = palette.primaryCyan.copy(alpha = floorGlowAlpha),
            start = Offset(0f, corridorBottom),
            end = Offset(screenWidth, corridorBottom),
            strokeWidth = if (!isPullingUp) 4f else 2.5f
        )
        // Floor Soft Glow Bloom
        drawScope.drawLine(
            color = palette.primaryAqua.copy(alpha = floorGlowAlpha * 0.4f),
            start = Offset(0f, corridorBottom + 3f),
            end = Offset(screenWidth, corridorBottom + 3f),
            strokeWidth = 8f
        )

        // Repeating geometric hash markings on floor and ceiling edges
        val hashSpacing = 90f
        var hashX = -panelOffset
        while (hashX < screenWidth + hashSpacing) {
            if (hashX >= 0) {
                // Floor chevron/hash
                drawScope.drawLine(
                    color = palette.primaryCyan.copy(alpha = floorGlowAlpha * 0.6f),
                    start = Offset(hashX, corridorBottom + 2f),
                    end = Offset(hashX + 16f, corridorBottom + 12f),
                    strokeWidth = 2f
                )
                // Ceiling chevron/hash
                drawScope.drawLine(
                    color = palette.primaryCyan.copy(alpha = ceilingGlowAlpha * 0.6f),
                    start = Offset(hashX, corridorTop - 2f),
                    end = Offset(hashX + 16f, corridorTop - 12f),
                    strokeWidth = 2f
                )
            }
            hashX += hashSpacing
        }
    }
}
