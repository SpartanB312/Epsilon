package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.MovementUtils.applySpeedPotionEffects
import club.eridani.epsilon.client.util.MovementUtils.isCentered
import club.eridani.epsilon.client.util.math.Vec2d
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.floor
import kotlin.math.hypot

internal object AutoCenter : Module(
    name = "AutoCenter",
    description = "Moves player to the center of a block",
    category = Category.Movement
) {
    private var ticks = 0
    private var center: Vec2d? = null

    fun centerPlayer(blockPos: BlockPos) {
        center = Vec2d(blockPos.x + 0.5, blockPos.z + 0.5)
        enable()
    }

    fun centerPlayer(vec3d: Vec3d) {
        center = Vec2d(vec3d.x, vec3d.z)
        enable()
    }

    override fun onEnable() {
        if (center == null) {
            runSafe {
                center = Vec2d(floor(player.posX) + 0.5, floor(player.posZ) + 0.5)
            }
        }
    }

    override fun onDisable() {
        ticks = 0
        center = null
    }


    init {
        safeListener<PlayerMoveEvent.Pre>(-2000) {
            val center = center
            if (center == null || ticks++ >= 5) {
                disable()
                return@safeListener
            }

            if (!player.isCentered(center.x, center.y)) {
                var x = center.x - player.posX
                var z = center.y - player.posZ

                val speed = hypot(x, z)
                val baseSpeed = if (player.isSneaking) 0.05746 else 0.2873
                val maxSpeed = player.applySpeedPotionEffects(baseSpeed)

                if (speed > maxSpeed) {
                    val multiplier = maxSpeed / speed
                    x *= multiplier
                    z *= multiplier
                }

                player.motionX = 0.0
                player.motionZ = 0.0
                it.x = x
                it.z = z
            } else {
                disable()
            }
        }
    }
}