package com.example.gravitysnap.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

enum class ParticleType {
    SPARK,
    SQUARE_SHARD,
    SHOCKWAVE_RING,
    SMOKE_PUFF,
    AMBIENT_DUST
}

class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    val color: Color,
    val maxLife: Float,
    val type: ParticleType = ParticleType.SPARK,
    var rotation: Float = 0f,
    var vRot: Float = 0f
) {
    var life: Float = maxLife

    val isAlive: Boolean
        get() = life > 0f

    val alpha: Float
        get() = (life / maxLife).coerceIn(0f, 1f)

    fun update(dt: Float) {
        life -= dt
        x += vx * dt
        y += vy * dt
        rotation += vRot * dt

        // Drag/friction
        when (type) {
            ParticleType.SQUARE_SHARD -> {
                vx *= (1f - 0.5f * dt).coerceIn(0f, 1f)
                vy *= (1f - 0.5f * dt).coerceIn(0f, 1f)
            }
            ParticleType.SHOCKWAVE_RING -> {
                size += 220f * dt
            }
            ParticleType.SPARK -> {
                vx *= (1f - 2f * dt).coerceIn(0f, 1f)
                vy *= (1f - 2f * dt).coerceIn(0f, 1f)
            }
            else -> {}
        }
    }

    fun draw(drawScope: DrawScope) {
        if (!isAlive) return
        val currentAlpha = alpha

        when (type) {
            ParticleType.SPARK -> {
                drawScope.drawCircle(
                    color = color.copy(alpha = currentAlpha),
                    radius = size * currentAlpha,
                    center = Offset(x, y)
                )
            }
            ParticleType.SQUARE_SHARD -> {
                val pSize = this@Particle.size
                val half = pSize / 2f
                drawScope.rotate(degrees = rotation, pivot = Offset(x, y)) {
                    drawRect(
                        color = color.copy(alpha = currentAlpha),
                        topLeft = Offset(x - half, y - half),
                        size = Size(pSize, pSize)
                    )
                }
            }
            ParticleType.SHOCKWAVE_RING -> {
                drawScope.drawCircle(
                    color = color.copy(alpha = currentAlpha * 0.8f),
                    radius = size,
                    center = Offset(x, y),
                    style = Stroke(width = (4f * currentAlpha).coerceAtLeast(1f))
                )
            }
            ParticleType.AMBIENT_DUST -> {
                drawScope.drawCircle(
                    color = color.copy(alpha = currentAlpha * 0.4f),
                    radius = size,
                    center = Offset(x, y)
                )
            }
            ParticleType.SMOKE_PUFF -> {
                drawScope.drawCircle(
                    color = color.copy(alpha = currentAlpha * 0.25f),
                    radius = size * (1.5f - currentAlpha * 0.5f),
                    center = Offset(x, y)
                )
            }
        }
    }
}

class ParticleSystem {
    private val particles = ArrayList<Particle>(256)
    var isEnabled: Boolean = true

    fun spawnSparks(x: Float, y: Float, count: Int, color: Color, speed: Float = 180f) {
        if (!isEnabled) return
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val spd = Random.nextFloat() * speed + 40f
            val vx = Math.cos(angle.toDouble()).toFloat() * spd
            val vy = Math.sin(angle.toDouble()).toFloat() * spd
            val life = Random.nextFloat() * 0.35f + 0.15f
            val size = Random.nextFloat() * 4f + 2.5f

            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    size = size,
                    color = color,
                    maxLife = life,
                    type = ParticleType.SPARK
                )
            )
        }
    }

    fun spawnExplosion(x: Float, y: Float, primaryColor: Color, secondaryColor: Color) {
        if (!isEnabled) return
        // Shockwave ring
        particles.add(
            Particle(
                x = x,
                y = y,
                vx = 0f,
                vy = 0f,
                size = 12f,
                color = primaryColor,
                maxLife = 0.5f,
                type = ParticleType.SHOCKWAVE_RING
            )
        )
        // Square shards (cube breaking apart)
        for (i in 0 until 24) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 320f + 80f
            val vx = Math.cos(angle.toDouble()).toFloat() * speed
            val vy = Math.sin(angle.toDouble()).toFloat() * speed
            val size = Random.nextFloat() * 10f + 5f
            val life = Random.nextFloat() * 0.6f + 0.3f
            val col = if (Random.nextBoolean()) primaryColor else secondaryColor
            val rot = Random.nextFloat() * 360f
            val vRot = (Random.nextFloat() - 0.5f) * 720f

            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    size = size,
                    color = col,
                    maxLife = life,
                    type = ParticleType.SQUARE_SHARD,
                    rotation = rot,
                    vRot = vRot
                )
            )
        }
    }

    fun spawnCloseCallSparks(x: Float, y: Float, color: Color) {
        if (!isEnabled) return
        // Bright upward/directional spark fountain
        for (i in 0 until 12) {
            val vx = (Random.nextFloat() - 0.5f) * 160f - 40f
            val vy = (Random.nextFloat() - 0.5f) * 200f
            val size = Random.nextFloat() * 4f + 2f
            val life = Random.nextFloat() * 0.4f + 0.2f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    size = size,
                    color = color,
                    maxLife = life,
                    type = ParticleType.SPARK
                )
            )
        }
    }

    fun spawnAmbient(width: Float, height: Float, count: Int, color: Color) {
        if (!isEnabled) return
        val currentAmbient = particles.count { it.type == ParticleType.AMBIENT_DUST }
        if (currentAmbient >= count) return

        for (i in 0 until (count - currentAmbient)) {
            val x = Random.nextFloat() * width
            val y = Random.nextFloat() * height
            val vx = -(Random.nextFloat() * 40f + 20f)
            val vy = (Random.nextFloat() - 0.5f) * 15f
            val size = Random.nextFloat() * 2.5f + 1f
            val life = Random.nextFloat() * 3f + 2f

            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    size = size,
                    color = color,
                    maxLife = life,
                    type = ParticleType.AMBIENT_DUST
                )
            )
        }
    }

    fun update(dt: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.update(dt)
            if (!p.isAlive) {
                iterator.remove()
            }
        }
    }

    fun draw(drawScope: DrawScope) {
        for (i in 0 until particles.size) {
            particles[i].draw(drawScope)
        }
    }

    fun clear() {
        particles.clear()
    }
}
