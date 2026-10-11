package com.example.gravitysnap

import com.example.gravitysnap.game.GravityDirection
import com.example.gravitysnap.game.Obstacle
import com.example.gravitysnap.game.ObstacleSide
import com.example.gravitysnap.game.ObstacleType
import com.example.gravitysnap.game.Player
import com.example.gravitysnap.game.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun testPlayerGravityFlip() {
        val player = Player(baseSize = 38f)
        player.reset(startX = 120f, floorY = 1000f)

        assertEquals(GravityDirection.DOWN, player.gravityDirection)
        assertEquals(PlayerState.FLOOR_RUNNING, player.state)

        // Flip to UP
        player.flipGravity()
        assertEquals(GravityDirection.UP, player.gravityDirection)
        assertEquals(PlayerState.TRANSITIONING, player.state)
        assertTrue(player.verticalVelocity < 0f)

        // Flip back to DOWN
        player.flipGravity()
        assertEquals(GravityDirection.DOWN, player.gravityDirection)
        assertEquals(PlayerState.TRANSITIONING, player.state)
        assertTrue(player.verticalVelocity > 0f)
    }

    @Test
    fun testPlayerFloorCeilingLanding() {
        val player = Player(baseSize = 40f)
        val floorY = 1000f
        val ceilingY = 200f
        player.reset(startX = 120f, floorY = floorY)

        // Initially at floor
        assertEquals(floorY - 20f, player.y, 0.01f)

        // Flip to ceiling
        var landedCeiling = false
        player.onLanded = { _, isFloor ->
            if (!isFloor) landedCeiling = true
        }
        player.flipGravity()

        // Simulate update frames until ceiling is reached
        for (i in 0 until 50) {
            player.update(dt = 0.016f, floorY = floorY, ceilingY = ceilingY)
        }

        assertTrue("Player should have landed on ceiling", landedCeiling)
        assertEquals(PlayerState.CEILING_RUNNING, player.state)
        assertEquals(ceilingY + 20f, player.y, 0.01f)
        assertEquals(0f, player.verticalVelocity, 0.01f)
    }

    @Test
    fun testFairCollisionHitboxes() {
        val floorY = 1000f
        val ceilingY = 200f
        val obs = Obstacle(
            id = 1L,
            x = 200f,
            width = 60f,
            height = 100f,
            side = ObstacleSide.FLOOR,
            type = ObstacleType.BLOCK
        )

        val hitboxes = obs.getHitboxes(floorY, ceilingY)
        assertEquals(1, hitboxes.size)
        val box = hitboxes[0]

        // Hitbox should be slightly inset from visual x (200f .. 260f)
        assertTrue("Hitbox left must be inset rightwards", box.left > 200f)
        assertTrue("Hitbox right must be inset leftwards", box.right < 260f)
        assertEquals(floorY, box.bottom, 0.01f)
    }

    @Test
    fun testDoubleBarrierSafeGap() {
        val corridorHeight = 800f
        val centerGapSize = corridorHeight * 0.42f // ~336f
        val topH = (corridorHeight - centerGapSize) * 0.5f
        val bottomH = (corridorHeight - centerGapSize) * 0.5f

        val obs = Obstacle(
            id = 2L,
            x = 300f,
            width = 80f,
            height = 0f,
            side = ObstacleSide.BOTH,
            type = ObstacleType.DOUBLE_BARRIER,
            topObstacleHeight = topH,
            bottomObstacleHeight = bottomH,
            gapSize = centerGapSize
        )

        val ceilingY = 100f
        val floorY = ceilingY + corridorHeight // 900f
        val hitboxes = obs.getHitboxes(floorY, ceilingY)

        assertEquals(2, hitboxes.size)
        val topBox = hitboxes[0]
        val bottomBox = hitboxes[1]

        // Check vertical gap between obstacles
        val gap = bottomBox.top - topBox.bottom
        assertTrue("Safe gap must accommodate player cube (38px)", gap > 200f)
    }

    @Test
    fun testCloseCallDetectionZone() {
        val floorY = 1000f
        val ceilingY = 200f
        val obs = Obstacle(
            id = 3L,
            x = 500f,
            width = 60f,
            height = 120f,
            side = ObstacleSide.FLOOR,
            type = ObstacleType.BLOCK
        )

        val hitboxes = obs.getHitboxes(floorY, ceilingY)
        val closeZones = obs.getCloseCallZone(floorY, ceilingY, margin = 26f)

        assertEquals(hitboxes.size, closeZones.size)
        assertTrue("Close call zone must be wider than hitbox", closeZones[0].width > hitboxes[0].width)
        assertTrue("Close call zone must be taller than hitbox", closeZones[0].height > hitboxes[0].height)
    }
}
