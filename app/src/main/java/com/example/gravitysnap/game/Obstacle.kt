package com.example.gravitysnap.game

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

enum class ObstacleSide {
    FLOOR,
    CEILING,
    BOTH
}

enum class ObstacleType {
    BLOCK,
    TALL_PILLAR,
    DOUBLE_BARRIER,
    ENERGY_BEAM,
    MOVING_BARRIER
}

class Obstacle(
    val id: Long,
    var x: Float,
    val width: Float,
    var height: Float,
    val side: ObstacleSide,
    val type: ObstacleType,
    // For DOUBLE_BARRIER:
    val topObstacleHeight: Float = 0f,
    val bottomObstacleHeight: Float = 0f,
    val gapCenterY: Float = 0f,
    val gapSize: Float = 0f
) {
    var cleared: Boolean = false
    var closeCallChecked: Boolean = false
    var oscillationPhase: Float = 0f

    // For moving barriers
    var currentYOffset: Float = 0f

    fun update(dt: Float, speed: Float) {
        x -= speed * dt

        if (type == ObstacleType.MOVING_BARRIER) {
            oscillationPhase += dt * 3.5f
            currentYOffset = Math.sin(oscillationPhase.toDouble()).toFloat() * 22f
        }
    }

    /**
     * Returns the bounding boxes for collision detection.
     * Note: hitboxes are intentionally inset slightly (fair collision)
     * so players aren't punished for pixel-perfect near misses.
     */
    fun getHitboxes(floorY: Float, ceilingY: Float): List<Rect> {
        val insetX = width * 0.12f
        val actualLeft = x + insetX
        val actualRight = x + width - insetX

        return when (side) {
            ObstacleSide.FLOOR -> {
                val effectiveH = height
                val top = floorY - effectiveH + (if (type == ObstacleType.MOVING_BARRIER) currentYOffset else 0f)
                val bottom = floorY
                listOf(
                    Rect(
                        left = actualLeft,
                        top = top + effectiveH * 0.08f,
                        right = actualRight,
                        bottom = bottom
                    )
                )
            }
            ObstacleSide.CEILING -> {
                val effectiveH = height
                val top = ceilingY
                val bottom = ceilingY + effectiveH + (if (type == ObstacleType.MOVING_BARRIER) currentYOffset else 0f)
                listOf(
                    Rect(
                        left = actualLeft,
                        top = top,
                        right = actualRight,
                        bottom = bottom - effectiveH * 0.08f
                    )
                )
            }
            ObstacleSide.BOTH -> {
                val list = mutableListOf<Rect>()
                if (topObstacleHeight > 0f) {
                    list.add(
                        Rect(
                            left = actualLeft,
                            top = ceilingY,
                            right = actualRight,
                            bottom = ceilingY + topObstacleHeight * 0.92f
                        )
                    )
                }
                if (bottomObstacleHeight > 0f) {
                    list.add(
                        Rect(
                            left = actualLeft,
                            top = floorY - bottomObstacleHeight * 0.92f,
                            right = actualRight,
                            bottom = floorY
                        )
                    )
                }
                list
            }
        }
    }

    /**
     * Zone around obstacle for close call detection.
     */
    fun getCloseCallZone(floorY: Float, ceilingY: Float, margin: Float = 26f): List<Rect> {
        return getHitboxes(floorY, ceilingY).map { box ->
            Rect(
                left = box.left - margin,
                top = box.top - margin,
                right = box.right + margin,
                bottom = box.bottom + margin
            )
        }
    }

    fun draw(
        drawScope: DrawScope,
        floorY: Float,
        ceilingY: Float,
        dangerColor: Color,
        dangerAccent: Color,
        isDark: Boolean
    ) {
        when (side) {
            ObstacleSide.FLOOR -> {
                val top = floorY - height + (if (type == ObstacleType.MOVING_BARRIER) currentYOffset else 0f)
                drawObstacleBox(
                    drawScope = drawScope,
                    rect = Rect(x, top, x + width, floorY),
                    isFloor = true,
                    dangerColor = dangerColor,
                    dangerAccent = dangerAccent,
                    type = type,
                    isDark = isDark
                )
            }
            ObstacleSide.CEILING -> {
                val bottom = ceilingY + height + (if (type == ObstacleType.MOVING_BARRIER) currentYOffset else 0f)
                drawObstacleBox(
                    drawScope = drawScope,
                    rect = Rect(x, ceilingY, x + width, bottom),
                    isFloor = false,
                    dangerColor = dangerColor,
                    dangerAccent = dangerAccent,
                    type = type,
                    isDark = isDark
                )
            }
            ObstacleSide.BOTH -> {
                if (topObstacleHeight > 0f) {
                    drawObstacleBox(
                        drawScope = drawScope,
                        rect = Rect(x, ceilingY, x + width, ceilingY + topObstacleHeight),
                        isFloor = false,
                        dangerColor = dangerColor,
                        dangerAccent = dangerAccent,
                        type = ObstacleType.BLOCK,
                        isDark = isDark
                    )
                }
                if (bottomObstacleHeight > 0f) {
                    drawObstacleBox(
                        drawScope = drawScope,
                        rect = Rect(x, floorY - bottomObstacleHeight, x + width, floorY),
                        isFloor = true,
                        dangerColor = dangerColor,
                        dangerAccent = dangerAccent,
                        type = ObstacleType.BLOCK,
                        isDark = isDark
                    )
                }
            }
        }
    }

    private fun drawObstacleBox(
        drawScope: DrawScope,
        rect: Rect,
        isFloor: Boolean,
        dangerColor: Color,
        dangerAccent: Color,
        type: ObstacleType,
        isDark: Boolean
    ) {
        val cornerRadius = CornerRadius(6f, 6f)
        val bodyColor = if (isDark) Color(0xFF160E18) else Color(0xFF3B1D28)

        // 1. Solid background body
        drawScope.drawRoundRect(
            color = bodyColor,
            topLeft = Offset(rect.left, rect.top),
            size = Size(rect.width, rect.height),
            cornerRadius = cornerRadius
        )

        // 2. Inner hazard warning pattern / core
        val innerMargin = 5f
        if (rect.width > innerMargin * 2 && rect.height > innerMargin * 2) {
            val innerRect = Rect(
                rect.left + innerMargin,
                rect.top + innerMargin,
                rect.right - innerMargin,
                rect.bottom - innerMargin
            )
            drawScope.drawRoundRect(
                color = dangerColor.copy(alpha = 0.28f),
                topLeft = Offset(innerRect.left, innerRect.top),
                size = Size(innerRect.width, innerRect.height),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Tech hazard diagonal line or core stripe
            val midY = (innerRect.top + innerRect.bottom) / 2f
            drawScope.drawLine(
                color = dangerAccent.copy(alpha = 0.75f),
                start = Offset(innerRect.left + 4f, midY),
                end = Offset(innerRect.right - 4f, midY),
                strokeWidth = 3f
            )
        }

        // 3. Crisp neon danger border
        drawScope.drawRoundRect(
            color = dangerColor,
            topLeft = Offset(rect.left, rect.top),
            size = Size(rect.width, rect.height),
            cornerRadius = cornerRadius,
            style = Stroke(width = 2.5f)
        )

        // 4. Glowing contact edge with the floor or ceiling
        val edgeY = if (isFloor) rect.top else rect.bottom
        drawScope.drawLine(
            color = dangerAccent,
            start = Offset(rect.left + 2f, edgeY),
            end = Offset(rect.right - 2f, edgeY),
            strokeWidth = 3.5f
        )

        // If energy beam, draw intense beam aura
        if (type == ObstacleType.ENERGY_BEAM) {
            drawScope.drawLine(
                color = dangerAccent.copy(alpha = 0.9f),
                start = Offset(rect.left - 4f, (rect.top + rect.bottom) / 2f),
                end = Offset(rect.right + 4f, (rect.top + rect.bottom) / 2f),
                strokeWidth = 5f
            )
        }
    }
}
