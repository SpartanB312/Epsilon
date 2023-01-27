package club.eridani.epsilon.client.module.misc

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.windowID
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.network.PacketDecentralizedEvent
import club.eridani.epsilon.client.module.Module
import net.minecraft.network.play.client.CPacketCloseWindow

object XCarry : Module(
    name = "XCarry",
    category = Category.Misc,
    alias = arrayOf("ExtraInventory"),
    description = "The crafting slots in your inventory become extra storage space"
) {

    private val forceCancel = setting("Force", false)

    init {
        decentralizedListener(PacketDecentralizedEvent.Send) { event ->
            if (event.packet !is CPacketCloseWindow) return@decentralizedListener
            if (event.packet.windowID == mc.player.inventoryContainer.windowId || forceCancel.value) event.cancel()
        }
    }

}