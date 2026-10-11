package com.example.gravitysnap.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class TrailNode(
    val x: Float,
    val y: Float,
    var alpha: Float = 0.6f,
    val rotation: Float,
    val size: Float
)

class Player(
    val baseSize: Float = 38f
) {
    var x: Float = 100f
    var y: Float = 300f
    var verticalVelocity: Float = 0f
    var gravityDirection: GravityDirection by mutableStateOf(GravityDirection.DOWN)
    var state: PlayerState = PlayerState.FLOOR_RUNNING

    // Visual animation states
    var rotation: Float = 0f
    var targetRotation: Float = 0f
    var stretchX: Float = 1.0f
    var stretchY: Float = 1.0f
    var glowIntensity: Float = 1.0f
    var breathingPhase: Float = 0f

    // Motion trail points
    val trail = ArrayList<TrailNode>(16)
    private var trailTimer: Float = 0f

    // Callback triggers for engine
    var onLanded: ((surfaceY: Float, isFloor: Boolean) -> Unit)? = null

    fun reset(startX: Float, floorY: Float) {
        x = startX
        y = floorY - baseSize / 2f
        verticalVelocity = 0f
        gravityDirection = GravityDirection.DOWN
        state = PlayerState.FLOOR_RUNNING
        rotation = 0f
        targetRotation = 0f
        stretchX = 1.0f
        stretchY = 1.0f
        glowIntensity = 1.0f
        breathingPhase = 0f
        trail.clear()
        trailTimer = 0f
    }

    fun flipGravity() {
        gravityDirection = if (gravityDirection == GravityDirection.DOWN) {
            GravityDirection.UP
        } else {
            GravityDirection.DOWN
        }

        state = PlayerState.TRANSITIONING

        // Fast stretch during flip transition
        stretchY = 1.45f
        stretchX = 0.70f
        glowIntensity = 2.4f

        // Initial vertical impulse
        val impulseSpeed = 1100f
        if (gravityDirection == GravityDirection.UP) {
            verticalVelocity = -impulseSpeed
            targetRotation += 90f
        } else {
            verticalVelocity = impulseSpeed
            targetRotation -= 90f
        }
    }

    fun update(
        dt: Float,
        floorY: Float,
        ceilingY: Float,
        reducedEffects: Boolean = false
    ) {
        breathingPhase += dt * 6f

        // Decay extra glow back to 1.0
        if (glowIntensity > 1.0f) {
            glowIntensity = (glowIntensity - dt * 4.0f).coerceAtLeast(1.0f)
        }

        // Return stretch smoothly back to 1.0
        val springSpeed = 16f * dt
        stretchX += (1.0f - stretchX) * springSpeed
        stretchY += (1.0f - stretchY) * springSpeed

        // Smooth rotation interpolation
        val rotDiff = targetRotation - rotation
        rotation += rotDiff * (22f * dt).coerceIn(0f, 1f)

        // Physics motion
        val gravityAccel = 5800f // snappy arcade vertical acceleration
        if (gravityDirection == GravityDirection.UP) {
            verticalVelocity -= gravityAccel * dt
        } else {
            verticalVelocity += gravityAccel * dt
        }

        y += verticalVelocity * dt

        // Boundaries: Floor & Ceiling
        val halfSize = baseSize / 2f
        val topSurface = ceilingY + halfSize
        val bottomSurface = floorY - halfSize

        if (gravityDirection == GravityDirection.UP && y <= topSurface) {
            // Landed on ceiling
            y = topSurface
            verticalVelocity = 0f
            if (state == PlayerState.TRANSITIONING) {
                state = PlayerState.CEILING_RUNNING
                // Landing impact squash
                stretchY = 0.72f
                stretchX = 1.30f
                onLanded?.invoke(ceilingY, false)
            }
        } else if (gravityDirection == GravityDirection.DOWN && y >= bottomSurface) {
            // Landed on floor
            y = bottomSurface
            verticalVelocity = 0f
            if (state == PlayerState.TRANSITIONING) {
                state = PlayerState.FLOOR_RUNNING
                // Landing impact squash
                stretchY = 0.72f
                stretchX = 1.30f
                onLanded?.invoke(floorY, true)
            }
        }

        // Trail recording
        if (!reducedEffects) {
            trailTimer += dt
            if (trailTimer >= 0.022f) { // ~45 times a second
                trailTimer = 0f
                trail.add(
                    0,
                    TrailNode(
                        x = x,
                        y = y,
                        alpha = if (state == PlayerState.TRANSITIONING) 0.65f else 0.35f,
                        rotation = rotation,
                        size = baseSize * 0.9f
                    )
                )
                if (trail.size > 8) {
                    trail.removeAt(trail.size - 1)
                }
            }

            // Fade existing trail
            val decay = dt * 3.5f
            val it = trail.iterator()
            while (it.hasNext()) {
                val node = it.next()
                node.alpha -= decay
                if (node.alpha <= 0f) {
                    it.remove()
                }
            }
        } else {
            trail.clear()
        }
    }

    /**
     * Hitbox slightly smaller than visual bounds for fair collision.
     */
    fun getHitbox(): Rect {
        val radius = (baseSize / 2f) * 0.78f
        return Rect(
            left = x - radius,
            top = y - radius,
            right = x + radius,
            bottom = y + radius
        )
    }

    fun draw(
        drawScope: DrawScope,
        primaryCyan: Color,
        primaryAqua: Color,
        coreWhite: Color,
        accentViolet: Color
    ) {
        val half = baseSize / 2f

        // 1. Draw glowing motion trail
        for (i in trail.indices.reversed()) {
            val node = trail[i]
            val nodeHalf = node.size / 2f
            drawScope.rotate(degrees = node.rotation, pivot = Offset(node.x, node.y)) {
                drawRoundRect(
                    color = primaryCyan.copy(alpha = (node.alpha * 0.45f).coerceIn(0f, 1f)),
                    topLeft = Offset(node.x - nodeHalf, node.y - nodeHalf),
                    size = Size(node.size, node.size),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 2f)
                )
            }
        }

        // 2. Draw Player Cube with Stretch & Rotation
        drawScope.rotate(degrees = rotation, pivot = Offset(x, y)) {
            drawScope.scale(scaleX = stretchX, scaleY = stretchY, pivot = Offset(x, y)) {
                val breathing = 1.0f + 0.06f * Math.sin(breathingPhase.toDouble()).toFloat()
                val glowAlpha = (0.28f * glowIntensity).coerceIn(0f, 0.75f)

                // Outer Soft Neon Bloom
                drawRoundRect(
                    color = primaryCyan.copy(alpha = glowAlpha),
                    topLeft = Offset(x - half * 1.35f * breathing, y - half * 1.35f * breathing),
                    size = Size(baseSize * 1.35f * breathing, baseSize * 1.35f * breathing),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                // Outer Neon Edge Glow
                drawRoundRect(
                    color = primaryAqua.copy(alpha = (0.45f * glowIntensity).coerceIn(0f, 0.9f)),
                    topLeft = Offset(x - half * 1.15f, y - half * 1.15f),
                    size = Size(baseSize * 1.15f, baseSize * 1.15f),
                    cornerRadius = CornerRadius(9f, 9f)
                )

                // Dark Cybernetic Cube Base Body
                drawRoundRect(
                    color = Color(0xFF031628),
                    topLeft = Offset(x - half, y - half),
                    size = Size(baseSize, baseSize),
                    cornerRadius = CornerRadius(7f, 7f)
                )

                // Vibrant Neon Outline
                drawRoundRect(
                    color = primaryCyan,
                    topLeft = Offset(x - half, y - half),
                    size = Size(baseSize, baseSize),
                    cornerRadius = CornerRadius(7f, 7f),
                    style = Stroke(width = 2.8f)
                )

                // Inner Radiant Core
                val coreSize = baseSize * 0.44f
                val coreHalf = coreSize / 2f
                drawRoundRect(
                    color = coreWhite,
                    topLeft = Offset(x - coreHalf, y - coreHalf),
                    size = Size(coreSize, coreSize),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Glossy Light Reflection (diagonal bevel accent)
                drawLine(
                    color = coreWhite.copy(alpha = 0.8f),
                    start = Offset(x - half + 5f, y - half + 4f),
                    end = Offset(x + half - 5f, y - half + 4f),
                    strokeWidth = 1.8f
                )
            }
        }
    }
}
