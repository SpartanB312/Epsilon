package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.event.events.InteractEvent
import club.eridani.epsilon.client.event.events.PacketEvent
import club.eridani.epsilon.client.event.events.PlayerInputEvent
import club.eridani.epsilon.client.event.events.TickEvent
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

/**
 * @see MixinBlockSoulSand
 * @see MixinBlockWeb
 */
internal object NoSlowDown :
    Module(name = "NoSlowDown", category = Category.Movement, description = "Prevents being slowed down when using an item or going through cobwebs") {
    private val packet by setting("Packet", true)
    private val bbtt by setting("2B2T", true)
    private val items by setting("Items", true)
    private val sneak by setting("Sneak", false)
    val soulSand by setting("Soul Sand", false)
    val cobweb by setting("Cobweb", false)
    private val slime by setting("Slime", false)

    private var isSneaking = false

    override fun onDisable() {
        if (isSneaking) {
            runSafe {
                if (!player.isSneaking) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
            }
        }

        isSneaking = false

        Blocks.SLIME_BLOCK.setDefaultSlipperiness(0.8f)
    }

    init {
        safeListener<InteractEvent.Item.RightClick> {
            if (bbtt && !isSneaking && items) {
                connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))
                isSneaking = true
            }
        }

        safeListener<PlayerInputEvent> {
            if (player.isRiding) return@safeListener

            if (!player.isHandActive && isSneaking) {
                if (!player.isSneaking) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))
                isSneaking = false
            }

            if ((sneak && player.isSneaking || items && player.isHandActive) && !mc.player.isRiding) {
                it.movementInput.moveStrafe *= 5.0f
                it.movementInput.moveForward *= 5.0f
            }
        }

        safeListener<PacketEvent.Send> {
            if (it.packet is CPacketPlayer && packetNoSlow()) {
                connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }
        }

        safeListener<PacketEvent.PostSend> {
            if (it.packet is CPacketPlayer && packetNoSlow()) {
                connection.sendPacket(CPacketPlayerTryUseItem(player.activeHand))
            }
        }

        safeListener<TickEvent.Pre> {
            if (slime) Blocks.SLIME_BLOCK.setDefaultSlipperiness(0.4945f)  // normal block speed 0.4945
            else Blocks.SLIME_BLOCK.setDefaultSlipperiness(0.8f)
        }
    }

    private fun SafeClientEvent.packetNoSlow(): Boolean {
        return packet && !player.isRiding && player.isHandActive && player.activeItemStack.item == Items.SHIELD
    }
}