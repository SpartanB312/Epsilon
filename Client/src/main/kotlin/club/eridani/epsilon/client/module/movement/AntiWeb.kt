package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.isInWeb
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.events.AddCollisionBoxEvent
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.isFlying
import club.eridani.epsilon.client.util.math.fastFloor
import club.eridani.epsilon.client.util.world.getBlock
import net.minecraft.init.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

internal object AntiWeb : Module(
    name = "AntiWeb",
    description = "Prevents walking into web",
    category = Category.Movement
) {
    private val speedMultiplier by setting("Speed Multiplier", 0.8f, 0.1f..1.0f, 0.1f)

    init {
        safeListener<AddCollisionBoxEvent> {
            if (it.entity == player && it.block == Blocks.WEB) {
                it.collidingBoxes.add(
                    AxisAlignedBB(
                        it.pos.x.toDouble(), it.pos.y.toDouble(), it.pos.z.toDouble(),
                        it.pos.x + 1.0, it.pos.y + 1.0, it.pos.z + 1.0
                    )
                )
            }
        }

        safeListener<PlayerMoveEvent.Pre>(-2000) {
            if (!player.isFlying && player.onGround && player.motionY <= 0.0 && player.motionY >= -0.08 && !player.isInWeb && isAboveWeb()) {
                it.x = player.motionX * speedMultiplier
                it.z = player.motionZ * speedMultiplier
            }
        }
    }

    private fun SafeClientEvent.isAboveWeb(): Boolean {
        val box = player.entityBoundingBox
        val pos = BlockPos.PooledMutableBlockPos.retain()
        val y = (player.posY - 0.08).fastFloor()

        for (x in box.minX.fastFloor()..box.maxX.fastFloor()) {
            for (z in box.minZ.fastFloor()..box.maxZ.fastFloor()) {
                if (world.getBlock(pos.setPos(x, y, z)) != Blocks.WEB) {
                    pos.release()
                    return false
                }
            }
        }

        pos.release()
        return true
    }
}