package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.PacketEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import net.minecraft.entity.passive.AbstractChestHorse
import net.minecraft.network.play.client.CPacketUseEntity

internal object MountBypass : Module(
    name = "MountBypass",
    category = Category.Misc,
    description = "Might allow you to mount chested animals on servers that block it"
) {
    init {
        listener<PacketEvent.Send> {
            if (it.packet !is CPacketUseEntity || it.packet.action != CPacketUseEntity.Action.INTERACT_AT) return@listener
            if (it.packet.getEntityFromWorld(mc.world) is AbstractChestHorse) it.cancel()
        }
    }
}