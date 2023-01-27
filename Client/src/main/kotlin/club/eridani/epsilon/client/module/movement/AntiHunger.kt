package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.PacketEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.common.extensions.onGround
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer

internal object AntiHunger : Module(
    name = "AntiHunger",
    category = Category.Movement,
    description = "Reduces hunger lost when moving around"
) {
    private val cancelMovementState = setting("Cancel Movement State", true)

    init {
        listener<PacketEvent.Send> {
            if (mc.player == null) return@listener
            if (cancelMovementState.value && it.packet is CPacketEntityAction) {
                if (it.packet.action == CPacketEntityAction.Action.START_SPRINTING || it.packet.action == CPacketEntityAction.Action.STOP_SPRINTING) {
                    it.cancel()
                }
            }
            if (it.packet is CPacketPlayer) {
                // Trick the game to think that tha player is flying even if he is on ground. Also check if the player is flying with the Elytra.
                it.packet.onGround =
                    (mc.player.fallDistance <= 0 || mc.playerController.isHittingBlock) && mc.player.isElytraFlying
            }
        }
    }
}