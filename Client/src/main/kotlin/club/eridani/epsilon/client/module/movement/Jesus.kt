package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.moving
import club.eridani.epsilon.client.common.extensions.y
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.events.AddCollisionBoxEvent
import club.eridani.epsilon.client.event.events.PacketEvent
import club.eridani.epsilon.client.event.events.PlayerTravelEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.BaritoneUtils
import club.eridani.epsilon.client.util.math.fastFloor
import club.eridani.epsilon.client.util.world.getBlock
import net.minecraft.block.BlockLiquid
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityBoat
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

internal object Jesus :
    Module(name = "Jesus", description = "Allows you to walk on water", category = Category.Movement) {
    private val mode by setting("Mode", Mode.Solid)

    private enum class Mode {
        Solid, Dolphin
    }

    private val waterWalkBox = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.99, 1.0)

    override fun onEnable() {
        BaritoneUtils.settings?.assumeWalkOnWater?.value = true
    }

    override fun onDisable() {
        BaritoneUtils.settings?.assumeWalkOnWater?.value = false
    }

    init {
        safeListener<PlayerTravelEvent> {
            if (mc.gameSettings.keyBindSneak.isKeyDown || player.fallDistance > 3.0f || !isInWater(player)) return@safeListener

            if (mode == Mode.Dolphin) {
                player.motionY += 0.03999999910593033 // regular jump speed
            } else {
                player.motionY = 0.1

                player.ridingEntity?.let {
                    if (it !is EntityBoat) it.motionY = 0.3
                }
            }
        }

        safeListener<PacketEvent.Send> {
            if (it.packet !is CPacketPlayer || !it.packet.moving) return@safeListener
            if (mc.gameSettings.keyBindSneak.isKeyDown || player.ticksExisted % 2 != 0) return@safeListener

            val entity = player.ridingEntity ?: player

            if (isAboveLiquid(entity, entity.entityBoundingBox, true) && !isInWater(entity)) {
                it.packet.y += 0.02
            }
        }

        safeListener<AddCollisionBoxEvent> {
            if (mode == Mode.Dolphin) return@safeListener
            if (mc.gameSettings.keyBindSneak.isKeyDown) return@safeListener
            if (it.entity == null || it.entity is EntityBoat) return@safeListener
            if (it.block !is BlockLiquid) return@safeListener

            if (player.fallDistance > 3.0f) return@safeListener

            if (it.entity != player && it.entity != player.ridingEntity) return@safeListener
            if (isInWater(it.entity) || it.entity.posY < it.pos.y) return@safeListener
            if (!isAboveLiquid(it.entity, it.entityBox, false)) return@safeListener

            it.collidingBoxes.add(waterWalkBox.offset(it.pos))
        }
    }

    private fun SafeClientEvent.isInWater(entity: Entity): Boolean {
        val box = entity.entityBoundingBox
        val y = (box.minY + 0.01).fastFloor()
        val pos = BlockPos.PooledMutableBlockPos.retain()

        for (x in box.minX.fastFloor()..box.maxX.fastFloor()) {
            for (z in box.minZ.fastFloor()..box.maxZ.fastFloor()) {
                if (world.getBlock(pos.setPos(x, y, z)) is BlockLiquid) {
                    pos.release()
                    return true
                }
            }
        }

        pos.release()
        return false
    }

    private fun SafeClientEvent.isAboveLiquid(entity: Entity, box: AxisAlignedBB, packet: Boolean): Boolean {
        val offset = when {
            packet -> 0.03
            entity is EntityPlayer -> 0.2
            else -> 0.5
        }

        val y = (box.minY - offset).fastFloor()
        val pos = BlockPos.PooledMutableBlockPos.retain()

        for (x in box.minX.fastFloor()..box.maxX.fastFloor()) {
            for (z in box.minZ.fastFloor()..box.maxZ.fastFloor()) {
                if (world.getBlock(pos.setPos(x, y, z)) is BlockLiquid) {
                    pos.release()
                    return true
                }
            }
        }

        pos.release()
        return false
    }

}