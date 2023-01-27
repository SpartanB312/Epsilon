package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.math.ceilToInt
import club.eridani.epsilon.client.util.math.floorToInt
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.max

object InstantDrop : Module(
    name = "InstantDrop",
    category = Category.Movement,
    description = "Increase your downwards velocity when falling"
) {
    private val height by setting("Height", 2f, 0f..3f, .1f)
    private val speed by setting("Speed", 1f, 0f..10f, .1f)

    init {
        listener<PlayerMoveEvent.Pre> { event ->
            runSafe {
                if (!mc.gameSettings.keyBindSneak.isKeyDown
                    && !mc.gameSettings.keyBindJump.isKeyDown
                    && !player.isElytraFlying
                    && !player.capabilities.isFlying
                    && !player.isOnLadder
                    && player.onGround
                    && event.y in -0.08..0.0
                    && !player.isInWater
                    && !player.isInLava
                    && (player.posY - world.getGroundLevel(player.entityBoundingBox) in 0.25..height.toDouble()
                            || player.posY - world.getGroundLevel(player.entityBoundingBox.offset(event.x, 0.0, event.z)) in 0.25..height.toDouble())) {
                    event.y -= speed
                }
            }
        }
    }

    private fun World.getGroundLevel(boundingBox: AxisAlignedBB): Double {
        var maxY = Double.MIN_VALUE
        val pos = BlockPos.PooledMutableBlockPos.retain()

        for (x in (boundingBox.minX - 0.1).floorToInt()..(boundingBox.maxX + 0.1).floorToInt()) {
            for (z in (boundingBox.minZ - 0.1).floorToInt()..(boundingBox.maxZ + 0.1).floorToInt()) {
                for (y in (boundingBox.minY - 0.5).floorToInt() downTo -1) {
                    if (y < maxY.ceilToInt() - 1) break

                    pos.setPos(x, y, z)
                    val box = this.getBlockState(pos).getCollisionBoundingBox(this, pos)
                    if (box != null) {
                        maxY = max(maxY, y + box.maxY)
                    }
                }
            }
        }

        return if (maxY == Double.MIN_VALUE) -999.0 else maxY
    }
}